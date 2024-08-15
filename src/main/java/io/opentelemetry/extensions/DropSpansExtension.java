package io.opentelemetry.extensions;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSamplerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;

/**
 * Note this class is wired into SPI via
 * {@code resources/META-INF/services/io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider}
 */
public class DropSpansExtension implements AutoConfigurationCustomizerProvider {

  private static final AttributeKey<String> URL_PATH = stringKey("url.path");

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    // Set the sampler to be the default parentbased_always_on, but drop calls listed in the env variable
    String dropSpansEnv = System.getenv("OTEL_EXCLUDE_URL_PATHS");
    if (dropSpansEnv != null) {
      // 使用 RuleBasedRoutingSamplerBuilder 直接声明构建器
      RuleBasedRoutingSamplerBuilder dropSpanBuilder = RuleBasedRoutingSampler.builder(
          SpanKind.SERVER,
          Sampler.parentBased(Sampler.alwaysOn())
      );

      for (String span : dropSpansEnv.split(",")) {
        dropSpanBuilder.drop(URL_PATH, span);
      }

      autoConfiguration.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) ->
          sdkTracerProviderBuilder.setSampler(Sampler.parentBased(dropSpanBuilder.build())));
    }
  }
}
