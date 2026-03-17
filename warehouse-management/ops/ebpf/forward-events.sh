#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL="${1:-http://127.0.0.1:8080/api/security/ebpf/ingest}"
INGEST_KEY="${EBPF_INGEST_KEY:-warehouse-ebpf-agent-key}"

bpftrace "$(dirname "$0")/warehouse-guard.bt" | while IFS= read -r line; do
  if [[ "$line" == \{* ]]; then
    curl -sS -X POST "$BACKEND_URL" \
      -H "Content-Type: application/json" \
      -H "X-EBPF-KEY: $INGEST_KEY" \
      -d "$line" >/dev/null
  fi
done
