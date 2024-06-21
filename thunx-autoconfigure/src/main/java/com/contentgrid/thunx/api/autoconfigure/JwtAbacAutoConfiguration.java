package com.contentgrid.thunx.api.autoconfigure;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.spring.security.JwtAbacConfiguration;
import com.contentgrid.thunx.spring.security.AbacJwtGrantedAuthoritiesConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@AutoConfiguration(before = {OAuth2ResourceServerAutoConfiguration.class, SecurityAutoConfiguration.class},
        after = {AbacAutoConfiguration.class})
@ConditionalOnClass({Jwt.class, GrantedAuthority.class, ThunkExpressionDecoder.class, JwtAbacConfiguration.class,
        AbacJwtGrantedAuthoritiesConverter.class, JwtAuthenticationConverter.class})
@ConditionalOnProperty(value = "contentgrid.thunx.abac.source", havingValue = "jwt")
@Import(JwtAbacConfiguration.class)
public class JwtAbacAutoConfiguration {

    @Bean
    @ConditionalOnDefaultWebSecurity
    @ConditionalOnMissingBean
    @ConditionalOnBean(ThunkExpressionDecoder.class)
    public JwtAuthenticationConverter abacJwtAuthenticationConverter(ThunkExpressionDecoder decoder) {
        var grantedAuthoritiesConverter = new AbacJwtGrantedAuthoritiesConverter(decoder);

        // Register the AbacJwtGrantedAuthoritiesConverter in a JwtAuthenticationConverter Bean,
        // so that it will be picked up by spring security
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

}
