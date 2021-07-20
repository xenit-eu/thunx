package eu.xenit.contentcloud.thunx.spring.data.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.xenit.contentcloud.thunx.encoding.ThunkExpressionDecoder;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import eu.xenit.contentcloud.thunx.encoding.json.ExpressionJsonConverter;
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
    public ThunkExpressionDecoder thunkDecoder() {
        return data -> {
            try {
                var json = new String(data, StandardCharsets.UTF_8);
                var expression = new ExpressionJsonConverter().decode(json);

                return ((ThunkExpression<Boolean>) expression);
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
    public AbacRequestFilter abacFilter(ThunkExpressionDecoder thunkDecoder, Repositories repos, EntityManager em, PlatformTransactionManager tm) {
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
