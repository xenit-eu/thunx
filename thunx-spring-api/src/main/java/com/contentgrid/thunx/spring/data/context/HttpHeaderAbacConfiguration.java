package com.contentgrid.thunx.spring.data.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @deprecated Use {@link com.contentgrid.thunx.spring.security.HttpHeaderAbacConfiguration} instead
 */
@Configuration(proxyBeanMethods = false)
@Deprecated(since = "0.14.1")
@Import(com.contentgrid.thunx.spring.security.HttpHeaderAbacConfiguration.class)
public class HttpHeaderAbacConfiguration {

    @Bean
    public AbacRequestFilter deprecatedAbacFilter(com.contentgrid.thunx.spring.security.AbacRequestFilter filter) {
        return new AbacRequestFilter(filter);
    }
}
