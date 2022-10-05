package com.contentgrid.thunx.pdp;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface RequestContext {

    /**
     * Return the HTTP method of the request.
     * @return the HTTP method as a plain String
     */
    String getHttpMethod();

    /**
     * Return the URI of the request, including a query string if any.
     * @return the URI of the request (never {@code null})
     */
    URI getURI();

    /**
     * Return a read-only map with parsed and decoded query parameter values.
     *
     * @return a map of all query parameters
     */
    Map<String, List<String>> getQueryParams();

    /**
     * Returns an array of @{code String} objects containing all of the values
     * the given request parameter has, or @{code null} if the parameter does not exist.
     *
     * @param name a @{code String} with the name of the parameter
     *
     * @return an array of @{code String} objects containing the parameter's values or @{code null}
     *
     * @see #getQueryParameter
     */
    default String[] getQueryParams(String name) {
        var params = this.getQueryParams().get(name);
        if (params == null) {
            return null;
        }

        return params.toArray(String[]::new);
    }

    /**
     * Returns the value of a request parameter as an @{code Optional} of @{code String}.
     *
     * If this method is called for a multivalued parameter, the value returned is equal to the first value
     * in the array returned by @{link #getQueryParams(String)}. This means that the return value
     * will be @{code Optional.empty()} when either the query parameter does not exist or has no value.
     *
     * @param name a @{code String} specifying the name of the parameter
     *
     * @return an @{code Optional} of @{code String} representing the single/first value of the parameter
     *
     * @see #getQueryParams(String)
     */
    default Optional<String> getQueryParameter(String name) {
        return Optional.ofNullable(this.getQueryParams(name)).flatMap(params -> Stream.of(params).findFirst());
    }

}
