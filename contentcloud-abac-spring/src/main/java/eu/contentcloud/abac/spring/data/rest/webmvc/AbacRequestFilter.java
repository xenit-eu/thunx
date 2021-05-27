package eu.contentcloud.abac.spring.data.rest.webmvc;

import static java.lang.String.format;

import eu.contentcloud.abac.predicates.model.Expression;
import eu.contentcloud.abac.spring.data.context.AbacContext;
import eu.contentcloud.abac.spring.data.context.EntityContext;
import eu.contentcloud.abac.spring.data.context.EntityManagerContext;
import java.io.IOException;
import java.util.Base64;
import javax.persistence.EntityManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.util.UrlPathHelper;

public class AbacRequestFilter implements Filter {

    private final Repositories repos;
    private final EntityManager em;
    private final PlatformTransactionManager tm;

    public AbacRequestFilter(Repositories repos, EntityManager em, PlatformTransactionManager tm) {
        this.repos = repos;
        this.em = em;
        this.tm = tm;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String path = new UrlPathHelper().getLookupPathForRequest(request);
        String[] pathElements = path.split("/");
        RepositoryInformation ri = RepositoryUtils.findRepositoryInformation(repos, pathElements[1]);
        if (ri == null) {
            ri = RepositoryUtils.findRepositoryInformation(repos, pathElements[2]);
        }
        if (ri == null) {
            throw new IllegalStateException(format("Unable to resolve entity class: %s", path));
        }
        Class<?> entityClass = ri.getDomainType();

        EntityInformation ei = JpaEntityInformationSupport.getEntityInformation(entityClass, em);
        if (entityClass != null) {
            EntityContext.setCurrentEntityContext(ei);
        }

        EntityManagerContext.setCurrentEntityContext(em, tm);

        // Emad
        String abacContext = request.getHeader("X-ABAC-Context");
        if (abacContext != null) {
            byte[] abacContextBytes = Base64.getDecoder().decode(abacContext);
            // which (version of?) decoder should we use ? -> get that info from JWT or other header ?
            Expression<Boolean> abacExpression = null;
//                PDisjunction pDisjunction = PDisjunction.newBuilder().mergeFrom(abacContextProtobytes).build();
//                Disjunction disjunction = ProtobufUtils.to(pDisjunction, "");
            AbacContext.setCurrentAbacContext(abacExpression);
        }

        filterChain.doFilter(servletRequest, servletResponse);

        AbacContext.clear();
        EntityContext.clear();
    }
}
