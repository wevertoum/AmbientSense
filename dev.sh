set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="${FRONTEND_DIR:-frontend-react}"

if ! command -v mvn >/dev/null 2>&1; then
  echo "mvn não encontrado no PATH. Instale Maven ou use o wrapper do projeto." >&2
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "npm não encontrado no PATH." >&2
  exit 1
fi

if [[ ! -d "$ROOT/backend-java" ]] || [[ ! -d "$ROOT/$FRONTEND_DIR" ]]; then
  echo "Pastas esperadas: backend-java e $FRONTEND_DIR (a partir de $ROOT)." >&2
  exit 1
fi


kill_tree() {
  local pid=$1
  local child
  [[ -z "$pid" ]] && return 0
  for child in $(pgrep -P "$pid" 2>/dev/null); do
    [[ -n "$child" ]] && kill_tree "$child"
  done
  kill -TERM "$pid" 2>/dev/null || true
}

BPID=""
FPID=""
cleanup() {
  [[ -n "$BPID" ]] && kill_tree "$BPID"
  [[ -n "$FPID" ]] && kill_tree "$FPID"
}
trap cleanup EXIT INT TERM HUP

echo ">> Backend:  mvn spring-boot:run (backend-java) → http://localhost:8080"
(
  cd "$ROOT/backend-java"
  mvn spring-boot:run
) &
BPID=$!

echo ">> Frontend: npm run dev ($FRONTEND_DIR) — veja a URL do Vite no log abaixo"
(
  cd "$ROOT/$FRONTEND_DIR"
  npm run dev
) &
FPID=$!

wait
