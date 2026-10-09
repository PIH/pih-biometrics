#!/bin/bash
# Builds the image: the app jar (Maven), the Neurotechnology Linux libraries from the private neurotec-sdk-9.0.0.0
# release in PIH/pih-artifacts (cached in docker/.sdk), the scripts in docker/ and the self-test samples. Needs `gh`
# logged in with read access to PIH/pih-artifacts, including the read:packages scope unless ~/.m2/settings.xml has
# credentials for it. Usage: docker/build.sh <image:tag>
set -euo pipefail

image=${1:?usage: docker/build.sh <image:tag>}
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
context=$root/docker/build
sdk_cache=$root/docker/.sdk
sdk_release=neurotec-sdk-9.0.0.0
sdk_lib=Neurotec_Biometric_9_0_SDK/Lib/Linux_x86_64

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

# The Neurotechnology jars come from PIH/pih-artifacts' Maven registry: without credentials for it in
# ~/.m2/settings.xml, use gh's token (needs the read:packages scope).
maven_settings=()
if ! grep -qs '<id>github-pih-artifacts</id>' ~/.m2/settings.xml; then
    token=$(gh auth token)
    cat > "$tmp/settings.xml" <<EOF
<settings><servers><server>
  <id>github-pih-artifacts</id><username>token</username><password>${token}</password>
</server></servers></settings>
EOF
    maven_settings=(-s "$tmp/settings.xml")
fi
mvn -B -q "${maven_settings[@]}" -f "$root/pom.xml" clean package -Dmaven.test.skip=true
jars=()
while IFS= read -r jar; do jars+=("$jar"); done < <(find "$root/target" -maxdepth 1 -name 'pih-biometrics-*.jar' \
    ! -name '*-sources.jar' ! -name '*-javadoc.jar')
[ ${#jars[@]} -eq 1 ] || { echo "expected one app jar in target/, found ${#jars[@]}" >&2; exit 1; }

if [ ! -d "$sdk_cache/$sdk_lib" ]; then
    gh release download "$sdk_release" -R PIH/pih-artifacts -D "$tmp"
    mkdir -p "$sdk_cache"
    tar -xzf "$tmp"/*.tar.gz -C "$sdk_cache" "$sdk_lib"
    rm -f "$tmp"/*.tar.gz
fi

rm -rf "$context"
mkdir -p "$context/selftest"
cp "${jars[0]}" "$context/pih-biometrics.jar"
cp -r "$sdk_cache/$sdk_lib" "$context/neurotec-lib"
cp "$root/docker/entrypoint.sh" "$root/docker/selftest.sh" "$context/"
cp "$root"/src/test/resources/org/pih/biometric/service/101-01-1 \
   "$root"/src/test/resources/org/pih/biometric/service/101-01-2 "$context/selftest/"
docker build -t "$image" -f "$root/docker/Dockerfile" "$context"
