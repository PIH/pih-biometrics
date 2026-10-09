#!/bin/bash
# Checks this server can enroll and match with its license: enrolls a sample template (FVC test prints from this
# repo's test resources, not patient data) as a throwaway subject, matches another print of the same finger against it,
# and deletes the subject. Usage, on the host: docker exec <container> pih-biometrics-selftest
set -euo pipefail

url=http://localhost:9000
samples=/opt/pih-biometrics/selftest
subject=selftest-$(date +%s)

# <subjectId JSON value> <sample file>
body() { printf '{"subjectId":%s,"fingerprints":[{"format":"PROPRIETARY","template":"%s"}]}' "$1" "$(cat "$samples/$2")"; }

cleanup() { curl -s -o /dev/null -X DELETE "$url/subject/$subject" || true; }
trap cleanup EXIT

echo "status: $(curl -fsS "$url/status")"
body "\"$subject\"" 101-01-1 | curl -fsS -o /dev/null -X POST -H 'Content-Type: application/json' --data-binary @- "$url/subject"
matches=$(body null 101-01-2 | curl -fsS -X POST -H 'Content-Type: application/json' --data-binary @- "$url/match")
if [[ $matches == *"\"subjectId\":\"$subject\""* ]]; then
    echo "OK: enrolled $subject and matched it"
else
    echo "FAIL: $subject wasn't matched: $matches" >&2
    exit 1
fi
