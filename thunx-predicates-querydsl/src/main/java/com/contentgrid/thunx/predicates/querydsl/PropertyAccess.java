package com.contentgrid.thunx.predicates.querydsl;

import java.lang.annotation.Annotation;

public interface PropertyAccess {

    Class<?> getType();

    Annotation[] getAnnotations();
    Annotation getAnnotation(Class<? extends Annotation> annotationClass);

    default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

}
