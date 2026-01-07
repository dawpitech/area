package models

import (
	"gorm.io/gorm"
	"time"
)

type LogType int

const (
	ErrorLog = iota
	WarnLog
)

var LogTypeName = map[LogType]string{
	ErrorLog: "error",
	WarnLog:  "warn",
}

func (logType LogType) String() string {
	return LogTypeName[logType]
}

type LogEntry struct {
	gorm.Model
	WorkflowID  uint
	OwnerUserID uint
	Timestamp   time.Time
	Type        string
	Message     string
}
