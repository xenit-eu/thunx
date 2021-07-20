package eu.xenit.contentcloud.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SymbolicReferenceTest {

    @Test
    void symbolToString() {
        //  user.clothing.coat.color
        var symbol = SymbolicReference.parse("user.clothing.coat.color");

        assertThat(symbol).hasToString("user.clothing.coat.color");
    }
}