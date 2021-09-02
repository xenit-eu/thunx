package org.springframework.data.jpa.repository.support;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.function.LongSupplier;

public interface CustomCountQueryProvider {

    void setEntityManager(EntityManager em);

    Query createNativeCountQuery(ThunkExpression<Boolean> expr, Class<?> javaType);

    LongSupplier longSupplier(Query countQuery);
}
