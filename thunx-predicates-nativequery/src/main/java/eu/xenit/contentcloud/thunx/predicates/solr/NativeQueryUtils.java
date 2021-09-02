package eu.xenit.contentcloud.thunx.predicates.solr;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;

public class NativeQueryUtils {

    public static String from(ThunkExpression<Boolean> thunk) {

        var nativeQueryExpr = thunk.accept(new NativeQueryConverter());
        return (String) nativeQueryExpr;
    }

}

