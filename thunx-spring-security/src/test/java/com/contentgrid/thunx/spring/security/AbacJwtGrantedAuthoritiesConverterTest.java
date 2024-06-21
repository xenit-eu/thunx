package com.contentgrid.thunx.spring.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentgrid.thunx.encoding.ThunkExpressionEncoder;
import com.contentgrid.thunx.encoding.json.JsonThunkExpressionCoder;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AbacJwtGrantedAuthoritiesConverterTest {

    private static final ThunkExpression<Boolean> EXPRESSION = Comparison.areEqual(
            SymbolicReference.of("entity", path -> path.string("counterparty").string("vat")),
            Scalar.of("BE0887582365"));

    private final ThunkExpressionEncoder encoder = new JsonThunkExpressionCoder();
    private AbacJwtGrantedAuthoritiesConverter converter;

    @BeforeEach
    void setup() {
        converter = new AbacJwtGrantedAuthoritiesConverter(new JsonThunkExpressionCoder());
    }

    @Test
    void convertDefault() {
        var encoded = new String(encoder.encode(EXPRESSION));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(converter.getAuthority(), encoded)
                .build();

        assertThat(converter.convert(jwt)).singleElement().isInstanceOfSatisfying(AbacContextAuthority.class, authority -> {
            assertThat(authority.getAuthority()).isEqualTo(converter.getClaim());
            assertThat(authority.getExpression()).isEqualTo(EXPRESSION);
        });
    }

    @Test
    void convertWithCustomAuthorityAndClaim() {
        var authority = "custom-abac-authority";
        converter.setAuthority(authority);

        var claim = "custom-abac-claim";
        converter.setClaim(claim);

        var encoded = new String(encoder.encode(EXPRESSION));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(claim, encoded)
                .build();

        assertThat(converter.convert(jwt)).singleElement().isInstanceOfSatisfying(AbacContextAuthority.class, grantedAuthority -> {
            assertThat(grantedAuthority.getAuthority()).isEqualTo(authority);
            assertThat(grantedAuthority.getExpression()).isEqualTo(EXPRESSION);
        });
    }

    @Test
    void convertInvalidClaim() {
        var encoded = new String(encoder.encode(EXPRESSION));

        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("invalid-claim", encoded)
                .build();

        assertThat(converter.convert(jwt)).isEmpty();
    }

}