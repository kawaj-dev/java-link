package com.javalink.model;

import java.net.URI;
import java.util.Objects;

/** Java SE 21の公式仕様・APIへの構造化された参照です。 */
public record CodeReadingOfficialReference(
        CodeReadingOfficialSource source,
        String version,
        String sourceName,
        String sectionNumber,
        String sectionTitle,
        String description,
        URI uri
) {

    public CodeReadingOfficialReference {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(sourceName, "sourceName must not be null");
        Objects.requireNonNull(sectionNumber, "sectionNumber must not be null");
        Objects.requireNonNull(sectionTitle, "sectionTitle must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(uri, "uri must not be null");

        if (version.isBlank() || sourceName.isBlank()
                || sectionNumber.isBlank() || sectionTitle.isBlank() || description.isBlank()) {
            throw new IllegalArgumentException("official reference fields must not be blank");
        }
        if (!uri.isAbsolute() || !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("official reference URI must use HTTPS");
        }
        if (!"docs.oracle.com".equalsIgnoreCase(uri.getHost())) {
            throw new IllegalArgumentException("official reference URI must use docs.oracle.com");
        }
    }
}
