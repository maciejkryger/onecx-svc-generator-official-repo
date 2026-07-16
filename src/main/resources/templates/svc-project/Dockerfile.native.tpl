FROM ghcr.io/onecx/docker-quarkus-native:{{dockerNativeVersion}}

COPY --chown=1001 target/*-runner /work/application