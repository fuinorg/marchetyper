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

import javax.validation.constraints.NotNull;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.fuin.objects4j.common.ConstraintViolationException;
import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.common.NotEmpty;

/**
 * Variable that holds a transformation of a source property.
 */
@XmlRootElement(name = "variable")
public final class Variable {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "source")
    private String source;

    @XmlAttribute(name = "transform")
    private String transform;

    @XmlTransient
    private TransformationType type;

    /**
     * JAXB constructor.
     */
    protected Variable() {
        super();
    }

    /**
     * Constructor with mandatory fields.
     * 
     * @param name
     *            Name of the variable.
     * @param source
     *            Source property.
     * @param type
     *            Transformation type.
     */
    public Variable(@NotEmpty final String name, @NotEmpty final String source, @NotNull final TransformationType type) {
        super();
        Contract.requireArgNotEmpty("name", name);
        Contract.requireArgNotEmpty("source", source);
        Contract.requireArgNotNull("type", type);
        this.name = name;
        this.source = source;
        this.type = type;
        this.transform = type.name();
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
     * Returns the source property name.
     * 
     * @return Source property.
     */
    public final String getSource() {
        return source;
    }

    /**
     * Returns the transformation type.
     * 
     * @return Transformation type.
     */
    public final TransformationType getTransformation() {
        return type;
    }

    void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {
        if (name == null) {
            throw new ConstraintViolationException("A 'name' attribute is required [" + name + "]");
        }
        if (source == null) {
            throw new ConstraintViolationException("A 'source' attribute is required [" + name + "]");
        }
        if (transform == null) {
            throw new ConstraintViolationException("A 'transform' attribute is required [" + name + "]");
        }
        try {
            type = TransformationType.valueOf(transform);
        } catch (final IllegalArgumentException ex) {
            throw new ConstraintViolationException("The transformation '" + transform + "' does not exist [" + name + "]");
        }
    }

}
