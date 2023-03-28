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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.common.FileExists;
import org.fuin.objects4j.common.FileExistsValidator;
import org.fuin.objects4j.common.IsFile;
import org.fuin.objects4j.common.IsFileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application configuration.
 */
@XmlRootElement(name = "marchetyper-config")
public final class ConfigImpl implements Config {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigImpl.class);

    @XmlAttribute(name = "src-dir")
    private String srcDir;

    @XmlAttribute(name = "dest-dir")
    private String destDir;

    @XmlAttribute(name = "binary-files")
    private String binaryFiles;

    @XmlAttribute(name = "text-files")
    private String textFiles;

    @XmlAttribute(name = "custom-pom-file")
    private String customPomFile;

    @XmlElement(name = "archetype")
    private Archetype archetype;

    @XmlElementWrapper(name = "variables")
    @XmlElement(name = "variable")
    private List<Variable> variables;

    @XmlElementWrapper(name = "path-mappings")
    @XmlElement(name = "mapping")
    private List<Mapping> pathMappings;

    @XmlElementWrapper(name = "text-file-mappings")
    @XmlElement(name = "mapping")
    private List<Mapping> fileMappings;

    @XmlElementWrapper(name = "file-includes")
    @XmlElement(name = "filter")
    private List<FileFilter> fileIncludes;

    @XmlElementWrapper(name = "file-excludes")
    @XmlElement(name = "filter")
    private List<FileFilter> fileExcludes;

    /**
     * Default constructor.
     */
    public ConfigImpl() {
        super();
    }

    /**
     * Constructor with source and destination directory.
     * 
     * @param srcDir
     *            Source directory.
     * @param destDir
     *            Destination directory.
     */
    public ConfigImpl(final File srcDir, final File destDir) {
        super();
        this.srcDir = srcDir.toString();
        this.destDir = destDir.toString();
    }

    @Override
    public final File getSrcDir(final File baseDir) {
        final File file = new File(srcDir);
        if (file.isAbsolute()) {
            return file;
        }
        return canonical(new File(baseDir, srcDir));
    }

    @Override
    public final File getDestDir(final File baseDir) {
        final File file = new File(destDir);
        if (file.isAbsolute()) {
            return file;
        }
        return canonical(new File(baseDir, destDir));
    }

    @Override
    public final File getCustomPomFile(final File baseDir) {
        if (customPomFile == null) {
            return null;
        }
        final File file = new File(customPomFile);
        if (file.isAbsolute()) {
            return file;
        }
        return canonical(new File(baseDir, customPomFile));
    }

    @Override
    public final Archetype getArchetype() {
        return archetype;
    }

    @Override
    public final List<Variable> getVariables() {
        if (variables == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(variables);
    }

    @Override
    public final List<Mapping> getPathMappings() {
        if (pathMappings == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(pathMappings);
    }

    @Override
    public final List<Mapping> getFileMappings() {
        if (fileMappings == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(fileMappings);
    }

    @Override
    public final List<FileFilter> getFileIncludes() {
        if (fileIncludes == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(fileIncludes);
    }

    @Override
    public final List<FileFilter> getFileExcludes() {
        if (fileExcludes == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(fileExcludes);
    }

    @Override
    public final String getBinaryFiles() {
        return binaryFiles;
    }

    @Override
    public final String getTextFiles() {
        return textFiles;
    }

    @Override
    public final boolean includes(final File file) {
        if (fileIncludes == null) {
            return false;
        }
        for (final FileFilter filter : fileIncludes) {
            if (filter.applies(file)) {
                LOG.info("File '{} included by: {}", file, filter);
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean excludes(final File file) {
        if (fileExcludes == null) {
            return false;
        }
        for (final FileFilter filter : fileExcludes) {
            if (filter.applies(file)) {
                LOG.info("File '{} excluded by: {}", file, filter);
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean isBinary(final File srcFile) {
        if (binaryFiles == null) {
            binaryFiles = ".*\\.(jar)";
        }
        return srcFile.getName().matches(binaryFiles);
    }

    @Override
    public final boolean isText(final File srcFile) {
        if (textFiles == null) {
            binaryFiles = ".*\\.(properties|md|java|xml)";
        }
        return srcFile.getName().matches(textFiles);
    }

    private File canonical(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to get canonical file of: " + file, ex);
        }
    }

    /**
     * Creates a new configuration instance by loading it from a file or creating a new instance.
     * 
     * @param parameters
     *            File to load configuration instance from.
     * 
     * @return New configuration instance.
     */
    public static ConfigImpl loadOrNew(final List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return new ConfigImpl();
        }
        return load(new File(parameters.get(0)));
    }

    /**
     * Creates a new configuration instance by loading it from a file.
     * 
     * @param file
     *            File to load configuration instance from.
     * 
     * @return New configuration instance.
     */
    public static ConfigImpl load(@NotNull @FileExists @IsFile final File file) {
        Contract.requireArgNotNull("file", file);
        FileExistsValidator.requireArgValid("file", file);
        IsFileValidator.requireArgValid("file", file);

        try (final Reader reader = new BufferedReader(new FileReader(file))) {
            final JAXBContext ctx = JAXBContext.newInstance(ConfigImpl.class);
            final Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (ConfigImpl) unmarshaller.unmarshal(reader);
        } catch (final IOException | JAXBException ex) {
            throw new RuntimeException("Error loading XML config: " + file, ex);
        }

    }

}
