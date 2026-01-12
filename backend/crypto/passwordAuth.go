package crypto

import (
	stdCrypto "crypto/rand"
	"crypto/subtle"
	"encoding/base64"
	"fmt"
	"github.com/juju/errors"
	"golang.org/x/crypto/argon2"
	"log"
	"strings"
)

type Argon2Configuration struct {
	HashRaw    []byte
	Salt       []byte
	TimeCost   uint32
	MemoryCost uint32
	Threads    uint8
	KeyLength  uint32
}

var Argon2Config = Argon2Configuration{
	TimeCost:   2,
	MemoryCost: 64 * 1024,
	Threads:    4,
	KeyLength:  32,
}

func generateCryptographicSalt(saltSize uint32) ([]byte, error) {
	salt := make([]byte, saltSize)
	_, err := stdCrypto.Read(salt)
	if err != nil {
		return nil, err
	}
	return salt, nil
}

func parseArgonEncodedHash(encodedHash string) (*Argon2Configuration, error) {
	elements := strings.Split(encodedHash, "$")
	if len(elements) != 6 {
		return nil, errors.New("Invalid hash format structure")
	}

	if !strings.HasPrefix(elements[1], "argon2id") {
		return nil, errors.New("Unsupported algorithm variant")
	}

	/*
		var version int
		_, err := fmt.Sscanf(elements[2], "v=%d", &version)
		if err != nil {
			return nil, err
		}
	*/

	config := &Argon2Configuration{}
	_, err := fmt.Sscanf(elements[3], "m=%d,t=%d,p=%d", &config.MemoryCost, &config.TimeCost, &config.Threads)
	if err != nil {
		return nil, err
	}

	salt, err := base64.RawStdEncoding.DecodeString(elements[4])
	if err != nil {
		return nil, err
	}

	hash, err := base64.RawStdEncoding.DecodeString(elements[5])
	if err != nil {
		return nil, err
	}

	config.Salt = salt
	config.HashRaw = hash
	config.KeyLength = uint32(len(hash))
	return config, nil
}

func ValidateHash(password string, encodedHash string) (bool, error) {
	config, err := parseArgonEncodedHash(encodedHash)
	if err != nil {
		return false, err
	}

	computedHash := argon2.IDKey(
		[]byte(password),
		config.Salt,
		config.TimeCost,
		config.MemoryCost,
		config.Threads,
		config.KeyLength,
	)

	match := subtle.ConstantTimeCompare(config.HashRaw, computedHash) == 1
	return match, nil
}

func GenerateEncodedHash(password string) (string, error) {
	salt, err := generateCryptographicSalt(16)
	if err != nil {
		log.Print(err.Error())
		return "", err
	}

	hashRaw := argon2.IDKey(
		[]byte(password),
		salt,
		Argon2Config.TimeCost,
		Argon2Config.MemoryCost,
		Argon2Config.Threads,
		Argon2Config.KeyLength,
	)

	formattedHash := fmt.Sprintf(
		"$argon2id$v=%d$m=%d,t=%d,p=%d$%s$%s",
		argon2.Version,
		Argon2Config.MemoryCost,
		Argon2Config.TimeCost,
		Argon2Config.Threads,
		base64.RawStdEncoding.EncodeToString(salt),
		base64.RawStdEncoding.EncodeToString(hashRaw),
	)
	return formattedHash, nil
}
