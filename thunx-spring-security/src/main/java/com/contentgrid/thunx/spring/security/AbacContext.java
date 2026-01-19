package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.predicates.model.ThunkExpression;

class AbacContext {

    private static ThreadLocal<ThunkExpression<Boolean>> currentAbacContext = new InheritableThreadLocal<ThunkExpression<Boolean>>();

    public static ThunkExpression<Boolean> getCurrentAbacContext() {
        return currentAbacContext.get();
    }

    public static void setCurrentAbacContext(ThunkExpression<Boolean> expression) {
        currentAbacContext.set(expression);
    }

    public static void clear() {
        currentAbacContext.set(null);
    }
}
