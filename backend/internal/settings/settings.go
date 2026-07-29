package settings

import "context"

type NotificationSettings struct {
	Enabled  bool
	Time     string
	TimeZone string
}

type Settings struct {
	SelectedCollections []string
	Notifications       NotificationSettings
}

type Patch struct {
	SelectedCollections *[]string
	Notifications       *NotificationPatch
}

type NotificationPatch struct {
	Enabled  *bool
	Time     *string
	TimeZone *string
}

type Repository interface {
	Get(ctx context.Context, userID string) (Settings, error)
	Save(ctx context.Context, userID string, value Settings) (Settings, error)
}

type Service struct {
	repo Repository
}

func NewService(repo Repository) Service {
	return Service{repo: repo}
}

func (s Service) Get(ctx context.Context, userID string) (Settings, error) {
	return s.repo.Get(ctx, userID)
}

func (s Service) Update(ctx context.Context, userID string, patch Patch) (Settings, error) {
	current, err := s.repo.Get(ctx, userID)
	if err != nil {
		return Settings{}, err
	}

	if patch.SelectedCollections != nil {
		current.SelectedCollections = *patch.SelectedCollections
	}
	if patch.Notifications != nil {
		if patch.Notifications.Enabled != nil {
			current.Notifications.Enabled = *patch.Notifications.Enabled
		}
		if patch.Notifications.Time != nil {
			current.Notifications.Time = *patch.Notifications.Time
		}
		if patch.Notifications.TimeZone != nil {
			current.Notifications.TimeZone = *patch.Notifications.TimeZone
		}
	}

	return s.repo.Save(ctx, userID, current)
}
