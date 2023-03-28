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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link ConfigImpl}.
 */
public final class ConfigTest {

    @Test
    public void testConstruction() throws IOException {

        // PREPARE
        final File baseDir = new File(".").getAbsoluteFile();
        final File srcDir = new File("src/test/resources" + this.getClass().getSimpleName());
        final File destDir = new File("target" + this.getClass().getSimpleName());

        // TEST
        final ConfigImpl testee = new ConfigImpl(srcDir, destDir);

        // VERIFY
        assertThat(testee.getSrcDir(baseDir).getCanonicalFile()).isEqualTo(srcDir.getCanonicalFile());
        assertThat(testee.getDestDir(baseDir).getCanonicalFile()).isEqualTo(destDir.getCanonicalFile());

    }

}
