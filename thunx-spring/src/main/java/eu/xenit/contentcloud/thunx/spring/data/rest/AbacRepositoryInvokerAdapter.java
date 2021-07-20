package eu.xenit.contentcloud.thunx.spring.data.rest;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import eu.xenit.contentcloud.thunx.spring.data.context.AbacContext;
import eu.xenit.contentcloud.thunx.spring.data.context.EntityContext;
import eu.xenit.contentcloud.thunx.spring.data.context.EntityManagerContext;
import java.lang.reflect.Field;
import java.util.Optional;
import javax.persistence.Id;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.content.commons.utils.BeanUtils;
import org.springframework.content.commons.utils.DomainObjectUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslRepositoryInvokerAdapter;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

public class AbacRepositoryInvokerAdapter extends QuerydslRepositoryInvokerAdapter {

    private QuerydslPredicateExecutor<Object> executor;
    private Predicate predicate;

    private ConversionService conversionService = new DefaultFormattingConversionService();

    public AbacRepositoryInvokerAdapter(RepositoryInvoker delegate, QuerydslPredicateExecutor<Object> executor, Predicate predicate) {
        super(delegate, executor, predicate);
        this.executor = executor;
        this.predicate = predicate;
    }

    @Override
    public <T> Optional<T> invokeFindById(Object id) {

        BooleanBuilder builder = new BooleanBuilder();

        Class<?> subjectType = EntityContext.getCurrentEntityContext().getJavaType();
        PathBuilder entityPath = new PathBuilder(subjectType, toAlias(subjectType));
        BooleanExpression idExpr = idExpr(conversionService.convert(id, Long.class), entityPath);
        Assert.notNull(idExpr, "id expression cannot be null");
        builder.and(idExpr);
        builder.and(predicate);

        return (Optional<T>) executor.findOne(builder.getValue());
    }

//  When saving an entity we first save and then findById that applies the abac policy.  If this find return null we throw a RNFE that rollback
//  the transaction

//  OPA policies are written in terms of the object being saved
//
    @Override
    public <T> T invokeSave(T object) {

        var abacContext = AbacContext.getCurrentAbacContext();
        if (abacContext == null) {
            return super.invokeSave(object);
        }

        PlatformTransactionManager tm = EntityManagerContext.getCurrentEntityContext().getTm();

        TransactionStatus status = null;
        T entityToReturn = null;
        try {

            if (tm != null) {
                status = tm.getTransaction(new DefaultTransactionDefinition());
            }

            T savedEntity = super.invokeSave(object);

            Field idField = DomainObjectUtils.getIdField(object.getClass());
            Assert.notNull(idField, "missing id field");

            BeanWrapper wrapper = new BeanWrapperImpl(savedEntity);
            Object id = wrapper.getPropertyValue(idField.getName());

            Optional<T> fetchedEntity = this.invokeFindById(id);
            if (!fetchedEntity.isPresent()) {
                throw new ResourceNotFoundException(String.format("id: %s", id));
            }

            entityToReturn = fetchedEntity.get();

            if (status != null && status.isCompleted() == false) {
                tm.commit(status);
            }
        } catch (Exception e) {
            if (status != null && status.isCompleted() == false) {
                tm.rollback(status);
            }
            throw e;
        }

        return entityToReturn;
    }

//    No implementation required.  When performing a delete through the REST API the Controller first does a findById call
//    that applies the abac policies
//
//    @Override
//    public void invokeDeleteById(Object id) {
//
//        Disjunction abacContext = ABACContext.getCurrentAbacContext();
//        if (abacContext == null) {
//            super.invokeDeleteById(id);
//        }
//
//        try {
//            Optional<Object> object = this.invokeFindById(conversionService.convert(id, Long.class));
//            object.ifPresent((it) -> {
//                enforceAbacAttributes(object, abacContext);
//                super.invokeDeleteById(conversionService.convert(id, Long.class));
//            });
//        } catch (Throwable t) {
//            int i=0;
//        }
//    }

    private String toAlias(Class<?> subjectType) {

        char c[] = subjectType.getSimpleName().toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private BooleanExpression idExpr(Object id, PathBuilder entityPath) {
        Field idField = BeanUtils.findFieldWithAnnotation(EntityContext.getCurrentEntityContext().getJavaType(), Id.class);
        PathBuilder idPath = entityPath.get(idField.getName(), id.getClass());
        return idPath.eq(Expressions.constant(id));
    }
}
