#!/bin/bash
# Smoke-tests a built image without a license: the entrypoint refuses bad settings, and the app starts, creates its
# database on a fresh volume as its non-root user, and loads the Neurotechnology native libraries (/status can only
# report a missing license once they're loaded). Usage: docker/test.sh <image:tag>
set -euo pipefail

image=${1:?usage: docker/test.sh <image:tag>}
name=pih-biometrics-test-$$
volume=$name-data
status_file=$(mktemp)

cleanup() {
    docker rm -f "$name" >/dev/null 2>&1 || true
    docker volume rm "$volume" >/dev/null 2>&1 || true
    rm -f "$status_file"
}
trap cleanup EXIT

fail() { echo "FAIL: $*" >&2; exit 1; }

# <variable=value> <text the refusal must contain>
expect_refused() {
    local output
    if output=$(docker run --rm -e "$1" "$image" 2>&1); then
        fail "started with $1"
    fi
    [[ $output == *"$2"* ]] || fail "refusing $1 didn't mention '$2': $output"
    echo "ok: refuses $1"
}

expect_refused PIH_BIOMETRICS_MATCHING_SPEED=low PIH_BIOMETRICS_MATCHING_SPEED
expect_refused PIH_BIOMETRICS_TEMPLATE_SIZE=HUGE PIH_BIOMETRICS_TEMPLATE_SIZE
expect_refused PIH_BIOMETRICS_MATCHING_THRESHOLD=high PIH_BIOMETRICS_MATCHING_THRESHOLD
expect_refused 'PIH_BIOMETRICS_LICENSE_BASE64=not base64!' PIH_BIOMETRICS_LICENSE_BASE64

docker run -d --name "$name" -v "$volume:/opt/pih-biometrics/data" -p 127.0.0.1::9000 "$image" >/dev/null
port=$(docker port "$name" 9000/tcp | head -1 | cut -d: -f2)

status=
for _ in $(seq 1 60); do
    code=$(curl -s -o "$status_file" -w '%{http_code}' "http://127.0.0.1:$port/status" || true)
    if [ "$code" = 200 ]; then status=$(cat "$status_file"); break; fi
    if [ "$code" = 500 ]; then fail "/status failed (native libraries not loaded?): $(cat "$status_file")"; fi
    docker inspect -f '{{.State.Running}}' "$name" | grep -q true || fail "exited: $(docker logs "$name" 2>&1 | tail -20)"
    sleep 2
done
[ -n "$status" ] || fail "/status didn't answer within 120s: $(docker logs "$name" 2>&1 | tail -20)"
[[ $status == *'"enabled":false'* && $status == *'Unable to obtain a license'* ]] ||
    fail "expected a missing-license status, got: $status"
echo "ok: starts and loads the native libraries without a license"

docker exec "$name" test -f /opt/pih-biometrics/data/biometrics.db || fail "no database on the volume"
echo "ok: creates its database on a fresh volume"

if docker exec "$name" pih-biometrics-selftest >/dev/null 2>&1; then fail "selftest passed without a license"; fi
echo "ok: selftest fails without a license"

echo PASS
