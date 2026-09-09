package com.example.cataloguecommon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Overrides the Zipkin span sender. Shared by both catalogue services.
 *
 * <p>{@code spring-boot-zipkin} 4.1.x auto-configures a {@code ZipkinHttpClientSender}
 * built on the JDK {@link java.net.http.HttpClient}. In our container network it
 * fails on every flush with {@code ConnectException} / {@code ClosedChannelException}
 * before connect, even though {@code curl} to the same {@code /api/v2/spans} URL
 * from the same container returns 202. So spans never reach Zipkin.
 *
 * <p>This bean is a {@code BytesMessageSender}, and Boot's sender is
 * {@code @ConditionalOnMissingBean(BytesMessageSender.class)}, so declaring it
 * here replaces the broken one with the plain {@code HttpURLConnection}-based
 * sender — the same one the BFF (Spring Boot 3.5) uses, which exports fine.
 */
@Configuration
public class TracingSenderConfig {

    @Bean
    BytesMessageSender zipkinSender(
            @Value("${management.zipkin.tracing.endpoint:http://localhost:9411/api/v2/spans}") String endpoint) {
        return URLConnectionSender.create(endpoint);
    }
}
