package com.contentgrid.thunx.predicates.querydsl;

import java.util.Optional;

/**
 * Strategy interface for introspecting and navigating through the object model.
 *
 * A property can be a field or a relation.
 */
public interface PropertyAccessStrategy {

    Optional<PropertyAccess> getProperty(Class<?> type, String pathElement);
}
