# DEBUG: scs-db-init volume path

## Symptoms
- `saa-scs-db-init` exit 1: `psql: error: /scs-db/schema.sql: No such file or directory`
- App fails: `relation "public.model_profile" does not exist`
- `docker inspect` Mounts.Source = `.../docker/db` (empty) instead of `projects/smart-cs-platform/db`

## Root Cause
Docker Compose 多文件合并时，override 中相对路径 `./db` 相对**首个** compose 文件父目录（`docker/`）解析，而非 override 所在的 `projects/smart-cs-platform/`。

## Fix Direction
将三处企业项目 override 的 `./db`（及 smartcs 的 `./monitor/...`）改为相对 `docker/` 的路径，例如 `../projects/smart-cs-platform/db`；或在文档/脚本中统一加 `--project-directory projects/<proj>`（需验证与首文件路径组合）。

推荐：改 volume 为 `../projects/<proj>/db:...`，与现有 `docker compose -f docker/... -f projects/.../override.yml` 调用方式兼容，且与 `deploy-smoke.sh` 一致。
