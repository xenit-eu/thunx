package com.contentgrid.thunx.spring.data.context;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.encoding.json.JsonThunkExpressionCoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AbacConfiguration {

    @Bean
    public ThunkExpressionDecoder thunkDecoder() {
        return new JsonThunkExpressionCoder();
    }
}
