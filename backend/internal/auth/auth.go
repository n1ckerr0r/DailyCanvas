package auth

import (
	"crypto/rand"
	"encoding/base64"
	"fmt"

	"github.com/n1ckerr0r/dailycanvas/backend/internal/platform/config"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/user"
)

type Tokens struct {
	AccessToken           string
	RefreshToken          string
	TokenType             string
	AccessTokenExpiresIn  int
	RefreshTokenExpiresIn int
}

type Service struct {
	cfg config.Auth
}

func NewService(cfg config.Auth) Service {
	return Service{cfg: cfg}
}

func (s Service) Issue(_ user.User) (Tokens, error) {
	accessToken, err := randomToken("atk")
	if err != nil {
		return Tokens{}, err
	}

	refreshToken, err := randomToken("rtk")
	if err != nil {
		return Tokens{}, err
	}

	return Tokens{
		AccessToken:           accessToken,
		RefreshToken:          refreshToken,
		TokenType:             s.cfg.TokenType,
		AccessTokenExpiresIn:  s.cfg.AccessTokenTTLSeconds,
		RefreshTokenExpiresIn: s.cfg.RefreshTokenTTLSeconds,
	}, nil
}

func randomToken(prefix string) (string, error) {
	buf := make([]byte, 24)
	if _, err := rand.Read(buf); err != nil {
		return "", fmt.Errorf("generate token: %w", err)
	}
	return prefix + "_" + base64.RawURLEncoding.EncodeToString(buf), nil
}
