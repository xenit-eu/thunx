package eu.contentcloud.security.abac.pdp.opa;

import eu.contentcloud.security.abac.pdp.RequestContext;

@FunctionalInterface
public interface OpaQueryProvider {

    String createQuery(RequestContext requestContext);
}
