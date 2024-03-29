package com.contentgrid.thunx.spring.data;

import com.contentgrid.thunx.spring.data.rest.AbacConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AbacConfiguration.class)
public @interface EnableAbac {
}
