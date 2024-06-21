package com.contentgrid.thunx.security;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@RequiredArgsConstructor
public class AbacJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @NonNull
    private final ThunkExpressionDecoder coder;

    @Getter
    @Setter
    @NonNull
    private String claim = "x-abac-context";

    @Getter
    @Setter
    @NonNull
    private String authority = "x-abac-context";

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        var context = (String)source.getClaim(claim);
        if (context == null) {
            return List.of();
        }
        var expression = coder.decode(context.getBytes(StandardCharsets.UTF_8));
        return List.of(new AbacContextAuthority(authority, expression));
    }
}
