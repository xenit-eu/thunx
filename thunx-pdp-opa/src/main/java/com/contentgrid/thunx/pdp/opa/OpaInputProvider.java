package com.contentgrid.thunx.pdp.opa;

import java.util.Map;

@FunctionalInterface
public interface OpaInputProvider<A, R> {
    Map<String, Object> createInput(A authenticationContext, R requestContext);
}
