# Built by docker/build.sh, which assembles the context: the app jar, the Neurotechnology Linux libraries, these
# scripts and the self-test samples. Contains proprietary Neurotechnology libraries: push only to the private
# ghcr.io/pih/pih-biometrics.
FROM eclipse-temurin:8-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 9000 biometrics \
    && useradd --system --uid 9000 --gid 9000 --home-dir /opt/pih-biometrics/data biometrics

COPY pih-biometrics.jar /opt/pih-biometrics/bin/pih-biometrics.jar
COPY neurotec-lib/ /opt/pih-biometrics/lib/
COPY selftest/ /opt/pih-biometrics/selftest/
COPY entrypoint.sh /usr/local/bin/pih-biometrics-entrypoint
COPY selftest.sh /usr/local/bin/pih-biometrics-selftest

RUN mkdir -p /opt/pih-biometrics/config /opt/pih-biometrics/data \
    && chown biometrics:biometrics /opt/pih-biometrics/config /opt/pih-biometrics/data

USER biometrics
WORKDIR /opt/pih-biometrics/data
EXPOSE 9000
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s CMD curl -fsS -o /dev/null http://localhost:9000/status
ENTRYPOINT ["/usr/local/bin/pih-biometrics-entrypoint"]
