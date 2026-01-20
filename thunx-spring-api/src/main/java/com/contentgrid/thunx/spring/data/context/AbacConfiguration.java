package com.contentgrid.thunx.spring.data.context;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @deprecated Use {@link com.contentgrid.thunx.spring.security.AbacConfiguration} instead
 */
@Configuration(proxyBeanMethods = false)
@Deprecated(since = "0.15.0")
@Import(com.contentgrid.thunx.spring.security.AbacConfiguration.class)
public class AbacConfiguration {
}
