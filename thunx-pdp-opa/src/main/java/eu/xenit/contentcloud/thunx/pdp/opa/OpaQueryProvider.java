package eu.xenit.contentcloud.thunx.pdp.opa;

import eu.xenit.contentcloud.thunx.pdp.RequestContext;

@FunctionalInterface
public interface OpaQueryProvider {

    String createQuery(RequestContext requestContext);
}
