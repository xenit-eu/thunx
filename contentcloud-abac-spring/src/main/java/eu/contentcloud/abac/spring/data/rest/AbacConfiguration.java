package eu.contentcloud.abac.spring.data.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.contentcloud.abac.encoding.AbacExpressionDecoder;
import eu.contentcloud.abac.predicates.model.Expression;
import eu.contentcloud.security.abac.predicates.converters.json.ExpressionJsonConverter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import javax.persistence.EntityManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AbacConfiguration {

    @Bean
    public AbacExpressionDecoder thunkDecoder() {
        return data -> {
            try {
                var json = new String(data, StandardCharsets.UTF_8);
                var expression = new ExpressionJsonConverter().decode(json);

                return ((Expression<Boolean>) expression);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    @Bean
    public AbacExceptionHandler exceptionHandler() {
        return new AbacExceptionHandler();
    }

    @Bean
    public AbacRequestFilter abacFilter(AbacExpressionDecoder thunkDecoder, Repositories repos, EntityManager em, PlatformTransactionManager tm) {
        return new AbacRequestFilter(thunkDecoder, repos, em, tm);
    }

    @Bean
    public FilterRegistrationBean<AbacRequestFilter> abacFilterRegistration(AbacRequestFilter filter, Repositories repos, EntityManager em, PlatformTransactionManager tm) {
        FilterRegistrationBean<AbacRequestFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(filter);
//        registrationBean.addUrlPatterns("/accountStates/*");
//        registrationBean.addUrlPatterns("/content/*");

        return registrationBean;
    }

}
