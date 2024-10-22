FROM busybox

COPY build/libs/opentelemetry-javaagent.jar /opt/otel/

RUN chmod -R go+r /opt/otel/opentelemetry-javaagent.jar
