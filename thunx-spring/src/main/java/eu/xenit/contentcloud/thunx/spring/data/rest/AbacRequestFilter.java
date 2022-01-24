package eu.xenit.contentcloud.thunx.spring.data.rest;

import eu.xenit.contentcloud.thunx.encoding.ThunkExpressionDecoder;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import eu.xenit.contentcloud.thunx.spring.data.context.AbacContext;
import eu.xenit.contentcloud.thunx.spring.data.context.EntityContext;
import eu.xenit.contentcloud.thunx.spring.data.context.EntityManagerContext;
import java.io.IOException;
import java.util.Base64;
import javax.persistence.EntityManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.util.UrlPathHelper;

@Slf4j
public class AbacRequestFilter implements Filter {

    private final ThunkExpressionDecoder thunkDecoder;
    private final ResourceMappings resourceMappings;

    private final EntityManager em;
    private final PlatformTransactionManager tm;

    public AbacRequestFilter(ThunkExpressionDecoder thunkDecoder,
            ResourceMappings resourceMappings,
            EntityManager em, PlatformTransactionManager tm) {
        this.thunkDecoder = thunkDecoder;
        this.resourceMappings = resourceMappings;
        this.em = em;
        this.tm = tm;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String lookupPath = new UrlPathHelper().getLookupPathForRequest(request);

        Class<?> domainType = null;
        for (ResourceMetadata resourceMapping : resourceMappings) {
            if(lookupPath.startsWith(resourceMapping.getPath().toString())) {
                domainType = resourceMapping.getDomainType();
                if(domainType != null) {
                    log.debug("Path {} is determined to map to resource type {} (via path {})", lookupPath, domainType, resourceMapping.getPath());
                    break;
                }
            }
        }


        if (domainType != null) {
            Class<?> entityClass = domainType;

            EntityInformation ei = JpaEntityInformationSupport.getEntityInformation(entityClass, em);
            EntityContext.setCurrentEntityContext(ei);

            EntityManagerContext.setCurrentEntityContext(em, tm);

            String abacContext = request.getHeader("X-ABAC-Context");
            if (abacContext != null) {
                byte[] abacContextBytes = Base64.getDecoder().decode(abacContext);
                // which (version of?) decoder should we use ? -> get that info from JWT or other header ?
                ThunkExpression<Boolean> abacExpression = this.thunkDecoder.decoder(abacContextBytes);
                log.debug("ABAC expression for this context is {}", abacExpression);
                AbacContext.setCurrentAbacContext(abacExpression);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);

        if(domainType != null) {
            AbacContext.clear();
            EntityContext.clear();
        }
    }
}
