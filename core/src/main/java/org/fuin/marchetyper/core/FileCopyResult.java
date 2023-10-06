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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.fuin.utils4j.Utils4J;

/**
 * Result of copying files.
 */
public final class FileCopyResult {

    private final List<File> binaryFiles;

    private final List<File> textFiles;

    private FileCopyResult() {
        binaryFiles = new ArrayList<>();
        textFiles = new ArrayList<>();
    }

    /**
     * Returns the list of binary files copied.
     * 
     * @return List of 1:1 copied files.
     */
    public List<File> getBinaryFiles() {
        return Collections.unmodifiableList(binaryFiles);
    }

    /**
     * Returns the list of binary files copied with a path relative to the given base directory.
     * 
     * @param baseDir
     *            Directory to use as base directory for creating the relative path.
     * 
     * @return List of 1:1 copied files.
     */
    public List<File> getRelativizedBinaryFiles(final File baseDir) {
        return getBinaryFiles().stream().map(file -> new File(Utils4J.getRelativePath(baseDir, file))).collect(Collectors.toList());
    }

    /**
     * Returns the list of text files copied.
     * 
     * @return List of copied text files that also may have been modified.
     */
    public List<File> getTextFiles() {
        return Collections.unmodifiableList(textFiles);
    }

    /**
     * Returns the list of text files copied with a path relative to the given base directory.
     * 
     * @param baseDir
     *            Directory to use as base directory for creating the relative path.
     * 
     * @return List of copied text files that also may have been modified.
     */
    public List<File> getRelativizedTextFiles(final File baseDir) {
        return getTextFiles().stream().map(file -> new File(Utils4J.getRelativePath(baseDir, file))).collect(Collectors.toList());
    }

    /**
     * Builder that creates an instance of the outer class.
     */
    public static final class Builder {

        private FileCopyResult delegate;

        /**
         * Default constructor.
         */
        public Builder() {
            delegate = new FileCopyResult();
        }

        /**
         * Adds a binary file.
         * 
         * @param file
         *            File to add.
         * 
         * @return Builder instance.
         */
        public Builder addBinaryFile(final File file) {
            delegate.binaryFiles.add(file);
            return this;
        }

        /**
         * Adds a text file.
         * 
         * @param file
         *            File to add.
         * 
         * @return Builder instance.
         */
        public Builder addTextFile(final File file) {
            delegate.textFiles.add(file);
            return this;
        }

        /**
         * Creates a new instance of the outer class.
         * 
         * @return New instance.
         */
        public FileCopyResult build() {
            final FileCopyResult tmp = delegate;
            delegate = new FileCopyResult();
            Collections.sort(delegate.binaryFiles);
            Collections.sort(delegate.textFiles);
            return tmp;
        }

    }

}
