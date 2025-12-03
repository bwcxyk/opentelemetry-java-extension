FROM busybox

COPY build/libs/opentelemetry-javaagent.jar /opt/otel/
COPY sdk-config.yaml /opt/otel/sdk-config.yaml

RUN chmod -R go+r /opt/otel
