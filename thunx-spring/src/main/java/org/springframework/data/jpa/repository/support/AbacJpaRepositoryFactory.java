package org.springframework.data.jpa.repository.support;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;

import javax.persistence.EntityManager;

import java.io.Serializable;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

public class AbacJpaRepositoryFactory extends JpaRepositoryFactory {

    private final EntityManager em;
    private final CrudMethodMetadataPostProcessor crudMethodMetadataPostProcessor;
    private EntityPathResolver entityPathResolver;

    private CustomCountQueryProvider countQueryProvider = null;

    public AbacJpaRepositoryFactory(EntityManager em) {
        super(em);
        this.em = em;
        this.crudMethodMetadataPostProcessor = new CrudMethodMetadataPostProcessor();
    }

    @Override
    protected RepositoryComposition.RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata) {

        RepositoryComposition.RepositoryFragments fragments = RepositoryComposition.RepositoryFragments.empty();

        boolean isQueryDslRepository = QUERY_DSL_PRESENT
                && QuerydslPredicateExecutor.class.isAssignableFrom(metadata.getRepositoryInterface());

        if (isQueryDslRepository) {

            if (metadata.isReactiveRepository()) {
                throw new InvalidDataAccessApiUsageException(
                        "Cannot combine Querydsl and reactive repository support in a single interface");
            }

            JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType());

            Object querydslFragment = getTargetRepositoryViaReflection(AbacQuerydslJpaPredicateExecutor.class, entityInformation,
                    em, entityPathResolver, crudMethodMetadataPostProcessor.getCrudMethodMetadata(), countQueryProvider);

            fragments = fragments.append(RepositoryFragment.implemented(querydslFragment));
        }

        return fragments;
    }

    @Override
    public void setEntityPathResolver(EntityPathResolver entityPathResolver) {
        super.setEntityPathResolver(entityPathResolver);
        this.entityPathResolver = entityPathResolver;
    }

    public void setCountQueryProvider(CustomCountQueryProvider countQueryProvider) {
        this.countQueryProvider = countQueryProvider;
    }

}
