#!/bin/bash
set -euo pipefail

log() { echo -e "\n========== $* =========="; }
run() { echo "+ $*"; eval "$*"; }

ROOT_DIR="${WORKSPACE:-$(pwd)}"
FRONT_DIR="$ROOT_DIR/expert-link-frontend"

NODE_VERSION_DIR="node-v16.20.2-linux-x64"
NODE_TAR="$FRONT_DIR/docker/frontend/jar/${NODE_VERSION_DIR}.tar.xz"
NODE_DIR="$FRONT_DIR/docker/frontend/jar/${NODE_VERSION_DIR}"

NODE="$NODE_DIR/bin/node"
NPM_CLI="$NODE_DIR/lib/node_modules/npm/bin/npm-cli.js"

# 纯 URL：不要用 [xx](xx)
NPM_REGISTRY="https://registry.npmmirror.com/"
# 如果你们网络对 npmmirror 不友好，改成：
# NPM_REGISTRY="https://registry.npmjs.org/"

NPM_CACHE_DIR="$ROOT_DIR/.npm-cache"

IMAGE_REPO="harbor.goertek.com/bpit-common-release/expert-link-frontend"
IMAGE_TAG="v${BUILD_NUMBER:-local}"
IMAGE="${IMAGE_REPO}:${IMAGE_TAG}"

DEPLOY_YAML="$FRONT_DIR/docker/frontend/k8s/deploy-sit.yaml"
SVC_YAML="$FRONT_DIR/docker/frontend/k8s/svc-sit.yaml"

K8S_NAMESPACE="bpit-common"
K8S_DEPLOY_NAME="expert-link-frontend-deploy"
K8S_LABEL_SELECTOR="app.kubernetes.io/name=expert-link-frontend"

NO_CACHE=1

log "0. 环境检查"
run "echo ROOT_DIR=$ROOT_DIR"
run "echo FRONT_DIR=$FRONT_DIR"
run "echo BUILD_NUMBER=${BUILD_NUMBER:-<empty>}"
run "echo IMAGE=$IMAGE"
run "echo NPM_REGISTRY=$NPM_REGISTRY"
run "pwd"
run "ls -lrth \"$ROOT_DIR\""

log "1. 准备 Node.js 16"
run "test -f \"$NODE_TAR\""
run "test -d \"$NODE_DIR\" || (echo 'Node 目录不存在，开始解压...' && tar -xf \"$NODE_TAR\" -C \"$FRONT_DIR/docker/frontend/jar/\")"
run "\"$NODE\" -v"
run "test -f \"$NPM_CLI\""
run "\"$NODE\" \"$NPM_CLI\" -v"

log "2. 配置 npm（registry + cache + 重试/超时 + 降并发）"
run "mkdir -p \"$NPM_CACHE_DIR\""
run "\"$NODE\" \"$NPM_CLI\" config set registry \"$NPM_REGISTRY\""

# 注意：这里不要用 run 包起来，否则 set -u 容易触发 REG 未定义
REG="$("$NODE" "$NPM_CLI" config get registry)"
echo "registry(from npm)=$REG"
echo "$REG" | grep -E '^https?://[^]+/?$' >/dev/null

run "\"$NODE\" \"$NPM_CLI\" config set cache \"$NPM_CACHE_DIR\" --global"
run "\"$NODE\" \"$NPM_CLI\" config set fetch-retries 5"
run "\"$NODE\" \"$NPM_CLI\" config set fetch-retry-mintimeout 20000"
run "\"$NODE\" \"$NPM_CLI\" config set fetch-retry-maxtimeout 120000"
run "\"$NODE\" \"$NPM_CLI\" config set fetch-timeout 120000"
run "\"$NODE\" \"$NPM_CLI\" config set maxsockets 3"

log "2.1 Registry 探活"
run "curl -I --max-time 10 \"$NPM_REGISTRY\" || true"
run "curl -I --max-time 10 \"${NPM_REGISTRY%/}/vite\" || true"

log "3. 安装依赖 & 构建"
run "cd \"$FRONT_DIR\""
run "rm -rf dist"

if [ -f package-lock.json ]; then
run "\"$NODE\" \"$NPM_CLI\" ci --prefer-offline --no-audit --no-fund --progress=true"
else
run "\"$NODE\" \"$NPM_CLI\" install --prefer-offline --no-audit --no-fund --progress=true"
fi

run "\"$NODE\" \"$NPM_CLI\" run build"

log "4. 校验 dist"
run "test -d dist"
run "ls -la dist | head -n 50"

log "5. 构建并推送镜像"
if [ \"$NO_CACHE\" = \"1\" ]; then
run "docker build --no-cache -t \"$IMAGE\" -f docker/frontend/dockerfile ."
else
run "docker build -t \"$IMAGE\" -f docker/frontend/dockerfile ."
fi
run "docker push \"$IMAGE\""

log "6. 更新 K8s YAML（替换镜像 tag）并部署"
run "grep -n \"image:\" \"$DEPLOY_YAML\" || true"
run "sed -i \"s/version/${IMAGE_TAG}/g\" \"$DEPLOY_YAML\""
run "grep -n \"image:\" \"$DEPLOY_YAML\" || true"

run "kubectl apply -f \"$SVC_YAML\""
run "kubectl apply -f \"$DEPLOY_YAML\""

log "7. 验证滚动更新"
run "kubectl -n \"$K8S_NAMESPACE\" rollout status deploy/$K8S_DEPLOY_NAME --timeout=180s"
run "kubectl -n \"$K8S_NAMESPACE\" get pods -l \"$K8S_LABEL_SELECTOR\" -o jsonpath='{range .items[*]}{.metadata.name}{\"\\t\"}{.spec.containers[0].image}{\"\\n\"}{end}'"

log "DONE"
echo "✅ 前端流水线完成：$IMAGE"
