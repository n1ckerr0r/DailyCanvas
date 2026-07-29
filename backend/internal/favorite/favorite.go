package favorite

import (
	"context"
	"time"

	"github.com/n1ckerr0r/dailycanvas/backend/internal/artwork"
)

type Sort string

const (
	SortRecentlyAdded Sort = "recently_added"
	SortTitle         Sort = "title"
	SortYear          Sort = "year"
	SortArtist        Sort = "artist"
)

type Item struct {
	Artwork artwork.Artwork
	AddedAt time.Time
}

type Repository interface {
	Add(ctx context.Context, userID string, artworkID string) error
	Remove(ctx context.Context, userID string, artworkID string) error
	List(ctx context.Context, userID string, sort Sort) ([]Item, error)
}

type Service struct {
	repo Repository
}

func NewService(repo Repository) Service {
	return Service{repo: repo}
}

func (s Service) Add(ctx context.Context, userID string, artworkID string) error {
	return s.repo.Add(ctx, userID, artworkID)
}

func (s Service) Remove(ctx context.Context, userID string, artworkID string) error {
	return s.repo.Remove(ctx, userID, artworkID)
}

func (s Service) List(ctx context.Context, userID string, sort Sort) ([]Item, error) {
	if sort == "" {
		sort = SortRecentlyAdded
	}
	return s.repo.List(ctx, userID, sort)
}
