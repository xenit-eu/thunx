package com.contentgrid.thunx.api.autoconfigure;

import com.contentgrid.thunx.spring.data.EnableAbac;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@AutoConfiguration
@ConditionalOnClass(RepositoryRestResource.class)
public class AbacAutoConfiguration {

    @EnableAbac
    public static class EnableAbacAutoConfiguration {
    }
}
