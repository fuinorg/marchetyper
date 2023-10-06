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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    @XmlElement(name = "property")
    private List<Property> properties;

    /**
     * Default constructor for JAXB.
     */
    protected Archetype() {
        super();
    }

    /**
     * Constructor with list.
     * 
     * @param groupId
     *            Group identifier.
     * @param artifactId
     *            Artifact identifier.
     * @param version
     *            Version.
     * @param properties
     *            Defined properties for the archetype.
     */
    public Archetype(final String groupId, final String artifactId, final String version, final List<Property> properties) {
        this(groupId, artifactId, version, properties.toArray(new Property[properties.size()]));
    }

    /**
     * Constructor with array.
     * 
     * @param groupId
     *            Group identifier.
     * @param artifactId
     *            Artifact identifier.
     * @param version
     *            Version.
     * @param properties
     *            Defined properties for the archetype.
     */
    public Archetype(final String groupId, final String artifactId, final String version, final Property... properties) {
        super();

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        if (properties == null) {
            this.properties = null;
        } else {
            this.properties = Arrays.asList(properties);
        }
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
        return Collections.unmodifiableList(properties);
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
        for (final Property property : properties) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Creates an argument list for generating an archetype using the configuration test data.
     * 
     * @return List of arguments for the 'archetype:generate' Maven command.
     */
    public final List<String> toArchetypeGenerateArgs() {
        final List<String> args = new ArrayList<>();
        args.add("-DarchetypeGroupId=" + groupId);
        args.add("-DarchetypeArtifactId=" + artifactId);
        args.add("-DarchetypeVersion=" + version);
        if (properties != null) {
            for (final Property property : properties) {
                args.add("-D" + property.getName() + "=" + property.getTestValue());
            }
            args.add("-DinteractiveMode=false");
        }
        return args;
    }

}
