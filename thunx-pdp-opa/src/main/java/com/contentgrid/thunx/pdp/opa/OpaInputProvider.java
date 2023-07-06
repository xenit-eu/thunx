package com.contentgrid.thunx.pdp.opa;

import com.contentgrid.thunx.pdp.AuthenticationContext;
import com.contentgrid.thunx.pdp.RequestContext;
import java.util.Map;

@FunctionalInterface
public interface OpaInputProvider {
    Map<String, Object> createInput(AuthenticationContext authenticationContext, RequestContext requestContext);
}
