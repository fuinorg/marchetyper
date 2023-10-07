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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Archetype definition.
 */
@XmlRootElement(name = "archetype")
public final class Archetype {

    @XmlAttribute(name = "group-id")
    private String groupId;

    @XmlAttribute(name = "artifact-id")
    private String artifactId;

    @XmlAttribute(name = "version")
    private String version;

    @XmlAttribute(name = "version-pom-file")
    private String versionPomFile;

    @XmlElement(name = "property")
    private List<Property> properties;

    /**
     * Default constructor for JAXB.
     */
    protected Archetype() {
        super();
    }

    /**
     * Returns the group identifier.
     * 
     * @return Group ID.
     */
    public final String getGroupId() {
        return groupId;
    }

    /**
     * Returns the artifact identifier.
     * 
     * @return Artifact ID.
     */
    public final String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the version.
     * 
     * @param baseDir
     *            Base directory, possibly used to locate pom.xml to extract version from.
     * 
     * @return Version.
     */
    public final String retrieveVersion(final File baseDir) {
        final File pomFile;
        if (versionPomFile == null) {
            pomFile = null;
        } else {
            final File file = new File(versionPomFile);
            if (file.isAbsolute()) {
                pomFile = file;
            } else {
                pomFile = new File(baseDir, versionPomFile);
            }
        }
        return retrieveVersion(pomFile, version);
    }

    /**
     * Returns the version.
     * 
     * @return Version.
     */
    public final String getVersion() {
        return version;
    }

    /**
     * Returns a list of defined properties.
     * 
     * @return Immutable list.
     */
    public final List<Property> getProperties() {
        if (properties == null) {
            Collections.emptyList();
        }
        return Collections.unmodifiableList(properties);
    }

    /**
     * Returns the file of the Maven POM to read the version from.
     * 
     * @param baseDir
     *            Base directory used in case the 'versionPomFile' is not absolute.
     * 
     * @return File.
     */
    public final File getVersionPomFile(final File baseDir) {
        if (versionPomFile == null) {
            return null;
        }
        final File file = new File(versionPomFile);
        if (file.isAbsolute()) {
            return file;
        }
        return canonical(new File(baseDir, versionPomFile));
    }

    /**
     * Tries to locate a property by it's name.
     * 
     * @param name
     *            Name of property to find.
     * 
     * @return Property or <code>null</code> if the key was not find.
     */
    public final Property findProperty(final String name) {
        for (final Property property : getProperties()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Creates an argument list for generating an archetype using the configuration test data.
     * 
     * @param baseDir
     *            Base directory for relative path resolution.
     * 
     * @return List of arguments for the 'archetype:generate' Maven command.
     */
    public final List<String> toArchetypeGenerateArgs(final File baseDir) {

        final List<String> args = new ArrayList<>();
        args.add("-DarchetypeGroupId=" + groupId);
        args.add("-DarchetypeArtifactId=" + artifactId);
        args.add("-DarchetypeVersion=" + retrieveVersion(baseDir));
        if (properties != null) {
            for (final Property property : properties) {
                args.add("-D" + property.getName() + "=" + property.getTestValue());
            }
            args.add("-DinteractiveMode=false");
        }
        return args;
    }

    /**
     * Builds an instance of the outer class.
     */
    public static final class Builder {

        private Archetype delegate;

        /**
         * Default constructor.
         */
        public Builder() {
            this.delegate = new Archetype();
        }

        /**
         * Sets the Maven group ID.
         * 
         * @param groupId
         *            ID to set.
         * 
         * @return The builder.
         */
        public Builder groupId(final String groupId) {
            delegate.groupId = groupId;
            return this;
        }

        /**
         * Sets the Maven artifact ID.
         * 
         * @param artifactId
         *            ID to set.
         * 
         * @return The builder.
         */
        public Builder artifactId(final String artifactId) {
            delegate.artifactId = artifactId;
            return this;
        }

        /**
         * Sets the Maven version.
         * 
         * @param version
         *            Number to set.
         * 
         * @return The builder.
         */
        public Builder version(final String version) {
            delegate.version = version;
            return this;
        }

        /**
         * Sets the Maven POM file reference to read the version from.
         * 
         * @param versionPomFile
         *            Path and file name of the POM to read the version from.
         * 
         * @return The builder.
         */
        public Builder versionPomFile(final String versionPomFile) {
            delegate.versionPomFile = versionPomFile;
            return this;
        }

        /**
         * Sets the properties.
         * 
         * @param properties
         *            Properties to set.
         * 
         * @return The builder.
         */
        public Builder properties(final List<Property> properties) {
            delegate.properties = properties;
            return this;
        }

        /**
         * Adds a property to the propetrty list.
         * 
         * @param property
         *            Property to add.
         * 
         * @return The builder.
         */
        public Builder addProperty(final Property property) {
            if (delegate.properties == null) {
                delegate.properties = new ArrayList<>();
            }
            delegate.properties.add(property);
            return this;
        }

        /**
         * Creates a new instance with data from the builder.
         * 
         * @return New instance.
         */
        public Archetype build() {
            if (delegate.groupId == null) {
                throw new IllegalStateException("groupId is mandatory, but not set");
            }
            if (delegate.artifactId == null) {
                throw new IllegalStateException("artifactId is mandatory, but not set");
            }
            if (delegate.version == null && delegate.versionPomFile == null) {
                throw new IllegalStateException("Either 'version' or 'versionPomFile' must be set");
            }
            final Archetype tmp = delegate;
            delegate = new Archetype();
            return tmp;
        }

    }

    static String retrieveVersion(final File versionPomFile, final String version) {
        if (version != null) {
            return version;
        }
        if (versionPomFile != null) {
            return readVersion(versionPomFile);
        }
        throw new IllegalStateException("Neither 'versionPomFile' nor 'version' was set");
    }

    private static String readVersion(File versionPomFile) {
        try (final Reader reader = new FileReader(versionPomFile)) {
            final MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            final Model model = xpp3Reader.read(reader);
            String version = model.getVersion();
            if (version == null && model.getParent() != null) {
                version = model.getParent().getVersion();
            }
            if (version == null) {
                throw new IllegalStateException("Version not available: " + versionPomFile);
            }
            if (version.contains("${")) {
                throw new IllegalStateException("Version with variable is not supported: '" + version + "' (" + versionPomFile + ")");
            }
            return version;
        } catch (final IOException | XmlPullParserException ex) {
            throw new IllegalStateException("Failed to read version from POM: " + versionPomFile, ex);
        }
    }

    private static File canonical(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to get canonical file of: " + file, ex);
        }
    }

}
