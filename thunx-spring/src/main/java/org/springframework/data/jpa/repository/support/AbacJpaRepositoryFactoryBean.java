package org.springframework.data.jpa.repository.support;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;

public class AbacJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

    private EntityPathResolver entityPathResolver;
    private JpaQueryMethodFactory queryMethodFactory;
    private EscapeCharacter escapeCharacter;

    private CustomCountQueryProvider countQueryProvider = new PostgresQueryPlanEstimateCountQueryProvider();

    public AbacJpaRepositoryFactoryBean(Class repositoryInterface) {
        super(repositoryInterface);
    }

    @Autowired(required=false)
    public void setCountQueryProvider(CustomCountQueryProvider queryCountProvider) {
        this.countQueryProvider = queryCountProvider;
    }

    @Autowired
    @Override
    public void setEntityPathResolver(ObjectProvider<EntityPathResolver> resolver) {
        this.entityPathResolver = resolver.getIfAvailable(() -> SimpleEntityPathResolver.INSTANCE);
    }

    @Autowired
    @Override
    public void setQueryMethodFactory(@Nullable JpaQueryMethodFactory factory) {
        super.setQueryMethodFactory(factory);
        if (factory != null) {
            this.queryMethodFactory = factory;
        }
    }

    public void setEscapeCharacter(char escapeCharacter) {
        super.setEscapeCharacter(escapeCharacter);
        this.escapeCharacter = EscapeCharacter.of(escapeCharacter);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {

        AbacJpaRepositoryFactory jpaRepositoryFactory = new AbacJpaRepositoryFactory(entityManager);
        jpaRepositoryFactory.setEntityPathResolver(entityPathResolver);
        jpaRepositoryFactory.setEscapeCharacter(escapeCharacter);

        countQueryProvider.setEntityManager(entityManager);
        jpaRepositoryFactory.setCountQueryProvider(countQueryProvider);

        if (queryMethodFactory != null) {
            jpaRepositoryFactory.setQueryMethodFactory(queryMethodFactory);
        }

        return jpaRepositoryFactory;
    }
}
