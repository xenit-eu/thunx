package com.contentgrid.thunx.pdp.opa;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

class DefaultOpaInputProviderTest {

    private static String[] toPath(String uri) {
        return DefaultOpaInputProvider.uriToPathArray(URI.create(uri));
    }
    @Test
    void uriToPathArray_rootShouldBeEmptyArray() {
        assertThat(toPath("https://localhost:8080/")).isEqualTo(new String[0]);
        assertThat(toPath("https://localhost:8080")).isEqualTo(new String[0]);
    }

    @Test
    void uriToPathArray_path() {
        assertThat(toPath("https://localhost:8080/api/foo")).isEqualTo(new String[]{"api", "foo"});
    }

    @Test
    void uriToPathArray_pathShouldBeNormalized() {
        assertThat(toPath("https://localhost:8080//api//bar")).isEqualTo(new String[]{ "api", "bar"});
    }
}