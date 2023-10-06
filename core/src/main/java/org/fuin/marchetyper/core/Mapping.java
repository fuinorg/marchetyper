/**
 * Copyright (C) 2023 Future Invent IT Consulting GmbH. All rights reserved. 
 * http://www.fuin.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.marchetyper.core;

import java.io.File;

import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.common.NotEmpty;
import org.fuin.objects4j.common.Nullable;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Maps a search term to a replace expression.
 */
@XmlRootElement(name = "mapping")
public final class Mapping {

    @XmlAttribute(name = "search")
    private String search;

    @XmlAttribute(name = "replace")
    private String replace;

    @XmlAttribute(name = "path")
    private String pathExpr;

    @XmlAttribute(name = "file")
    private String fileExpr;

    /**
     * JAXB constructor.
     */
    protected Mapping() {
        super();
    }

    /**
     * Constructor with mandatory fields.
     * 
     * @param search
     *            Expression to search.
     * @param replace
     *            Expression to use for replacing the search term.
     */
    public Mapping(@NotEmpty final String search, @NotEmpty final String replace) {
        this(search, replace, null, null);
    }

    /**
     * Constructor with all fields.
     * 
     * @param search
     *            Term to search.
     * @param replace
     *            Text to use for replacing the search term.
     * @param pathExpr
     *            Regular path expression (path without file name) used to determine if the replacement should be applied. This restricts
     *            the mapping process to the given path types.
     * @param fileExpr
     *            Regular file name expression (file name without path) used to determine if the replacement should be applied. This
     *            restricts the mapping process to the given file names.
     */
    public Mapping(@NotEmpty final String search, @NotEmpty final String replace, @Nullable final String pathExpr,
            @Nullable final String fileExpr) {
        super();
        Contract.requireArgNotEmpty("search", search);
        Contract.requireArgNotEmpty("replace", replace);
        this.search = search;
        this.replace = replace;
        this.pathExpr = pathExpr;
        this.fileExpr = fileExpr;
    }

    /**
     * Returns the term to search.
     * 
     * @return Text to find.
     */
    public final String getSearch() {
        return search;
    }

    /**
     * Returns the text to use for replacing the search term.
     * 
     * @return Target text.
     */
    public final String getReplace() {
        return replace;
    }

    /**
     * Returns the regular path expression (path without file name) used to determine if the replacement should be applied. This restricts
     * the mapping process to the given path types.
     * 
     * @return Path without file name.
     */
    public final String getPathExpr() {
        return pathExpr;
    }

    /**
     * Returns the regular file name expression (file name without path) used to determine if the replacement should be applied. This
     * restricts the mapping process to the given file names.
     * 
     * @return File name without path.
     */
    public final String getFileExpr() {
        return fileExpr;
    }

    /**
     * Verifies if the condition applies for a given file.
     * 
     * @param file
     *            File name and path to verify the rul against.
     * 
     * @return TRUE if the rule matches.
     */
    public final boolean applies(final File file) {
        return applies(null, file);
    }

    /**
     * Verifies if the condition applies for a given file.
     * 
     * @param defaultRegExFilenameSelector
     *            Regular expression that works on filenames. It will be used to determine if the replacement should be applied at all for
     *            the given type of file. May be <code>null</code> if all file types are OK. It's something like
     *            "(.*\.(properties|md|java|yml|yaml|xml))|Dockerfile" to identify if it's a text file. It will ONLY be used in case this
     *            mapping has neither a {@link #fileExpr} nor a {@link #pathExpr} defined (both are {@literal null}).
     * @param file
     *            File name and path to verify the rule against.
     * 
     * @return TRUE if the rule matches.
     */
    public final boolean applies(final String defaultRegExFilenameSelector, final File file) {
        if (fileExpr == null) {
            if (pathExpr == null) {
                if (defaultRegExFilenameSelector == null) {
                    return true;
                }
                return file.getName().matches(defaultRegExFilenameSelector);
            }
            return file.getPath().replace(File.separatorChar, '/').matches(pathExpr);
        }
        if (pathExpr == null) {
            return file.getName().matches(fileExpr);
        }
        return file.getPath().replace(File.separatorChar, '/').matches(pathExpr) && file.getName().matches(fileExpr);

    }

    @Override
    public String toString() {
        return "Mapping [search=" + search + ", replace=" + replace + ", pathExpr=" + pathExpr + ", fileExpr=" + fileExpr + "]";
    }

    void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {
        this.search = search.replace("\\r", "\r");
        this.search = search.replace("\\n", "\n");
        this.search = search.replace("\\t", "\t");
        this.replace = replace.replace("\\r", "\r");
        this.replace = replace.replace("\\n", "\n");
        this.replace = replace.replace("\\t", "\t");
    }
}
