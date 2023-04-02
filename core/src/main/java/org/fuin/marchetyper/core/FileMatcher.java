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

/**
 * Determines if a file matches some condition.
 */
public interface FileMatcher {

    /**
     * Determines if a file should be included.
     *
     * @param file
     *            File to test.
     *
     * @return {@literal true} if the file should be included base on the inclusion pattern.
     */
    public boolean includes(File file);

    /**
     * Determines if a file should be excluded.
     *
     * @param file
     *            File to test.
     *
     * @return {@literal true} if the file should be excluded base on the exclusion pattern.
     */
    public boolean excludes(File file);

    /**
     * Determines if a file is considered binary.
     *
     * @param file
     *            File to test.
     *
     * @return {@literal true} if the file is interpreted as binary.
     */
    public boolean isBinary(File file);

    /**
     * Determines if a file is considered text.
     *
     * @param file
     *            File to test.
     *
     * @return {@literal true} if the file is interpreted as text.
     */
    public boolean isText(File file);

}
