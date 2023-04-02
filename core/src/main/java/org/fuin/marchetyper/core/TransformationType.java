package org.fuin.marchetyper.core;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a transformation function with a dedicated name.
 */
public enum TransformationType {

    // Does not work as the Maven Archetype Plugin has no dependency to CaseUtils in case of calling it from the command line
    // TO_CAMEL_CASE("$CaseUtils.toCamelCase($~varName~, true, $delim.charAt(0), $delim.charAt(1), $delim.charAt(2), $delim.charAt(3))"),

    /** Converts all dots in a string to slashes. */
    DOT_2_SLASH("$StringUtils.replace($~varName~, \".\", \"/\")"),

    /** Converts all slashes in a string to dots. */
    SLASH_2_DOT("$StringUtils.replace($~varName~, \"/\", \".\")");

    private String code;

    private TransformationType(final String code) {
        this.code = code;
    }

    /**
     * Replaces the variable in the code with another name.
     * 
     * @param varName
     *            Variable name to replace the '~varName~' token with.
     * 
     * @return Replaced code fragment.
     */
    public String getCode(String varName) {
        return StringUtils.replace(code, "~varName~", varName);
    }

}
