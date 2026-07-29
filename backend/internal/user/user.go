package user

import (
	"context"
	"time"
)

type User struct {
	ID        string
	Email     string
	CreatedAt time.Time
}

type Repository interface {
	GetDefault(ctx context.Context) (User, error)
}

type Service struct {
	repo Repository
}

func NewService(repo Repository) Service {
	return Service{repo: repo}
}

func (s Service) Current(ctx context.Context) (User, error) {
	return s.repo.GetDefault(ctx)
}
