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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.fuin.objects4j.common.Nullable;

/**
 * Defines a path and/or file filter with expressions.
 */
@XmlRootElement(name = "filter")
public final class FileFilter {

    @XmlAttribute(name = "path")
    private String pathExpr;

    @XmlAttribute(name = "file")
    private String fileExpr;

    /**
     * JAXB constructor.
     */
    protected FileFilter() {
        super();
    }

    /**
     * Constructor with all fields.
     * 
     * @param pathExpr
     *            Regular path expression used to determine if the file should be selected.
     * @param fileExpr
     *            Regular file name expression used to determine if the file should be selected.
     */
    public FileFilter(@Nullable final String pathExpr, @Nullable final String fileExpr) {
        super();
        this.pathExpr = trim(pathExpr);
        this.fileExpr = trim(fileExpr);
    }

    /**
     * Returns the regular path expression used to determine if the file should be selected.
     * 
     * @return Regular path expression.
     */
    public final String getPath() {
        return pathExpr;
    }

    /**
     * Returns the regular file name expression used to determine if the file should be selected.
     * 
     * @return Regular file name expression.
     */
    public final String getFile() {
        return fileExpr;
    }

    /**
     * Verifies if the condition applies for a given file.
     * 
     * @param file
     *            File name and path to verify the rule against.
     * 
     * @return TRUE if the rule matches.
     */
    public final boolean applies(final File file) {
        if (fileExpr == null) {
            if (pathExpr == null) {
                return true;
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
        if (pathExpr == null) {
            return "FileFilter [fileExpr=" + fileExpr + "]";
        }
        if (fileExpr == null) {
            return "FileFilter [pathExpr=" + pathExpr + "]";
        }
        return "FileFilter [pathExpr=" + pathExpr + ", fileExpr=" + fileExpr + "]";
    }

    private static String trim(final String str) {
        if (str == null) {
            return null;
        }
        final String trimmed = str.trim();
        if (trimmed.length() == 0) {
            return null;
        }
        return trimmed;
    }

}
