package http

import (
	"net/http"
	"os"
	"path/filepath"
	"strings"

	"github.com/go-chi/chi/v5"

	openapi "github.com/n1ckerr0r/dailycanvas/backend/internal/adapters/http/openapi"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/artwork"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/auth"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/favorite"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/platform/config"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/settings"
	"github.com/n1ckerr0r/dailycanvas/backend/internal/user"
)

type Dependencies struct {
	ArtworkService  artwork.Service
	AuthService     auth.Service
	FavoriteService favorite.Service
	SettingsService settings.Service
	UserService     user.Service
}

func NewRouter(cfg config.Config, deps Dependencies) http.Handler {
	router := chi.NewRouter()
	router.Use(cors)

	handler := Handler{
		artworks:  deps.ArtworkService,
		auth:      deps.AuthService,
		favorites: deps.FavoriteService,
		settings:  deps.SettingsService,
		users:     deps.UserService,
	}

	openapi.HandlerFromMuxWithBaseURL(handler, router, "/api/v1")
	router.Get("/healthz", func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
	})

	images := http.StripPrefix("/images/", http.FileServer(http.Dir(cfg.ImagesDir)))
	router.Handle("/images/*", images)
	if strings.TrimSpace(cfg.WebDir) == "" {
		router.NotFound(func(w http.ResponseWriter, r *http.Request) {
			http.NotFound(w, r)
		})
	} else {
		router.NotFound(staticFallback(cfg.WebDir))
	}

	return router
}

func staticFallback(webDir string) http.HandlerFunc {
	files := http.FileServer(http.Dir(webDir))
	return func(w http.ResponseWriter, r *http.Request) {
		if strings.HasPrefix(r.URL.Path, "/api/") {
			http.NotFound(w, r)
			return
		}

		target := filepath.Join(webDir, filepath.Clean(r.URL.Path))
		if _, err := os.Stat(target); err != nil {
			r.URL.Path = "/"
		}
		files.ServeHTTP(w, r)
	}
}

func cors(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, OPTIONS")
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		next.ServeHTTP(w, r)
	})
}
