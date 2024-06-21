package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
public class AbacContextAuthority implements GrantedAuthority {

    private final String authority;
    private final ThunkExpression<Boolean> expression;

}
