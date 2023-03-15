package org.fuin.marchetyper.core;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a transformation function with a dedicated name.
 */
public enum TransformationType {

    // Does not work as the Maven Archetype Plugin has no dependency to CaseUtils in case of calling it from the command line
    // TO_CAMEL_CASE("$CaseUtils.toCamelCase($~varName~, true, $delim.charAt(0), $delim.charAt(1), $delim.charAt(2), $delim.charAt(3))"),

    DOT_2_SLASH("$StringUtils.replace($~varName~, \".\", \"/\")"),

    SLASH_2_DOT("$StringUtils.replace($~varName~, \"/\", \".\")");

    private String code;

    private TransformationType(final String code) {
        this.code = code;
    }

    public String getCode(String varName) {
        return StringUtils.replace(code, "~varName~", varName);
    }

}
