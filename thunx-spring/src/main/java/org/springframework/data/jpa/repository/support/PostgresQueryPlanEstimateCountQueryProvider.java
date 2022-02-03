package org.springframework.data.jpa.repository.support;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.hibernate.HibernateQuery;
import org.hibernate.Session;
import org.hibernate.query.internal.QueryImpl;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

public class PostgresQueryPlanEstimateCountQueryProvider implements CustomCountQueryProvider {

    private EntityManager em;

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Override
    public Query createNativeCountQuery(Predicate predicate, Class<?> subjectType) {

        PathBuilder<?> entityPath = new PathBuilder(subjectType, toAlias(subjectType));

        String strSql = generateNativeQueryFromPredicate(predicate, subjectType, entityPath);

        String strWhereClause = extractWhereClause(strSql);
        strWhereClause = insertParameters(strWhereClause, predicate.accept(new ConstantExtractor(), null));

        String strCountQuery = String.format("SELECT 1 FROM %s WHERE %s", extractTableName(strSql), strWhereClause);

        return em.createNativeQuery(String.format("SELECT count_estimate('%s');", strCountQuery));
    }

    private String extractTableName(String sql) {

        int fromIdx = sql.indexOf("from");
        int whereIdx = sql.indexOf("where");
        String strTableName = sql.substring(fromIdx + 4, whereIdx);
        if (StringUtils.hasText(strTableName)) {
            strTableName = strTableName.trim();
        }
        return strTableName;
    }

    private String extractWhereClause(String sql) {

        int whereIdx = sql.indexOf("where");
        String strWhereClause = sql.substring(whereIdx + 5);
        if (StringUtils.hasText(strWhereClause)) {
            strWhereClause = strWhereClause.trim();
        }
        return strWhereClause;
    }

    private String generateNativeQueryFromPredicate(Predicate predicate, Class<?> subjectType, PathBuilder<?> entityPath) {
        HibernateQuery jpql = new HibernateQuery();
        jpql.from(entityPath);
        jpql.where(predicate);

        Session session = em.unwrap(Session.class);
        Query nq = session.createQuery(jpql.toString(), subjectType);
        return ((QueryImpl)nq).getQueryPlan().getTranslators()[0].getSQLString();
    }

    private String insertParameters(String strWhereClause, List<Constant<?>> accept) {

        String strResult = strWhereClause;
        int i=0;

        while (strResult.indexOf("?") != -1) {
            strResult = strResult.replaceFirst("\\?", accept.get(i).toString());
            i++;
        }

        return strResult;
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

    private String toAlias(Class<?> subjectType) {

        char c[] = subjectType.getSimpleName().toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    class ConstantExtractor implements Visitor<List<Constant<?>>, Void>
    {

        private List<Constant<?>> constants = new ArrayList<>();

        public ConstantExtractor() {
        }

        @Override
        public List<Constant<?>> visit(Constant<?> expr, Void context) {
            return Collections.singletonList(expr);
        }

        @Override
        public List<Constant<?>> visit(FactoryExpression<?> expr, Void context) {
            return visit(expr.getArgs());
        }

        @Override
        public List<Constant<?>> visit(Operation<?> expr, Void context) {
            return visit(expr.getArgs());
        }

        @Override
        public List<Constant<?>> visit(ParamExpression<?> expr, Void context) {
            return null;
        }

        @Override
        public List<Constant<?>> visit(Path<?> expr, Void context) {
            return null;
        }

        @Override
        public List<Constant<?>> visit(SubQueryExpression<?> expr, Void context) {
            return null;
        }

        @Override
        public List<Constant<?>> visit(TemplateExpression<?> expr, Void context) {
            return visit(expr.getArgs());
        }

        private List<Constant<?>> visit(List<?> exprs) {
            for (Object e : exprs)
            {
                if (e instanceof Expression)
                {
                    List<Constant<?>> constants = ((Expression<?>) e).accept(this, null);
                    if (constants != null)
                    {
                        for (Constant candidate : constants) {

                            // only add if it is not already there
                            boolean add = true;
                            for (Constant existing : this.constants) {
                                if (existing == candidate) {
                                    add = false;
                                    break;
                                }
                            }

                            if (add) {
                                this.constants.add(candidate);
                            }
                        }
                    }
                }
            }
            return constants;
        }
    }
}
