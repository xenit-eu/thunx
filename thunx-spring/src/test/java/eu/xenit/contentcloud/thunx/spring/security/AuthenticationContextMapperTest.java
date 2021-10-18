package eu.xenit.contentcloud.thunx.spring.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

class AuthenticationContextMapperTest {

    @Test
    void fromOidcUser() {
        var instant = Instant.now();

        var idToken = OidcIdToken.withTokenValue("dummy")
                .subject("04c2cbec-faad-4dc8-ba6f-edb3d5b902e9")
                .claim("preferred_username", "alice")
                .issuedAt(instant)
                .expiresAt(instant.plus(5, ChronoUnit.MINUTES))
                .authTime(instant)
                .issuer("https://auth.content-cloud.eu/auth/realms/my-org")
                .authorizedParty("content-cloud-gateway")
                .build();
        var userInfo = new OidcUserInfo(Map.of(
                "contentcloud:custom", List.of("blue", "green"),
                "email_verified", false
        ));
        var userRole = new OidcUserAuthority(idToken, userInfo);
        var oidcUser = new DefaultOidcUser(Set.of(userRole), idToken, userInfo, "preferred_username");

        var auth = new TestingAuthenticationToken(oidcUser, null, AuthorityUtils.NO_AUTHORITIES);

        var context = AuthenticationContextMapper.fromAuthentication(auth);

        assertThat(context.getName()).isEqualTo("alice");
        assertThat(context.isAuthenticated()).isEqualTo(true);

        assertThat(context.getUser()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "contentcloud:custom", List.of("blue", "green"),
                "email_verified", false
        ));

        assertThat(context.getDetails())
                .hasEntrySatisfying("claims", claims -> assertThat(claims)
                        .asInstanceOf(InstanceOfAssertFactories.MAP)
                        .containsEntry("sub", "04c2cbec-faad-4dc8-ba6f-edb3d5b902e9")
                        .containsEntry("iat", instant)
                        .containsEntry("exp", instant.plus(300, ChronoUnit.SECONDS))
                        .containsEntry("iss", "https://auth.content-cloud.eu/auth/realms/my-org")
                        .containsEntry("azp", "content-cloud-gateway"));
    }

    @Test
    void fromJwtAccessToken() {
        var instant = Instant.now();

        var jwtToken = new ClaimAccessor() {

            @Override
            public Map<String, Object> getClaims() {
                return Map.of("sub", "04c2cbec-faad-4dc8-ba6f-edb3d5b902e9",
                        "iat", instant,
                        "preferred_username", "alice",
                        "contentcloud:custom", List.of("blue", "green"),
                        "email_verified", false
                );

            }
        };
        var auth = new TestingAuthenticationToken(jwtToken, null, AuthorityUtils.NO_AUTHORITIES);

        var context = AuthenticationContextMapper.fromAuthentication(auth);

        assertThat(context.isAuthenticated()).isEqualTo(true);
        assertThat(context.getUser()).containsAllEntriesOf(Map.of(
                "iat", instant,
                "preferred_username", "alice",
                "contentcloud:custom", List.of("blue", "green"),
                "email_verified", false
        ));

    }
}