package eu.xenit.contentcloud.thunx.spring.security;

import eu.xenit.contentcloud.thunx.pdp.AuthenticationContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

public class AuthenticationContextMapper {

    /**
     * Fallback list of OIDC claim names that can be used as user-info
     *
     * @see <a href="https://www.iana.org/assignments/jwt/jwt.xhtml#claims">IANA JSON Web Token Claims Registry</a>
     */
    private static final List<String> USERINFO_CLAIMS = List.of(
            "sub",
            "email",
            "preferred_username",
            "name",
            "email_verified",
            "locale",
            "zoneinfo");

    public static AuthenticationContext fromAuthentication(@NonNull Authentication authentication) {
        return new DefaultAuthenticationContext(
                authentication.getName(),
                authentication.isAuthenticated(),
                extractAuthDetails(authentication),
                extractUserDetails(authentication));
    }

    private static Map<String, Object> extractAuthDetails(Authentication authentication) {
        var result = new HashMap<String, Object>();

        var principal = authentication.getPrincipal();
        if (principal instanceof ClaimAccessor) {
            result.put("claims", ((ClaimAccessor) principal).getClaims());
        }

        return result;
    }

    private static Map<String, Object> extractUserDetails(Authentication authentication) {
        var principal = authentication.getPrincipal();
        if (principal instanceof OidcUser) {
            var oidcUser = (OidcUser) principal;
            var userInfo = oidcUser.getUserInfo();
            if (userInfo != null) {
                return userInfo.getClaims();
            } else {
                // pulling a set of standard user-profile claims
                // https://www.iana.org/assignments/jwt/jwt.xhtml#claims
                var result = new HashMap<String, Object>();
                USERINFO_CLAIMS.forEach(claim -> {
                    consumeIfPresent(oidcUser.getClaims(), claim, result::put);
                });
                return result;
            }
        }

        if (principal instanceof ClaimAccessor) {
            return ((ClaimAccessor) principal).getClaims();
        }

        // fallback to check authorities on the auth-object
        return authentication.getAuthorities().stream()
                .filter(OAuth2UserAuthority.class::isInstance)
                .map(OAuth2UserAuthority.class::cast)
                .findAny()
                .map(authority -> authority.getAttributes())
                .orElse(Map.of());
    }

    private static void consumeIfPresent(Map<String, Object> map, String key, BiConsumer<String, Object> callback) {
        var result = map.get(key);
        if (result != null) {
            callback.accept(key, result);
        }
    }

    @Value
    static class DefaultAuthenticationContext implements AuthenticationContext {
        String name;
        boolean authenticated;
        Map<String, Object> details;
        Map<String, Object> user;
    }
}
