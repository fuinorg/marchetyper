/**
 * Copyright (C) 2023 Future Invent IT Consulting GmbH. All rights reserved.
 * http://www.fuin.org/
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.marchetyper.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * Test for the {@link DotFileMapper} class.
 */
class DotFileMapperTest {

    @Test
    void testMap() {

        assertThat(new DotFileMapper(file -> file).map(null)).isEqualTo(null);
        assertThat(new DotFileMapper(file -> file).map(new File("abc.txt"))).isEqualTo(new File("abc.txt"));
        assertThat(new DotFileMapper(file -> file).map(new File("abc."))).isEqualTo(new File("abc."));
        assertThat(new DotFileMapper(file -> file).map(new File(".abc"))).isEqualTo(new File("_.abc"));

        assertThat(new DotFileMapper(file -> new File("d/e/f/.abc")).map(new File("a/b/c/.abc"))).isEqualTo(new File("d/e/f/_.abc"));

    }

}
