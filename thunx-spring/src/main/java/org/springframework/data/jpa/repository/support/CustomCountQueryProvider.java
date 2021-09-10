package org.springframework.data.jpa.repository.support;

import com.querydsl.core.types.Predicate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.function.LongSupplier;

public interface CustomCountQueryProvider {

    void setEntityManager(EntityManager em);

    Query createNativeCountQuery(Predicate predicate, Class<?> javaType);

    LongSupplier longSupplier(Query countQuery);
}
