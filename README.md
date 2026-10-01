 File Search Service

Spring Boot + PostgreSQL + MinIO backend for authenticated file management and search:
upload, download, delete, rename, list, metadata, folders, and full-text content search.

 Stack

- Spring Boot / Java 
- PostgreSQL - file/folder metadata, plus native full-text search (`tsvector` + GIN index,
  auto-maintained by a trigger — no separate search engine needed)
- MinIO - S3-compatible object storage for the actual file bytes
- Apache Tika - extracts text from uploaded files (PDF, Word, plain text, etc.) at upload
  time so `content_tsv` can index it
- Flyway - versioned schema migrations
- JWT - this service *validates* tokens, it does not issue them (see "Plugging in your
  existing auth" below)



MinIO console: http://localhost:9001 (minioadmin / minioadmin). The `user-files` bucket is
created automatically on startup if it doesn't exist.

Environment variables (all optional, defaults are dev-friendly — see `application.yml`):
`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`,
`MINIO_SECRET_KEY`, `MINIO_BUCKET`.

Plugging in your existing auth

This service never issues JWTs — it only validates them, using `app.jwt.secret`, which
must be the same secret your existing auth service (`spring-boot-auth-api`) signs with.

`JwtService.extractUserId()` currently reads a `userId` claim, falling back to the standard
`sub` claim. If your auth service's token payload uses a different claim name for the user
id, that's the one line to change (`src/main/java/com/crimson/fileSearch/security/JwtService.java`).

Every endpoint requires `Authorization: Bearer <token>`; there's no login/register/refresh
here — that stays in your auth service.

 API

All endpoints are under `/api` and require a Bearer token.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/files` (multipart, fields: `file`, optional `folderId`) | Upload |
| GET | `/api/files/{id}` | Metadata |
| GET | `/api/files/{id}/download?redirect=true\|false` | Presigned download URL (or 302 redirect) |
| DELETE | `/api/files/{id}` | Delete (soft-delete + removes object from MinIO) |
| PATCH | `/api/files/{id}/rename` (`{"newFilename": "..."}`) | Rename |
| GET | `/api/files` | List/filter — `folderId`, `filename`, `extension`, `createdAfter`, `createdBefore`, `page`, `size` |
| GET | `/api/files/search?q=...` | Full-text search over filename + file contents — same filters as list, ranked by relevance |
| POST | `/api/folders` (`{"name": "...", "parentId": "..."}`) | Create folder |
| GET | `/api/folders/{id}` | Folder metadata |
| GET | `/api/folders?parentId=...` | List folders (omit `parentId` for root) |

Search syntax

`GET /api/files/search?q=quarterly report -draft` uses Postgres's `websearch_to_tsquery`,
so users get familiar syntax for free: `"exact phrase"`, `-exclude`, `word1 OR word2`.

 Design notes

- Ownership & isolation: every query is scoped by `owner_id` extracted from the JWT —
  users can only ever see/touch their own files and folders.
- Object key layout: `{ownerId}/{fileId}/{filename}` in MinIO — collision-proof, and
  deleting a user's account could later be a single prefix-delete.
- Soft delete: files get `deleted_at` set rather than being hard-deleted, so undelete or
  audit trails are possible later without a schema change.
- Content extraction is best-effort**: unsupported/corrupt/encrypted files just fall back
  to filename-only search rather than failing the upload.
- Large files skip extraction (`app.content-extraction.max-bytes`, default 20MB) to keep
  uploads fast — adjust or move extraction to an async job if you need it for bigger files.
- Rename in object storage is copy+delete since MinIO/S3 have no native rename.

