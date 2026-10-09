#!/bin/bash
# Writes the app's configuration from the environment (see docker/README.md), then starts it.
set -euo pipefail

home=/opt/pih-biometrics
config=$home/config/application.yml
license=$home/config/license.lic

refuse() { echo "pih-biometrics: $*" >&2; exit 1; }

threshold=${PIH_BIOMETRICS_MATCHING_THRESHOLD:-}
speed=${PIH_BIOMETRICS_MATCHING_SPEED:-}
size=${PIH_BIOMETRICS_TEMPLATE_SIZE:-}
[[ -z $threshold || $threshold =~ ^[0-9]+$ ]] || refuse "PIH_BIOMETRICS_MATCHING_THRESHOLD must be a whole number, not '$threshold'"
[[ -z $speed || $speed =~ ^(LOW|MEDIUM|HIGH)$ ]] || refuse "PIH_BIOMETRICS_MATCHING_SPEED must be LOW, MEDIUM or HIGH, not '$speed'"
[[ -z $size || $size =~ ^(COMPACT|SMALL|MEDIUM|LARGE)$ ]] || refuse "PIH_BIOMETRICS_TEMPLATE_SIZE must be COMPACT, SMALL, MEDIUM or LARGE, not '$size'"

rm -f "$license"
if [ -n "${PIH_BIOMETRICS_LICENSE_BASE64:-}" ]; then
    (umask 077 && printf '%s' "$PIH_BIOMETRICS_LICENSE_BASE64" | base64 -d > "$license" 2>/dev/null) ||
        refuse "PIH_BIOMETRICS_LICENSE_BASE64 isn't valid base64 (encode the license file with: base64 -w0 <file>.lic)"
else
    echo "pih-biometrics: no PIH_BIOMETRICS_LICENSE_BASE64; it will start but can't enroll or match" >&2
fi

{
    echo 'server:'
    echo '  port: 9000'
    echo 'matchingServiceEnabled: true'
    echo 'fingerprintScanningEnabled: false'
    echo "sqliteDatabasePath: \"$home/data/biometrics.db\""
    if [ -f "$license" ]; then
        echo 'licenseFiles:'
        echo "  - \"$license\""
    fi
    if [ -n "$threshold" ]; then echo "matchingThreshold: \"$threshold\""; fi
    if [ -n "$speed" ]; then echo "matchingSpeed: \"$speed\""; fi
    if [ -n "$size" ]; then echo "templateSize: \"$size\""; fi
} > "$config"

exec java -Djna.library.path="$home/lib" -jar "$home/bin/pih-biometrics.jar" --spring.config.location="file:$config"
