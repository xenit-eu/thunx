package eu.xenit.contentcloud.thunx.spring.data.rest;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import eu.xenit.contentcloud.thunx.spring.data.context.AbacContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
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
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;

public class AbacRepositoryInvokerAdapter extends QuerydslRepositoryInvokerAdapter {

    private QuerydslPredicateExecutor<Object> executor;
    private PlatformTransactionManager transactionManager;
    private Class<?> domainType;
    private Predicate predicate;

    private ConversionService conversionService = new DefaultFormattingConversionService();

    public AbacRepositoryInvokerAdapter(
            RepositoryInvoker delegate,
            QuerydslPredicateExecutor<Object> executor,
            PlatformTransactionManager transactionManager,
            Class<?> domainType,
            Predicate predicate) {
        super(delegate, executor, predicate);
        this.executor = executor;
        this.transactionManager = transactionManager;
        this.domainType = domainType;
        this.predicate = predicate;
    }

    @Override
    public <T> Optional<T> invokeFindById(Object id) {

        BooleanBuilder builder = new BooleanBuilder();

        PathBuilder entityPath = new PathBuilder(domainType, toAlias(domainType));
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

        TransactionStatus status = null;
        T entityToReturn = null;
        try {

            if (transactionManager != null) {
                status = transactionManager.getTransaction(new DefaultTransactionDefinition());
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
                transactionManager.commit(status);
            }
        } catch (Exception e) {
            if (status != null && status.isCompleted() == false) {
                transactionManager.rollback(status);
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
        Field idField = DomainObjectUtils.getIdField(domainType);
        PathBuilder idPath = entityPath.get(idField.getName(), id.getClass());
        return idPath.eq(Expressions.constant(id));
    }


    static class DomainObjectUtils {

        private static final boolean JAVAX_PERSISTENCE_PRESENT = ClassUtils.isPresent(
                "javax.persistence.Id", DomainObjectUtils.class.getClassLoader());

        static final Field getIdField(Class<?> domainClass) {

            Optional<Field> javaxPersistenceId = JAVAX_PERSISTENCE_PRESENT
                    ? DomainObjectUtils.findFieldWithAnnotation(domainClass, javax.persistence.Id.class)
                    : Optional.empty();

            return javaxPersistenceId
                    .or(() -> DomainObjectUtils.findFieldWithAnnotation(domainClass, org.springframework.data.annotation.Id.class))
                    .orElse(null);
        }

        private static Optional<Field> findFieldWithAnnotation(Class<?> domainObjClass,
                                                                 Class<? extends Annotation> annotationClass)
                throws SecurityException, BeansException {

            // First look for the annotation on the accessor methods
            BeanWrapper wrapper = new BeanWrapperImpl(domainObjClass);
            return Stream.of(wrapper.getPropertyDescriptors())
                    .map(descriptor -> findFieldByName(domainObjClass, descriptor.getName()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(field -> field.isAnnotationPresent(annotationClass))
                    .findFirst()

            // Otherwise look for the annotation on the fields directly
                    .or(() -> allFields(domainObjClass)
                            .filter(field -> field.isAnnotationPresent(annotationClass))
                            .findFirst());


        }

        private static Optional<Field> findFieldByName(Class<?> type, String fieldName) {
            return allFields(type)
                    .filter(field -> field.getName().equals(fieldName))
                    .findFirst();
        }

        private static Stream<Field> allFields(Class<?> type) {
            return Stream.concat(
                    Stream.of(type.getDeclaredFields()),
                    type.getSuperclass() != null ? allFields(type.getSuperclass()) : Stream.empty());
        }
    }
}
