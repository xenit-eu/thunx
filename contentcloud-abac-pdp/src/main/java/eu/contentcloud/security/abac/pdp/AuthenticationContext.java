package eu.contentcloud.security.abac.pdp;

import java.util.Map;

public interface AuthenticationContext {

    String getName();
    boolean isAuthenticated();

    /**
     * Provides additional details about the authenticated session. These might be an IP
     * address, OIDC Claims, ...
     *
     * @return additional details about the authentication request
     */
    Map<String, Object> getDetails();

    /**
     * Provides user profile attributes. This might be email, locale, custom user attributes
     * @return
     */
    Map<String, Object> getUser();

}
