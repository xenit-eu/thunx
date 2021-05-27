package eu.contentcloud.abac.spring.data.rest.webmvc;

import static java.lang.String.format;

import javax.persistence.EntityManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AbacConfiguration {

    @Bean
    public AbacExceptionHandler exceptionHandler() {
        return new AbacExceptionHandler();
    }

    @Bean
    public AbacRequestFilter abacFilter(Repositories repos, EntityManager em, PlatformTransactionManager tm) {
        return new AbacRequestFilter(repos, em, tm);
    }

    @Bean
    public FilterRegistrationBean<AbacRequestFilter> abacFilterRegistration(Repositories repos, EntityManager em, PlatformTransactionManager tm) {
        FilterRegistrationBean<AbacRequestFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(abacFilter(repos, em, tm));
        registrationBean.addUrlPatterns("/accountStates/*");
        registrationBean.addUrlPatterns("/content/*");

        return registrationBean;
    }

}
