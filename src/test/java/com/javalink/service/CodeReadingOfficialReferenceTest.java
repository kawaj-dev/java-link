package com.javalink.service;

import com.javalink.model.CodeReadingOfficialReference;
import com.javalink.model.CodeReadingOfficialSource;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodeReadingOfficialReferenceTest {

    @Test
    void acceptsJavaSe21OracleHttpsReference() {
        assertDoesNotThrow(() -> reference(
                URI.create("https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.6")
        ));
    }

    @Test
    void rejectsNonHttpsAndNonOracleReferences() {
        assertThrows(IllegalArgumentException.class, () -> reference(
                URI.create("http://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.6")
        ));
        assertThrows(IllegalArgumentException.class, () -> reference(
                URI.create("https://example.com/jls-6.html#jls-6.6")
        ));
    }

    private CodeReadingOfficialReference reference(URI uri) {
        return new CodeReadingOfficialReference(
                CodeReadingOfficialSource.JLS,
                "Java SE 21",
                "JLS Java SE 21",
                "§6.6",
                "Access Control",
                "Javaのアクセス制御について定めています。",
                uri
        );
    }
}
