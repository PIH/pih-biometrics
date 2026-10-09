# pih-biometrics image

`ghcr.io/pih/pih-biometrics`: this app in server mode (stores, enrolls and matches fingerprint
templates) for OpenMRS, on port 9000 over plain HTTP. It is **private**: it contains Neurotechnology's
proprietary native libraries, which their SDK license lets us distribute only inside our own products
to their users. Don't push it anywhere else.

## Configuration

| Variable | Default | |
|---|---|---|
| `PIH_BIOMETRICS_LICENSE_BASE64` | none | The Neurotechnology license file, base64-encoded: `base64 -w0 <file>.lic`. Without it the app starts but can't enroll or match |
| `PIH_BIOMETRICS_MATCHING_THRESHOLD` | app default (72) | whole number |
| `PIH_BIOMETRICS_MATCHING_SPEED` | app default (`LOW`) | `LOW`, `MEDIUM` or `HIGH` |
| `PIH_BIOMETRICS_TEMPLATE_SIZE` | app default (`LARGE`) | `COMPACT`, `SMALL`, `MEDIUM` or `LARGE` |

Mount a volume at `/opt/pih-biometrics/data`: it holds the SQLite database of fingerprint templates
(patient data) and is the working directory. Fingerprint scanning is off: scanners attach to the
client on the user's workstation, not to this server.

The license is an internet license: it's checked online at least once every 7 days, and runs on one
computer at a time. Moving it means stopping it on the old computer and possibly waiting for that
activation to expire.

## Checking a running server

```bash
docker exec <container> pih-biometrics-selftest
```

enrolls a sample template (FVC test prints, not patient data) as a throwaway subject, matches another
print of the same finger against it, deletes the subject, and says OK or FAIL.

## Building

```bash
docker/build.sh ghcr.io/pih/pih-biometrics:dev   # needs gh with read access to PIH/pih-artifacts
docker/test.sh ghcr.io/pih/pih-biometrics:dev    # smoke test, no license needed
```

`build.sh` uses `gh` for the SDK release, and for the Neurotechnology jars too unless
`~/.m2/settings.xml` has credentials for `github-pih-artifacts`; then `gh` needs the `read:packages`
scope (`gh auth refresh -h github.com -s read:packages`).

CI builds and pushes `<version>` on every master build and release, and `latest` on release.
