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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.common.NotEmpty;
import org.fuin.objects4j.common.Nullable;

/**
 * Maven archetype property.
 */
@XmlRootElement(name = "property")
public final class Property {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "default")
    private String defaultValue;

    @XmlAttribute(name = "test")
    private String testValue;

    /**
     * JAXB constructor.
     */
    protected Property() {
        super();
    }

    /**
     * Constructor with mandatory fields.
     * 
     * @param name
     *            Name of the property.
     * @param defaultValue
     *            Default value.
     */
    public Property(@NotEmpty final String name, @Nullable final String defaultValue) {
        this(name, defaultValue, null);
    }

    /**
     * Constructor with mandatory fields.
     * 
     * @param name
     *            Name of the property.
     * @param defaultValue
     *            Default value.
     * @param testValue
     *            Value used for a comparison test.
     */
    public Property(@NotEmpty final String name, @Nullable final String defaultValue, @Nullable final String testValue) {
        super();
        Contract.requireArgNotEmpty("name", name);
        this.name = name;
        this.defaultValue = defaultValue;
        this.testValue = testValue;
    }

    /**
     * Returns the name.
     * 
     * @return Name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the default value.
     * 
     * @return Default value.
     */
    public final String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the test value.
     * 
     * @return Value used for a comparison test.
     */
    public final String getTestValue() {
        return testValue;
    }

    void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {
        if (defaultValue != null) {
            this.defaultValue = defaultValue.replace("\\r", "\r");
            this.defaultValue = defaultValue.replace("\\n", "\n");
            this.defaultValue = defaultValue.replace("\\t", "\t");
        }
        if (testValue != null) {
            this.testValue = testValue.replace("\\r", "\r");
            this.testValue = testValue.replace("\\n", "\n");
            this.testValue = testValue.replace("\\t", "\t");
        }
    }

}
