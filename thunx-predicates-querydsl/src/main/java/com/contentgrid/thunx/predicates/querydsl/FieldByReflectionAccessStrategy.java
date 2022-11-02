package com.contentgrid.thunx.predicates.querydsl;

import com.querydsl.core.util.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;

public class FieldByReflectionAccessStrategy implements PropertyAccessStrategy {

    @Override
    public Optional<PropertyAccess> getProperty(Class<?> type, String pathElement) {

        return Optional.ofNullable(ReflectionUtils.getFieldOrNull(type, pathElement))
                .map(FieldPropertyAccess::new);
    }

    @Value
    private static class FieldPropertyAccess implements PropertyAccess {

        @NonNull
        Field field;

        @Override
        public Class<?> getType() {
            return this.field.getType();
        }

        @Override
        public Annotation[] getAnnotations() {
            return this.field.getAnnotations();
        }

        @Override
        public Annotation getAnnotation(Class<? extends Annotation> annotationClass) {
            return this.field.getAnnotation(annotationClass);
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return this.field.isAnnotationPresent(annotationClass);
        }
    }
}
