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
import java.util.List;

/**
 * Application configuration.
 */
public interface Config {

    /**
     * Returns the source directory.
     *
     * @param baseDir
     *            Base directory used in case the 'srcDir' is not absolute.
     * @return Source directory.
     */
    public File getSrcDir(File baseDir);

    /**
     * Returns the destination directory.
     *
     * @param baseDir
     *            Base directory used in case the 'destDir' is not absolute.
     * @return Destination directory.
     */
    public File getDestDir(File baseDir);

    /**
     * Defines the newly created archetype should be executed with defined test values. The result will then be compared with the original
     * source project.
     * 
     * @return TRUE for starting a test.
     */
    public boolean isTest();

    /**
     * Returns the archetype definition.
     * 
     * @return Archetype.
     */
    public Archetype getArchetype();

    public List<Variable> getVariables();

    public List<Mapping> getPathMappings();

    public List<Mapping> getFileMappings();

    public List<FileFilter> getFileIncludes();

    public List<FileFilter> getFileExcludes();

    public String getBinaryFiles();

    public String getTextFiles();

    public boolean includes(File file);

    public boolean excludes(File file);

    public boolean isBinary(File srcFile);

    public boolean isText(File srcFile);

}
