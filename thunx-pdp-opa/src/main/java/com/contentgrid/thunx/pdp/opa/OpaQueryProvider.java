package com.contentgrid.thunx.pdp.opa;

@FunctionalInterface
public interface OpaQueryProvider<R> {

    String createQuery(R requestContext);
}
