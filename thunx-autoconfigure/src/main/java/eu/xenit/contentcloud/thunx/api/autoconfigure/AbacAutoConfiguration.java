package eu.xenit.contentcloud.thunx.api.autoconfigure;

import eu.xenit.contentcloud.thunx.spring.data.EnableAbac;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@Configuration
@ConditionalOnClass(RepositoryRestResource.class)
public class AbacAutoConfiguration {

    @Configuration
    @EnableAbac
    public static class EnableAbacAutoConfiguration {
    }
}
