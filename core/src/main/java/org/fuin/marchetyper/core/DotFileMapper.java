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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps file names starting with a '.' to a file name starting with '_.'.
 */
public final class DotFileMapper implements PathMapper {

    private static final Logger LOG = LoggerFactory.getLogger(DotFileMapper.class);

    private final PathMapper delegate;

    /**
     * Constructor with delegate.
     * 
     * @param delegate
     *            Mapper to wrap.
     */
    public DotFileMapper(PathMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public File map(File file) {
        if (file == null) {
            return null;
        }
        File result = delegate.map(file);
        if (result.getName().startsWith(".")) {
            LOG.info("Applying ARCHETYPE-505 workaround to: {}", result);
            result = new File(result.getParentFile(), "_" + result.getName());
        }
        return result;
    }

}
