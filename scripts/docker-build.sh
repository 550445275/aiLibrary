#!/usr/bin/env bash
# 串联：前端 build → Maven package → docker build。在项目根执行。
# 使用：从仓库根执行 ./scripts/docker-build.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo ">> frontend: npm ci && npm run build"
( cd "$ROOT/frontend" && npm ci && npm run build )

echo ">> mvn -DskipTests package"
mvn -DskipTests package

TAG="library:0.0.1"
echo ">> docker build -t $TAG"
docker build -t "$TAG" "$ROOT"
echo "Done. Image: $TAG"
