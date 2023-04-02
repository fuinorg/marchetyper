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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fuin.objects4j.common.ConstraintViolationException;
import org.fuin.objects4j.common.Contract;

/**
 * Replaces tokens before returning lines.
 */
public final class ReplacingFileReader extends Reader {

    private final String[] searchList;

    private final String[] replacementList;

    private PushbackReader delegate;

    private int maxTokenLen;

    private String replaceStr;

    private int replaceIdx;

    /**
     * Constructor with all data.
     * 
     * @param file
     *            File to read.
     * @param bufferSize
     *            Size of the read buffer.
     * @param defaultRegExFilenameSelector
     *            Regular expression that works on filenames. It will be used to determine if the replacement should be applied at all for
     *            the given type of file. May be <code>null</code> if all file types are OK. It's something like
     *            "(.*\.(properties|md|java|yml|yaml|xml))|Dockerfile" to identify if it's a text file. It will ONLY be used in case a
     *            mapping has neither a {@link #fileExpr} nor a {@link #pathExpr} defined (both are {@literal null}).
     * @param mappings
     *            Key/Value pairs to replace.
     */
    private ReplacingFileReader(final File file, final int bufferSize, final String defaultRegExFilenameSelector,
            final List<Mapping> mappings) {
        super();
        Contract.requireArgNotNull("file", file);
        Contract.requireArgNotNull("mappings", mappings);

        // Find valid mappings
        final List<Mapping> validMappings = new ArrayList<>();
        for (final Mapping mapping : mappings) {
            if (mapping == null) {
                throw new ConstraintViolationException("The argument 'replacements' contains null elements: " + mappings.toArray());
            }
            if (mapping.getSearch() == null) {
                throw new ConstraintViolationException("The argument 'search' contains null value: " + mapping);
            }
            if (mapping.getSearch().trim().length() == 0) {
                throw new ConstraintViolationException("The argument 'search' contains empty string: " + mapping);
            }
            if (mapping.getReplace() == null) {
                throw new ConstraintViolationException("The argument 'replace' contains null value: " + mapping);
            }
            if (mapping.getReplace().trim().length() == 0) {
                throw new ConstraintViolationException("The argument 'replace' contains empty string: " + mapping);
            }
            if (mapping.applies(defaultRegExFilenameSelector, file)) {
                validMappings.add(mapping);
            }
        }

        // Create search/replace array
        this.maxTokenLen = 0;
        this.searchList = new String[validMappings.size()];
        this.replacementList = new String[validMappings.size()];
        for (int i = 0; i < validMappings.size(); i++) {
            final Mapping mapping = validMappings.get(i);
            searchList[i] = mapping.getSearch().trim();
            replacementList[i] = mapping.getReplace().trim();
            if (searchList[i].length() > maxTokenLen) {
                maxTokenLen = searchList[i].length();
            }
        }
        try {
            this.delegate = new PushbackReader(
                    new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8")), bufferSize),
                    maxTokenLen + 1);
        } catch (final FileNotFoundException ex) {
            throw new IllegalStateException(
                    "The existance of the file was verified in the builder, but now the file does not exist anymore: " + file, ex);
        }

    }

    @Override
    public final int read() throws IOException {

        if (replaceStr != null) {
            // Currently returning replacement
            if (replaceIdx < replaceStr.length()) {
                return replaceStr.charAt(replaceIdx++);
            }
            if (replaceIdx == replaceStr.length()) {
                replaceStr = null;
                replaceIdx = 0;
            }
        }

        // Read next chunk
        final char[] buffer = new char[maxTokenLen];
        final int count = delegate.read(buffer);
        if (count < 1 || buffer[0] == '\u0000') {
            return -1;
        }
        final String str = String.valueOf(buffer, 0, count);

        for (int i = 0; i < searchList.length; i++) {
            final String search = searchList[i];
            if (str.startsWith(search)) {
                final int len = maxTokenLen - search.length();
                final int off = search.length();
                if (len > 0) {
                    delegate.unread(buffer, off, len);
                }
                replaceStr = replacementList[i];
                replaceIdx = 0;
                return replaceStr.charAt(replaceIdx++);
            }
        }

        // No match
        final char ch = buffer[0];
        delegate.unread(buffer, 1, count - 1);
        return ch;

    }

    @Override
    public final int read(final char[] cbuf, final int off, final int len) throws IOException {
        int charsRead = 0;
        for (int i = 0; i < len; i++) {
            final int nextChar = read();
            if (nextChar == -1) {
                if (charsRead == 0) {
                    charsRead = -1;
                }
                break;
            }
            charsRead = i + 1;
            cbuf[off + i] = (char) nextChar;
        }
        return charsRead;
    }

    @Override
    public final boolean ready() throws IOException {
        return delegate.ready();
    }

    @Override
    public final void close() throws IOException {
        delegate.close();
    }

    /**
     * Creates a new instance of the outer class.
     */
    public static final class Builder {

        private File file;

        private int bufferSize;

        private String defaultRegExFilenameSelector;

        private List<Mapping> mappings;

        /**
         * Constructor with file.
         * 
         * @param file
         *            File to read.
         * @throws FileNotFoundException
         *             the given file does not exist.
         */
        public Builder(final File file) throws FileNotFoundException {
            Contract.requireArgNotNull("file", file);
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            this.file = file;
            this.bufferSize = 1024;
            this.mappings = new ArrayList<>();
        }

        /**
         * Sets the size of the buffer for the underlying input stream.
         * 
         * @param bufferSize
         *            Size of the input stream buffer to use.
         * 
         * @return The builder.
         */
        public Builder bufferSize(final int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        /**
         * Sets the default file pattern to use in case a mapping has not defined it's own.
         * 
         * @param defaultRegExFilenameSelector
         *            Regular expression that works on filenames. It will be used to determine if the replacement should be applied at all
         *            for the given type of file. May be <code>null</code> if all file types are OK. It's something like
         *            "(.*\.(properties|md|java|yml|yaml|xml))|Dockerfile" to identify if it's a text file. It will ONLY be used in case
         *            this mapping has neither a <b>fileExpr</b> nor a <b>pathExpr</b> defined (both are {@literal null}).
         * 
         * @return The Builder.
         */
        public Builder defaultRegExFilenameSelector(final String defaultRegExFilenameSelector) {
            this.defaultRegExFilenameSelector = defaultRegExFilenameSelector;
            return this;
        }

        /**
         * Sets the list of mappings.
         * 
         * @param mappings
         *            List of find/replace operations to apply.
         * 
         * @return The builder.
         */
        public Builder mappings(final List<Mapping> mappings) {
            if (mappings == null) {
                this.mappings = Collections.emptyList();
            } else {
                this.mappings = new ArrayList<>(mappings);
            }
            return this;
        }

        /**
         * Sets the array of mappings.
         * 
         * @param mappings
         *            Array of find/replace operations to apply.
         * 
         * @return The builder.
         */
        public Builder mapping(final Mapping... mappings) {
            if (mappings == null) {
                this.mappings = Collections.emptyList();
            } else {
                this.mappings = Arrays.asList(mappings);
            }
            return this;
        }

        /**
         * Creates a new instance.
         * 
         * @return The new instance.
         */
        public ReplacingFileReader build() {
            return new ReplacingFileReader(file, bufferSize, defaultRegExFilenameSelector, mappings);
        }

    }

}
