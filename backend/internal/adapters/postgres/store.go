package postgres

import (
	"context"
	"database/sql"
	"fmt"
	"strings"
	"time"

	"github.com/lib/pq"

	"github.com/n1ckerr0r/dailycanvas/backend/internal/artwork"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/favorite"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/settings"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/user"
)

type Store struct {
	db *sql.DB
}

func NewStore(db *sql.DB) *Store {
	return &Store{db: db}
}

func (s *Store) GetDefault(ctx context.Context) (user.User, error) {
	var value user.User
	err := s.db.QueryRowContext(ctx, `
		SELECT id, email, created_at
		FROM users
		ORDER BY created_at ASC
		LIMIT 1
	`).Scan(&value.ID, &value.Email, &value.CreatedAt)
	return value, err
}

func (s *Store) Get(ctx context.Context, userID string) (settings.Settings, error) {
	var rawCollections []string
	value := settings.Settings{}

	err := s.db.QueryRowContext(ctx, `
		SELECT selected_collections, notifications_enabled, notification_time, notification_time_zone
		FROM user_settings
		WHERE user_id = $1
	`, userID).Scan(pq.Array(&rawCollections), &value.Notifications.Enabled, &value.Notifications.Time, &value.Notifications.TimeZone)
	if err != nil {
		return value, err
	}

	value.SelectedCollections = rawCollections
	return value, nil
}

func (s *Store) Save(ctx context.Context, userID string, value settings.Settings) (settings.Settings, error) {
	_, err := s.db.ExecContext(ctx, `
		UPDATE user_settings
		SET selected_collections = $2,
			notifications_enabled = $3,
			notification_time = $4,
			notification_time_zone = $5
		WHERE user_id = $1
	`, userID, pq.Array(value.SelectedCollections), value.Notifications.Enabled, value.Notifications.Time, value.Notifications.TimeZone)
	if err != nil {
		return settings.Settings{}, err
	}
	return s.Get(ctx, userID)
}

func (s *Store) GetToday(ctx context.Context, userID string, collections []artwork.Collection) (artwork.Artwork, error) {
	items, err := s.listArtworks(ctx, userID, baseArtworkQuery(`
		WHERE a.collection = ANY($2)
		ORDER BY a.published_on DESC, a.title ASC
		LIMIT 1
	`), userID, pq.Array(collectionStrings(collections)))
	if err != nil {
		return artwork.Artwork{}, err
	}
	if len(items) == 0 {
		return artwork.Artwork{}, artwork.ErrNotFound
	}
	return items[0], nil
}

func (s *Store) ListGallery(ctx context.Context, userID string, filter artwork.Filter, collections []artwork.Collection) ([]artwork.Artwork, error) {
	args := []any{userID}
	where := []string{"a.collection = ANY($2)"}
	args = append(args, pq.Array(collectionStrings(collections)))

	if filter.Collection != nil {
		args = append(args, string(*filter.Collection))
		where = append(where, fmt.Sprintf("a.collection = $%d", len(args)))
	}
	if filter.TagID != "" {
		args = append(args, filter.TagID)
		where = append(where, fmt.Sprintf("EXISTS (SELECT 1 FROM artwork_tags tag WHERE tag.artwork_id = a.id AND tag.tag_id = $%d)", len(args)))
	}
	if strings.TrimSpace(filter.Query) != "" {
		args = append(args, "%"+strings.TrimSpace(filter.Query)+"%")
		where = append(where, fmt.Sprintf(`(
			a.title ILIKE $%[1]d OR
			a.artist_name ILIKE $%[1]d OR
			a.description ILIKE $%[1]d OR
			EXISTS (SELECT 1 FROM artwork_tags tag WHERE tag.artwork_id = a.id AND tag.tag_name ILIKE $%[1]d)
		)`, len(args)))
	}

	args = append(args, filter.Limit, (filter.Page-1)*filter.Limit)
	query := baseArtworkQuery(`
		WHERE ` + strings.Join(where, " AND ") + `
		ORDER BY a.published_on DESC, a.title ASC
		LIMIT $` + fmt.Sprintf("%d", len(args)-1) + ` OFFSET $` + fmt.Sprintf("%d", len(args)))

	return s.listArtworks(ctx, userID, query, args...)
}

