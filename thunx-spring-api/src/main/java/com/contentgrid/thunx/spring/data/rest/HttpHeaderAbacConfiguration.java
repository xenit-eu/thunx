package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.spring.data.context.AbacContext;
import com.contentgrid.thunx.spring.data.context.AbacContextSupplier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpHeaderAbacConfiguration {

    @Bean
    public AbacRequestFilter abacFilter(ThunkExpressionDecoder thunkDecoder) {
        return new AbacRequestFilter(thunkDecoder);
    }

    @Bean
    public FilterRegistrationBean<AbacRequestFilter> abacFilterRegistration(AbacRequestFilter filter) {
        FilterRegistrationBean<AbacRequestFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(filter);

        return registrationBean;
    }

    @Bean
    public AbacContextSupplier headerAbacContextSupplier() {
        return AbacContext::getCurrentAbacContext;
    }

}
