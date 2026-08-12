package com.javalink.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalink.model.CodeReadingExplanationSection;
import com.javalink.model.ExplanationSectionType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExplanationSectionTypeTest {

    @Test
    void 全種類を従来どおり小文字JSONで表現する() throws Exception {
        List<String> values = Arrays.stream(ExplanationSectionType.values())
                .map(ExplanationSectionType::value)
                .toList();

        assertEquals(
                List.of(
                        "text", "table", "diagram", "examples", "qa", "comparison", "list",
                        "official-references"
                ),
                values
        );
        assertEquals("\"table\"", new ObjectMapper().writeValueAsString(
                ExplanationSectionType.TABLE
        ));
    }

    @Test
    void SectionModelに文字列layoutが存在しない() {
        List<String> components = Arrays.stream(
                        CodeReadingExplanationSection.class.getRecordComponents()
                )
                .map(component -> component.getName())
                .toList();

        assertEquals(
                List.of("sectionType", "title", "entries", "officialReferences"),
                components
        );
        assertFalse(components.contains("layout"));
    }
}
