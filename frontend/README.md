# Frontend

This directory contains the desktop website frontend. It does not depend on a bundler and can be served as a regular static directory.

## Files

- `index.html` - desktop interface shell.
- `styles.css` - complete visual system for the desktop version.
- `app.js` - backend data loading, tab switching, card rendering, settings, and detail rendering.
- `config.js` - frontend runtime config. Set `apiBaseUrl` here instead of changing `app.js` when the backend moves to another domain or port.

## How It Works

- By default, `config.js` builds the API URL from `window.location.origin`, so the frontend expects the API next to the backend-served site.
- The backend can serve this directory as the root site through the `WEB_DIR` variable.
- No API addresses are hardcoded in `app.js`.

## Running

If the backend is running and `WEB_DIR` points to `../frontend`, the site is available at the backend root:

```bash
http://localhost:37117/
```
