# Safe Delete

A small full-stack application for safely deleting files from a restricted directory. The backend validates every requested path against a configured `allowed_root` before deletion, refusing symlinks, missing files, directories, and anything outside the allowed root. The frontend provides a simple textarea-based UI for submitting paths and viewing per-path results.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.jsx
        ├── App.jsx
        └── App.css
```

## How to run

**Backend** (listens on `http://localhost:8080`):

```bash
cd backend
mvn spring-boot:run
```

**Frontend** (dev server on `http://localhost:5173`, proxies `/api` to the backend):

```bash
cd frontend
npm install
npm run dev
```

## API

### `POST /api/delete`

Deletes a list of file paths, subject to the safety checks described below.

- **Request body:** JSON array of absolute file paths, e.g. `["/abs/path/a.txt", "/abs/path/b.txt"]`
- **Response:** JSON object with a `results` array, one entry per requested path:

```json
{
  "results": [
    { "path": "/abs/path/a.txt", "status": "deleted" },
    { "path": "/abs/path/b.txt", "status": "refused", "reason": "outside_allowed_root" }
  ]
}
```

Possible `status` values: `deleted`, `refused`, `error`. Possible `reason` values for `refused`: `symlink`, `not_found`, `outside_allowed_root`, `not_regular_file`.

If `allowed_root` itself cannot be resolved, the endpoint returns `{ "error": "..." }` instead.

## Notes

- **Allowed root:** configured via `app.allowed-root` in `backend/src/main/resources/application.properties` (default `./allowed_root`). The directory must exist before the endpoint is called; otherwise all requests fail with an error.
- **Safety checks:** each path is resolved with `toRealPath()` and must be a regular file strictly inside the resolved `allowed_root`. Symlinks are refused outright, and directories are rejected (`not_regular_file`).
- **No authentication or authorization** is implemented. Do not expose this service on an untrusted network as-is.
- **No CORS configuration** on the backend; the frontend relies on Vite's dev proxy. For production, serve the built frontend behind the same origin as the backend or add explicit CORS rules.
- **No persistence or audit log** of deletions is kept.
- **Path input is trusted as-is** by the client; the server performs all validation, but there is no rate limiting or request size limit beyond Spring Boot defaults.
- **Build output:** `npm run build` produces static assets in `frontend/dist/`; the backend does not currently serve them.