package org.springframework.data.jpa.repository.support;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import eu.xenit.contentcloud.thunx.predicates.solr.NativeQueryUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.function.LongSupplier;

public class PostgresQueryPlanEstimateCountQueryProvider implements CustomCountQueryProvider {

    private EntityManager em;

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Override
    public Query createNativeCountQuery(ThunkExpression<Boolean> expr, Class<?> javaType) {

        String strCountQuery = "SELECT 1 FROM " + getTableName(javaType);

        if (expr != null) {
            String strPredicate = NativeQueryUtils.from(expr);
            if (StringUtils.hasLength(strPredicate)) {
                strCountQuery = strCountQuery + " WHERE " + strPredicate;
            }
        }

        return em.createNativeQuery(String.format("SELECT count_estimate('%s');", strCountQuery));
    }

    @Override
    public LongSupplier longSupplier(Query countQuery) {

        return new LongSupplier() {
            @Override
            public long getAsLong() {
                return Long.parseLong(countQuery.getSingleResult().toString());
            }
        };
    }

    private String getTableName(Class<?> javaType) {

        return javaType.getSimpleName().replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }
}