func (s *Store) GetByID(ctx context.Context, userID string, artworkID string) (artwork.Artwork, error) {
	items, err := s.listArtworks(ctx, userID, baseArtworkQuery(`
		WHERE a.id = $2
		LIMIT 1
	`), userID, artworkID)
	if err != nil {
		return artwork.Artwork{}, err
	}
	if len(items) == 0 {
		return artwork.Artwork{}, artwork.ErrNotFound
	}
	return items[0], nil
}

func (s *Store) GetCalendar(ctx context.Context, from time.Time, to time.Time) ([]artwork.CalendarItem, error) {
	rows, err := s.db.QueryContext(ctx, `
		SELECT published_on, id, image_path
		FROM artworks
		WHERE published_on BETWEEN $1 AND $2
		ORDER BY published_on ASC
	`, from, to)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	items := make([]artwork.CalendarItem, 0)
	for rows.Next() {
		var value artwork.CalendarItem
		if err := rows.Scan(&value.Date, &value.ArtworkID, &value.ImagePath); err != nil {
			return nil, err
		}
		items = append(items, value)
	}
	return items, rows.Err()
}

func (s *Store) ListTags(ctx context.Context) ([]artwork.Tag, error) {
	rows, err := s.db.QueryContext(ctx, `
		SELECT DISTINCT tag_id, tag_name, tag_type
		FROM artwork_tags
		ORDER BY tag_name ASC
	`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	items := make([]artwork.Tag, 0)
	for rows.Next() {
		var item artwork.Tag
		if err := rows.Scan(&item.ID, &item.Name, &item.Type); err != nil {
			return nil, err
		}
		items = append(items, item)
	}
	return items, rows.Err()
}

func (s *Store) HasViewedOn(ctx context.Context, userID string, artworkID string, day time.Time) (bool, error) {
	var exists bool
	err := s.db.QueryRowContext(ctx, `
		SELECT EXISTS(
			SELECT 1
			FROM artwork_views
			WHERE user_id = $1 AND artwork_id = $2 AND viewed_on = $3
		)
	`, userID, artworkID, dateOnly(day)).Scan(&exists)
	return exists, err
}

func (s *Store) MarkViewedOn(ctx context.Context, userID string, artworkID string, day time.Time) error {
	_, err := s.db.ExecContext(ctx, `
		INSERT INTO artwork_views(user_id, artwork_id, viewed_on)
		VALUES ($1, $2, $3)
		ON CONFLICT (user_id, artwork_id, viewed_on) DO NOTHING
	`, userID, artworkID, dateOnly(day))
	return err
}

func (s *Store) Add(ctx context.Context, userID string, artworkID string) error {
	_, err := s.db.ExecContext(ctx, `
		INSERT INTO favorites(user_id, artwork_id, added_at)
		VALUES ($1, $2, NOW())
		ON CONFLICT (user_id, artwork_id) DO UPDATE SET added_at = EXCLUDED.added_at
	`, userID, artworkID)
	return err
}

func (s *Store) Remove(ctx context.Context, userID string, artworkID string) error {
	_, err := s.db.ExecContext(ctx, `DELETE FROM favorites WHERE user_id = $1 AND artwork_id = $2`, userID, artworkID)
	return err
}

func (s *Store) List(ctx context.Context, userID string, sort favorite.Sort) ([]favorite.Item, error) {
	orderBy := "f.added_at DESC"
	switch sort {
	case favorite.SortArtist:
		orderBy = "a.artist_name ASC, f.added_at DESC"
	case favorite.SortTitle:
		orderBy = "a.title ASC, f.added_at DESC"
	case favorite.SortYear:
		orderBy = "a.year_value ASC, f.added_at DESC"
	}

	query := `
		SELECT
			a.id, a.title, a.artist_id, a.artist_name, a.country, a.collection,
			a.year_value, a.year_label, a.image_path, a.description, a.facts, a.published_on,
			TRUE AS is_favorite, f.added_at
		FROM favorites f
		JOIN artworks a ON a.id = f.artwork_id
		WHERE f.user_id = $1
		ORDER BY ` + orderBy

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	items := make([]favorite.Item, 0)
	ids := make([]string, 0)
	for rows.Next() {
		var value favorite.Item
		var rawFacts []string
		if err := rows.Scan(
			&value.Artwork.ID,
			&value.Artwork.Title,
			&value.Artwork.Artist.ID,
			&value.Artwork.Artist.Name,
			&value.Artwork.Country,
			&value.Artwork.Collection,
			&value.Artwork.YearValue,
			&value.Artwork.YearLabel,
			&value.Artwork.ImagePath,
			&value.Artwork.Description,
			pq.Array(&rawFacts),
			&value.Artwork.PublishedOn,
			&value.Artwork.IsFavorite,
			&value.AddedAt,
		); err != nil {
			return nil, err
		}
		value.Artwork.Facts = rawFacts
		items = append(items, value)
		ids = append(ids, value.Artwork.ID)
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}

	tagsByArtwork, err := s.tagsByArtworkIDs(ctx, ids)
	if err != nil {
		return nil, err
	}
	for i := range items {
		items[i].Artwork.Tags = tagsByArtwork[items[i].Artwork.ID]
	}
	return items, nil
}

func (s *Store) listArtworks(ctx context.Context, userID string, query string, args ...any) ([]artwork.Artwork, error) {
	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	items := make([]artwork.Artwork, 0)
	ids := make([]string, 0)
	for rows.Next() {
		var item artwork.Artwork
		var rawFacts []string
		if err := rows.Scan(
			&item.ID,
			&item.Title,
			&item.Artist.ID,
			&item.Artist.Name,
			&item.Country,
			&item.Collection,
			&item.YearValue,
			&item.YearLabel,
			&item.ImagePath,
			&item.Description,
			pq.Array(&rawFacts),
			&item.PublishedOn,
			&item.IsFavorite,
		); err != nil {
			return nil, err
		}
		item.Facts = rawFacts
		items = append(items, item)
		ids = append(ids, item.ID)
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}

	tagsByArtwork, err := s.tagsByArtworkIDs(ctx, ids)
	if err != nil {
		return nil, err
	}
	for i := range items {
		items[i].Tags = tagsByArtwork[items[i].ID]
	}

	return items, nil
}

func (s *Store) tagsByArtworkIDs(ctx context.Context, ids []string) (map[string][]artwork.Tag, error) {
	result := make(map[string][]artwork.Tag, len(ids))
	if len(ids) == 0 {
		return result, nil
	}

	rows, err := s.db.QueryContext(ctx, `
		SELECT artwork_id, tag_id, tag_name, tag_type
		FROM artwork_tags
		WHERE artwork_id = ANY($1)
		ORDER BY tag_name ASC
	`, pq.Array(ids))
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	for rows.Next() {
		var artworkID string
		var tag artwork.Tag
		if err := rows.Scan(&artworkID, &tag.ID, &tag.Name, &tag.Type); err != nil {
			return nil, err
		}
		result[artworkID] = append(result[artworkID], tag)
	}
	return result, rows.Err()
}

func baseArtworkQuery(suffix string) string {
	return `
		SELECT
			a.id, a.title, a.artist_id, a.artist_name, a.country, a.collection,
			a.year_value, a.year_label, a.image_path, a.description, a.facts, a.published_on,
			COALESCE(f.artwork_id IS NOT NULL, FALSE) AS is_favorite
		FROM artworks a
		LEFT JOIN favorites f
			ON f.artwork_id = a.id AND f.user_id = $1
	` + suffix
}

func collectionStrings(items []artwork.Collection) []string {
	result := make([]string, 0, len(items))
	for _, item := range items {
		result = append(result, string(item))
	}
	return result
}

func dateOnly(value time.Time) time.Time {
	return time.Date(value.Year(), value.Month(), value.Day(), 0, 0, 0, 0, time.UTC)
}
