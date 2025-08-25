package com.contentgrid.thunx.api.autoconfigure;

import com.contentgrid.thunx.spring.data.context.AbacConfiguration;
import com.contentgrid.thunx.spring.data.context.AbacContextSupplier;
import com.contentgrid.thunx.spring.data.context.HttpHeaderAbacConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(AbacConfiguration.class)
public class AbacContextAutoConfiguration {

    @ConditionalOnProperty(value = "contentgrid.thunx.abac.source", havingValue = "header", matchIfMissing = true)
    @Import(HttpHeaderAbacConfiguration.class)
    public static class HttpHeaderAbacAutoConfiguration {
    }

    @ConditionalOnProperty(value = "contentgrid.thunx.abac.source", havingValue = "none")
    public static class NoneAbacAutoConfiguration {
        // Only when 'contentgrid.thunx.abac.source' equals 'none', a predicate resolver that does no checking is created

        @Bean
        public AbacContextSupplier noneAbacContextSupplier() {
            return () -> null;
        }
    }
}
