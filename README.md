# OpenTelemetry spans drop extension

This project embeds a simple extension in the OpenTelemetry Java agent that drops spans based on the environment variables OTEL_EXCLUDE_URL_PATHS and OTEL_EXCLUDE_DB_STATEMENT.

## Usage

Add the OTEL_EXCLUDE_URL_PATHS or OTEL_EXCLUDE_DB_STATEMENT environment variables, and set the spans to be dropped by separating multiple values with a comma (,).

### [Java instrumentation](https://opentelemetry.io/docs/instrumentation/java/automatic/)
Simply download the [latest](https://github.com/bwcxyk/opentelemetry-java-extension/releases) version instead of the javaagent, and you are good to go.  


### [Opentelemetry operator](https://github.com/open-telemetry/opentelemetry-operator#use-customized-or-vendor-instrumentation)

```yaml
apiVersion: opentelemetry.io/v1alpha1
kind: Instrumentation
metadata:
  name: my-instrumentation
spec:
  java:
    env:
      # Will drop spans towards health and metrics endpoints
      - name: OTEL_EXCLUDE_URL_PATHS
        value: .*/health,.*/metrics
      - name: OTEL_EXCLUDE_DB_STATEMENT
        value: PING,QUIT
    image: bwcxyk/opentelemetry-javaagent:2.6.0
```

## Current versions
* Extension version => [2.6.0](https://github.com/bwcxyk/opentelemetry-java-extension/releases)
* [OpenTelemetry java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation) => 2.6.0
* [OpenTelemetry SDK](https://github.com/open-telemetry/opentelemetry-java) => 1.40.0

## References :
* [Embedded extension](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/examples/extension/README.md#embed-extensions-in-the-opentelemetry-agent)
* [NewRelic exemples](https://github.com/newrelic/newrelic-opentelemetry-examples)
* [vmaleze extension](https://github.com/vmaleze/opentelemetry-java-ignore-spans)
* [lumigo-io custom](https://github.com/lumigo-io/opentelemetry-java-distro/blob/main/custom)
