package eu.xenit.contentcloud.thunx.spring.autoconfigure;

import eu.xenit.contentcloud.thunx.encoding.ThunkExpressionDecoder;
import eu.xenit.contentcloud.thunx.spring.data.rest.AbacExceptionHandler;
import eu.xenit.contentcloud.thunx.spring.data.rest.AbacRequestFilter;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbacAutoConfigurationTest {

    @Test
    public void shouldEnableAbac() {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(TestContext.class);
        context.refresh();

        assertThat(context.getBean(ThunkExpressionDecoder.class), is(not(nullValue())));
        assertThat(context.getBean(AbacExceptionHandler.class), is(not(nullValue())));
        assertThat(context.getBean(AbacRequestFilter.class), is(not(nullValue())));
        assertThat(context.getBean("abacFilterRegistration"), is(not(nullValue())));
        assertThat(context.getBean("interceptRepositoryRestMvcConfiguration"), is(not(nullValue())));
        assertThat(context.getBean("ensureQueryDslPredication"), is(not(nullValue())));

        context.close();
    }

    @Configuration
    @EnableAutoConfiguration(exclude={org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class})
    public static class TestContext {

        @Bean
        public Repositories repositories(ApplicationContext context) {
            return new Repositories(context);
        }
    }
}
