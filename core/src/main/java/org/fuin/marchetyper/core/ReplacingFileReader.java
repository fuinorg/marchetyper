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
import java.util.List;

import javax.validation.constraints.NotNull;

import org.fuin.objects4j.common.ConstraintViolationException;
import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.common.Nullable;

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
     * Constructor with array and buffer size.
     * 
     * @param file
     *            File to read.
     * @param bufferSize
     *            Size of the read buffer.
     * @param defaultFileExpr
     *            Default regular file name expression used to determine if the replacement should be applied. May be <code>null</code> if
     *            there is not default.
     * @param mappings
     *            Key/Value pairs to replace.
     * 
     * @throws FileNotFoundException
     *             The given file does not exist.
     */
    public ReplacingFileReader(@NotNull final File file, final int bufferSize, @Nullable final String defaultFileExpr,
            @NotNull final Mapping... mappings) throws FileNotFoundException {
        this(file, bufferSize, defaultFileExpr, mappings == null ? (List<Mapping>) null : Arrays.asList(mappings));
    }

    /**
     * Constructor with array.
     * 
     * @param file
     *            File to read.
     * @param defaultFileExpr
     *            Default regular file name expression used to determine if the replacement should be applied. May be <code>null</code> if
     *            there is not default.
     * @param mappings
     *            Key/Value pairs to replace.
     * 
     * @throws FileNotFoundException
     *             The given file does not exist.
     */
    public ReplacingFileReader(@NotNull final File file, @Nullable final String defaultFileExpr, @NotNull final Mapping... mappings)
            throws FileNotFoundException {
        this(file, 1024, defaultFileExpr, mappings == null ? (List<Mapping>) null : Arrays.asList(mappings));
    }

    /**
     * Constructor with list.
     * 
     * @param file
     *            File to read.
     * @param defaultFileExpr
     *            Default regular file name expression used to determine if the replacement should be applied. May be <code>null</code> if
     *            there is not default.
     * @param mappings
     *            Key/Value pairs to replace.
     * 
     * @throws FileNotFoundException
     *             The given file does not exist.
     */
    public ReplacingFileReader(@NotNull final File file, @Nullable final String defaultFileExpr, @NotNull final List<Mapping> mappings)
            throws FileNotFoundException {
        this(file, 1024, defaultFileExpr, mappings);
    }

    /**
     * Constructor with all data.
     * 
     * @param file
     *            File to read.
     * @param bufferSize
     *            Size of the read buffer.
     * @param defaultFileExpr
     *            Default regular file name expression used to determine if the replacement should be applied. May be <code>null</code> if
     *            there is not default.
     * @param mappings
     *            Key/Value pairs to replace.
     * 
     * @throws FileNotFoundException
     *             The given file does not exist.
     */
    public ReplacingFileReader(@NotNull final File file, final int bufferSize, @Nullable final String defaultFileExpr,
            @NotNull final List<Mapping> mappings) throws FileNotFoundException {
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
            if (mapping.applies(defaultFileExpr, file)) {
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
        this.delegate = new PushbackReader(
                new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8")), bufferSize),
                maxTokenLen + 1);

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

}
