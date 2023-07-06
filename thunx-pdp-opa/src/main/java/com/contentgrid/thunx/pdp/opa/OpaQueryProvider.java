package com.contentgrid.thunx.pdp.opa;

import com.contentgrid.thunx.pdp.RequestContext;

@FunctionalInterface
public interface OpaQueryProvider<R extends RequestContext> {

    String createQuery(R requestContext);
}
