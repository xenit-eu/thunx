package eu.xenit.contentcloud.thunx.api.autoconfigure;

import eu.xenit.contentcloud.thunx.spring.data.EnableAbac;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnClass({ MongoClient.class, MongoContentStoresRegistrar.class })
//@ConditionalOnMissingBean(MongoStoreFactoryBean.class)
//@Import({ MongoContentAutoConfigureRegistrar.class, MongoStoreConfiguration.class })
public class AbacAutoConfiguration {

    @Configuration
    @EnableAbac
    public static class EnableAbacAutoConfiguration {

    }

}
