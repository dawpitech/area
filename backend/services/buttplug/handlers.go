package buttplug

import (
	"context"
	"dawpitech/area/engines/workflowEngine"
	"dawpitech/area/models"
	"log"
	"slices"
	"strconv"
	"time"

	"github.com/juju/errors"
	"golang.org/x/sync/errgroup"
	"libdb.so/go-buttplug"
	"libdb.so/go-buttplug/schema/ptr"
	"libdb.so/go-buttplug/schema/v3"
)

type Config struct {
	Addr        string
	SetLevel    float64
	PollSensors time.Duration
}

var config = Config{
	Addr:        "",
	SetLevel:    1.0,
	PollSensors: 10 * time.Second,
}

type Session struct {
	wg *errgroup.Group
	ws *buttplug.Websocket
}

func VibrateHandler(ctx models.Context) error {
	vibrationAmount, vibrationAmountOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "buttplug_vibrate_device_intensity", ctx)
	vibrationDuration, vibrationDurationOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "buttplug_vibrate_device_duration", ctx)
	buttplugServer, buttplugServerOK := workflowEngine.GetParam(workflowEngine.ReactionHandler, "buttplug_server_ip", ctx)

	if !(vibrationAmountOK && vibrationDurationOK && buttplugServerOK) {
		return errors.New("Missing parameters")
	}

	amount, err := strconv.Atoi(vibrationAmount)
	duration, err2 := strconv.Atoi(vibrationDuration)
	if err != nil || err2 != nil {
		return errors.New("Invalid intensity value")
	}
	if amount < 0 || amount > 100 {
		return errors.New("Intensity value must be between 0 and 100")
	}

	config.SetLevel = float64(amount) / 100.0
	config.PollSensors = time.Duration(duration) * time.Second
	config.Addr = "ws://" + buttplugServer + ":12345/"

	go runVibration(config)
	return nil
}

func runVibration(cfg Config) {
	ctx, cancel := context.WithTimeout(context.Background(), cfg.PollSensors)
	defer cancel()

	sess := &Session{
		wg: &errgroup.Group{},
		ws: buttplug.NewWebsocket(cfg.Addr, nil),
	}

	sess.startConnection(ctx)
	sess.startPollingSensors(ctx, cfg.PollSensors)
	sess.startVibratingAll(ctx, cfg.SetLevel)

	done := make(chan struct{})
	go func() {
		err := sess.wg.Wait()
		if err != nil {
			return
		}
		close(done)
	}()

	select {
	case <-done:
		log.Println("Vibration session completed")
	case <-ctx.Done():
		if errors.Is(ctx.Err(), context.DeadlineExceeded) {
			log.Println("Vibration session ended due to timeout")
		} else {
			log.Printf("Vibration session ended: %v\n", ctx.Err())
		}
	}
}

func (s *Session) startConnection(ctx context.Context) {
	s.wg.Go(func() error {
		return s.ws.Start(ctx)
	})
}

func (s *Session) startPollingSensors(ctx context.Context, pollInterval time.Duration) {
	type sensorsKey struct {
		deviceIndex schema.DeviceIndex
		sensorIndex int
	}

	msgs, cancel := s.ws.MessageChannel(ctx)
	s.wg.Go(func() error {
		defer cancel()

		sensors := map[sensorsKey]schema.SensorReadCmdItem{}
		ticker := time.NewTicker(pollInterval)
		defer ticker.Stop()
		poll := make(chan struct{}, 1)

		for {
			select {
			case <-ctx.Done():
				return ctx.Err()
			case msg := <-msgs:
				switch msg := msg.(type) {
				case *schema.DeviceListMessage:
					sensors = make(map[sensorsKey]schema.SensorReadCmdItem)
					for _, device := range msg.Devices {
						for i, sensor := range device.DeviceMessages.SensorReadCmd {
							sensors[sensorsKey{deviceIndex: device.DeviceIndex, sensorIndex: i}] = sensor
						}
					}
					select {
					case poll <- struct{}{}:
					default:
					}
				case *schema.SensorReadingMessage:
					key := sensorsKey{deviceIndex: msg.DeviceIndex, sensorIndex: msg.SensorIndex}
					if sensor, ok := sensors[key]; ok {
						log.Printf("Sensor reading: DeviceIndex=%d SensorIndex=%d Data=%v Descriptor=%s\n",
							msg.DeviceIndex, msg.SensorIndex, msg.Data, sensor.FeatureDescriptor)
					}
				}
			case <-ticker.C:
				select {
				case poll <- struct{}{}:
				default:
				}
			case <-poll:
				for k, sensor := range sensors {
					_, err := s.ws.Send(ctx, &schema.SensorReadCmdMessage{
						DeviceIndex: k.deviceIndex,
						SensorIndex: k.sensorIndex,
						SensorType:  sensor.SensorType,
					})
					if err != nil {
						log.Printf("Failed to poll sensor: %+v, err: %v\n", k, err)
					}
				}
			}
		}
	})
}

func (s *Session) startVibratingAll(ctx context.Context, intensity float64) {
	s.wg.Go(func() error {
		reply, err := s.ws.SendCommand(ctx, &schema.RequestDeviceListMessage{})
		if err != nil {
			return err
		}

		deviceList, ok := reply.(*schema.DeviceListMessage)
		if !ok {
			return errors.New("Invalid device list response")
		}

		for _, device := range deviceList.Devices {
			if slices.ContainsFunc(device.DeviceMessages.ScalarCmd, func(cmd schema.ScalarCmdInfo) bool {
				return ptr.ValueOrZero(cmd.ActuatorType) == buttplug.ActuatorVibrate
			}) {
				scalars := []schema.Scalar{}
				for i, cmd := range device.DeviceMessages.ScalarCmd {
					if ptr.ValueOrZero(cmd.ActuatorType) == buttplug.ActuatorVibrate {
						scalars = append(scalars, schema.Scalar{
							Index:        i,
							Scalar:       intensity,
							ActuatorType: *cmd.ActuatorType,
						})
					}
				}
				_, sendErr := s.ws.Send(ctx, &schema.ScalarCmdMessage{
					DeviceIndex: device.DeviceIndex,
					Scalars:     scalars,
				})
				if sendErr != nil {
					log.Printf("Failed to vibrate device %q: %v\n", device.DeviceName, sendErr)
				}
			}
		}

		return nil
	})
}
