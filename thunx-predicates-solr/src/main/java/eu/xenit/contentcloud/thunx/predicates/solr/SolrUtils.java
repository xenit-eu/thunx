package eu.xenit.contentcloud.thunx.predicates.solr;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;

public class SolrUtils {

    public static String from(ThunkExpression<Boolean> thunk/*, PathBuilder<?> entityPath*/) {

        var solrExpr = thunk.accept(new SolrConverter());
        return (String) solrExpr;
    }

}

