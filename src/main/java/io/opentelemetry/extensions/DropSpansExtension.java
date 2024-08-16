package io.opentelemetry.extensions;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSamplerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.util.logging.Logger;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

/**
 * DropSpansExtension类用于配置OpenTelemetry，以自动过滤掉符合特定条件的Span。
 * 它实现AutoConfigurationCustomizerProvider接口，允许在SDK初始化时自定义配置。
 */
public class DropSpansExtension implements AutoConfigurationCustomizerProvider {
  // 日志记录器用于记录配置过程中的信息
  private static final Logger LOGGER = Logger.getLogger(DropSpansExtension.class.getName());
  // 定义了一系列属性键，用于匹配Span的属性，以便决定是否丢弃该Span
  private static final AttributeKey<String> URL_PATH = stringKey("url.path");
  private static final AttributeKey<String> DB_STATE = stringKey("db.statement");
  private static final AttributeKey<String> HTTP_TARGET = stringKey("http.target");
  private static final AttributeKey<String> HTTP_URL = stringKey("http.url");
  private static final AttributeKey<String> URL_FULL = stringKey("url.full");
  // 定义了环境变量模式，用于匹配需要丢弃的Span的HTTP路径和数据库语句
  private static final String dropSpansEnv = ".*/actuator/health";
  private static final String dropDbEnv = "PING,QUIT,AUTH*,select .* from dual";

  /**
   * 自定义配置方法，添加了自定义的Sampler定制器，用于分别定制服务器和客户端的Span过滤逻辑。
   * 
   * @param autoConfiguration 自动配置定制器，用于添加自定义配置
   */
  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
            .addSamplerCustomizer(DropSpansExtension::customizeServerSpans)
            .addSamplerCustomizer(DropSpansExtension::customizeClientSpans);
  }

  /**
   * 定制服务器端Span的过滤逻辑。
   * 通过配置RuleBasedRoutingSampler，动态决定哪些Span应当被丢弃。
   * 
   * @param sampler 初始的Sampler对象，将被包装或定制
   * @param configProperties 配置属性，用于定制过滤逻辑
   * @return 定制后的Sampler对象
   */
  private static Sampler customizeServerSpans(Sampler sampler, ConfigProperties configProperties) {
    RuleBasedRoutingSamplerBuilder dropSpanBuilder =
            RuleBasedRoutingSampler.builder(SpanKind.SERVER, sampler);

    // 遍历并匹配预定义的HTTP路径，配置对应的Span丢弃规则
    for (String span : dropSpansEnv.split(",")) {
      dropSpanBuilder.drop(URL_PATH, span);
      dropSpanBuilder.drop(HTTP_TARGET, span);
    }
    // 输出配置的环境变量模式，便于调试和日志记录
    System.out.println("dropSpansEnv:"+dropSpansEnv);
    return dropSpanBuilder.build();
  }

  /**
   * 定制客户端Span的过滤逻辑。
   * 类似于customizeServerSpans方法，但专注于客户端Span。
   * 
   * @param sampler 初始的Sampler对象，将被包装或定制
   * @param configProperties 配置属性，用于定制过滤逻辑
   * @return 定制后的Sampler对象
   */
  private static Sampler customizeClientSpans(Sampler sampler, ConfigProperties configProperties) {
    RuleBasedRoutingSamplerBuilder dropSpanBuilder =
            RuleBasedRoutingSampler.builder(SpanKind.CLIENT, sampler);

    // 遍历并匹配预定义的URL和数据库语句，配置对应的Span丢弃规则
    for (String span : dropDbEnv.split(",")) {
      dropSpanBuilder.drop(HTTP_URL, span);
      dropSpanBuilder.drop(URL_FULL, span);
      dropSpanBuilder.drop(DB_STATE, span);
    }

    // 输出配置的环境变量模式，便于调试和日志记录
    System.out.println("dropDbEnv:"+dropDbEnv);
    return dropSpanBuilder.build();
  }
}
