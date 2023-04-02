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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fuin.utils4j.fileprocessor.FileHandlerResult;
import org.fuin.utils4j.fileprocessor.FileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies files from one directory to another and eventually modifies text files during this process.
 */
public final class FileCopy {

    private static final Logger LOG = LoggerFactory.getLogger(FileCopy.class);

    private File srcBaseDir;

    private File destBaseDir;

    private PathMapper pathMapper;

    private FileMatcher fileMatcher;

    private HeaderProvider headerProvider;

    private String defaultRegExFilenameSelector;

    private List<Mapping> mappings;

    private FileCopy() {
        super();
        mappings = new ArrayList<>();
    }

    /**
     * Executes the copy.
     * 
     * @return Result.
     */
    public FileCopyResult copy() {

        final FileCopyResult.Builder result = new FileCopyResult.Builder();

        allFiles(srcBaseDir).stream().filter((file) -> {
            if (fileMatcher.includes(file)) {
                return true;
            }
            return !fileMatcher.excludes(file);
        }).forEach((srcFile) -> {

            final File destFile = pathMapper.map(srcFile);
            destFile.getParentFile().mkdirs();
            if (fileMatcher.isBinary(srcFile)) {
                copyBinaryFile(srcFile, destFile);
                result.addBinaryFile(destFile);
            } else if (fileMatcher.isText(srcFile)) {
                copyTextFile(srcFile, destFile, defaultRegExFilenameSelector, mappings, headerProvider);
                result.addTextFile(destFile);
            } else {
                throw new IllegalStateException("File found that is neither binary nor text file: " + srcFile);
            }

        });

        return result.build();
    }

    private static void copyBinaryFile(final File srcFile, final File destFile) {
        LOG.info("Copy binary {} to {}", srcFile, destFile);
        try {
            FileUtils.copyFile(srcFile, destFile);
        } catch (final IOException ex) {
            throw new RuntimeException("Error copying binary file from " + srcFile + " to " + destFile, ex);
        }
    }

    private static void copyTextFile(final File srcFile, final File destFile, final String defaultRegExFilenameSelector,
            final List<Mapping> mappings, final HeaderProvider headerProvider) {
        LOG.info("Copy text {} to {}", srcFile, destFile);
        try (final ReplacingFileReader reader = new ReplacingFileReader.Builder(srcFile)
                .defaultRegExFilenameSelector(defaultRegExFilenameSelector).mappings(mappings).build()) {

            try (final Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(destFile), Charset.forName("utf-8")))) {
                headerProvider.write(writer);
                IOUtils.copy(reader, writer);
            }

        } catch (final IOException ex) {
            throw new RuntimeException("Error copying text file from " + srcFile + " to " + destFile, ex);
        }

    }

    private static List<File> allFiles(final File dir) {
        final List<File> files = new ArrayList<>();
        new FileProcessor((file) -> {
            if (file.isFile()) {
                files.add(file);
            }
            return FileHandlerResult.CONTINUE;
        }).process(dir);
        return files;
    }

    /**
     * Builds a new instance of the outer class.
     */
    public static final class Builder {

        private FileCopy delegate;

        public Builder() {
            delegate = new FileCopy();
        }

        /**
         * Sets the source directory with the files to copy.
         * 
         * @param baseDir
         *            Source directory.
         * 
         * @return The builder.
         */
        public Builder srcBaseDir(final File baseDir) {
            delegate.srcBaseDir = baseDir;
            return this;
        }

        /**
         * Sets the target directory to copy the files to.
         * 
         * @param baseDir
         *            Target directory.
         * 
         * @return The builder.
         */
        public Builder destBaseDir(final File baseDir) {
            delegate.destBaseDir = baseDir;
            return this;
        }

        /**
         * Mapper that modifies the path in the target directory.
         * 
         * @param pathMapper
         *            Mapper.
         * 
         * @return The builder.
         */
        public Builder pathMapper(final PathMapper pathMapper) {
            delegate.pathMapper = pathMapper;
            return this;
        }

        /**
         * Determines if a file in the source directory or it's sub directors should be copied.
         * 
         * @param fileMatcher
         *            Matcher.
         * 
         * @return The builder.
         */
        public Builder fileMatcher(final FileMatcher fileMatcher) {
            delegate.fileMatcher = fileMatcher;
            return this;
        }

        /**
         * Provides a header to insert in every text file that is copied.
         * 
         * @param headerProvider
         *            Provides the header text to insert at the beginning of every text file.
         * 
         * @return The builder.
         */
        public Builder headerProvider(final HeaderProvider headerProvider) {
            delegate.headerProvider = headerProvider;
            return this;
        }

        /**
         * Sets the default file pattern to use in case a mapping has not defined it's own.
         * 
         * @param defaultRegExFilenameSelector
         *            Regular expression that works on filenames. It will be used to determine if the replacement should be applied at all
         *            for the given type of file. May be <code>null</code> if all file types are OK. It's something like
         *            "(.*\.(properties|md|java|yml|yaml|xml))|Dockerfile" to identify if it's a text file. It will ONLY be used in case
         *            this mapping has neither a {@link #fileExpr} nor a {@link #pathExpr} defined (both are {@literal null}).
         * 
         * @return The Builder.
         */
        public Builder defaultRegExFilenameSelector(final String defaultRegExFilenameSelector) {
            delegate.defaultRegExFilenameSelector = defaultRegExFilenameSelector;
            return this;
        }

        /**
         * Sets the list of mappings that define search/replace.
         * 
         * @param mappings
         *            List of find/replace operations to apply.
         * 
         * @return The builder.
         */
        public Builder mappings(final List<Mapping> mappings) {
            if (mappings == null) {
                delegate.mappings = Collections.emptyList();
            } else {
                delegate.mappings = new ArrayList<>(mappings);
            }
            return this;
        }

        /**
         * Sets the array of mappings that define/search replace.
         * 
         * @param mappings
         *            Array of find/replace operations to apply.
         * 
         * @return The builder.
         */
        public Builder mapping(final Mapping... mappings) {
            if (mappings == null) {
                delegate.mappings = Collections.emptyList();
            } else {
                delegate.mappings = Arrays.asList(mappings);
            }
            return this;
        }

        /**
         * Builds a new instance of the outer class.
         * 
         * @return The new instance.
         */
        public FileCopy build() {
            if (delegate.srcBaseDir == null) {
                throw new IllegalStateException("It's mandatory to set a value for 'srcBaseDir'");
            }
            if (delegate.destBaseDir == null) {
                throw new IllegalStateException("It's mandatory to set a value for 'destBaseDir'");
            }
            if (delegate.pathMapper == null) {
                delegate.pathMapper = (file) -> file;
            }
            if (delegate.fileMatcher == null) {
                throw new IllegalStateException("It's mandatory to set a value for 'fileMatcher'");
            }
            if (delegate.headerProvider == null) {
                delegate.headerProvider = (writer) -> {
                };
            }

            final FileCopy tmp = delegate;
            delegate = new FileCopy();
            return tmp;
        }

    }

    /**
     * Adds an additional header.
     */
    public interface HeaderProvider {

        /**
         * Writes the header to the given writer.
         * 
         * @param writer
         *            Writer to add a header to.
         */
        public void write(Writer writer);

    }

}
