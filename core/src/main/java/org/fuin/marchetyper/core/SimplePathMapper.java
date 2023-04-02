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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.fuin.objects4j.common.ConstraintViolationException;
import org.fuin.objects4j.common.Contract;
import org.fuin.utils4j.Utils4J;

/**
 * Replaces a source path segment (structure of directories) with a target path segment. Example: Given a directory structure
 * 'a/b/c/d/e/f/g'. Source segment 'b/c/d' can be replaced with target segment 'x' . The target structure will be 'a/x/e/f/g'. Files within
 * a replacement path are not allowed and will be copied 1:1 to the same structure in the target.
 */
public final class SimplePathMapper implements PathMapper {

    private final File srcDir;

    private final File destDir;

    private final List<Mapping> mappings;

    /**
     * Constructor with array.
     * 
     * @param srcDir
     *            Source directory.
     * @param destDir
     *            Destination directory.
     * @param mappings
     *            Segments in the path structure to replace.
     */
    public SimplePathMapper(@NotNull final File srcDir, @NotNull final File destDir, @NotNull final Mapping... mappings) {
        this(srcDir, destDir, mappings == null ? (List<Mapping>) null : Arrays.asList(mappings));
    }

    /**
     * Constructor with list.
     * 
     * @param srcDir
     *            Source directory.
     * @param destDir
     *            Destination directory.
     * @param mappings
     *            Segments in the path structure to replace. <code>null</code> elements in the list are not allowed.
     */
    public SimplePathMapper(@NotNull final File srcDir, @NotNull final File destDir, @NotNull final List<Mapping> mappings) {
        super();

        Contract.requireArgNotNull("srcDir", srcDir);
        Contract.requireArgNotNull("destDir", destDir);
        Contract.requireArgNotNull("mappings", mappings);

        for (final Mapping mapping : mappings) {
            if (mapping == null) {
                throw new ConstraintViolationException("The argument 'replacements' contains null elements: " + mappings.toArray());
            }
            if (mapping.getSearch() == null) {
                throw new ConstraintViolationException("The argument 'search' contain null value: " + mapping);
            }
            if (mapping.getReplace() == null) {
                throw new ConstraintViolationException("The argument 'replace' contain null value: " + mapping.getSearch());
            }
        }

        this.srcDir = srcDir;
        this.destDir = destDir;
        this.mappings = mappings;

    }

    /**
     * Maps the source file to a target file.
     * 
     * @param file
     *            Source file to map.
     * 
     * @return Target file.
     */
    public final File map(@NotNull final File file) {
        Contract.requireArgNotNull("file", file);

        // Find mappings that apply
        final List<Mapping> validMappings = new ArrayList<>();
        for (final Mapping mapping : mappings) {
            if (mapping.applies(file)) {
                validMappings.add(mapping);
            }
        }

        // Create search/replace array
        final String[] searchList = new String[validMappings.size()];
        final String[] replacementList = new String[validMappings.size()];
        for (int i = 0; i < validMappings.size(); i++) {
            final Mapping kv = validMappings.get(i);
            searchList[i] = kv.getSearch();
            replacementList[i] = kv.getReplace().toString();
        }

        // Replace
        final String srcPathAndName = Utils4J.getRelativePath(srcDir, file).replace(File.separatorChar, '/');
        final String destPathAndName = StringUtils.replaceEach(srcPathAndName, searchList, replacementList);
        return new File(destDir, destPathAndName);

    }

}
