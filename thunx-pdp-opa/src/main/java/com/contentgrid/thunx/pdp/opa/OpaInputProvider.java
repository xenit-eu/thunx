package com.contentgrid.thunx.pdp.opa;

import com.contentgrid.thunx.pdp.AuthenticationContext;
import com.contentgrid.thunx.pdp.RequestContext;
import java.util.Map;

@FunctionalInterface
public interface OpaInputProvider<A extends AuthenticationContext, R extends RequestContext> {
    Map<String, Object> createInput(A authenticationContext, R requestContext);
}
