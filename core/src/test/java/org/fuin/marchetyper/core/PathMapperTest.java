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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link PathMapper}.
 */
public final class PathMapperTest {

    @Test
    public void testMap() throws IOException {

        // PREPARE
        final File srcDir = new File("src/test/resources" + this.getClass().getSimpleName());
        final File targetDir = new File("target" + this.getClass().getSimpleName());
        final String targetPath = path(targetDir);
        //@formatter:off
        final PathMapper testee = new PathMapper(srcDir, targetDir,
                new Mapping("b/c", "__bc__"),
                new Mapping("e/f/g", "__efg__"),
                new Mapping("x/y/z", "__xyz__"));
        //@formatter:on

        // TEST & VERIFY

        // Valid KeyValues

        //@formatter:off
        assertThat(path(testee.map(new File(srcDir, "G.txt")))).isEqualTo(targetPath + "/G.txt");
        assertThat(path(testee.map(new File(srcDir, "a/b/c/D.txt")))).isEqualTo(targetPath + "/a/__bc__/D.txt");
        assertThat(path(testee.map(new File(srcDir, "a/b/c/d/e/f/g/G.txt")))).isEqualTo(targetPath + "/a/__bc__/d/__efg__/G.txt");
        assertThat(path(testee.map(new File(srcDir, "x/y/z/Z.txt")))).isEqualTo(targetPath + "/__xyz__/Z.txt");
        //@formatter:on

        // File inside KeyValue 'b/c' will be mapped 1:1 to target
        assertThat(path(testee.map(new File(srcDir, "a/b/B.txt")))).isEqualTo(targetPath + "/a/b/B.txt");

        // Test usage with streams

        //@formatter:off
        final List<File> result =
             Arrays.asList(new File(srcDir, "a/b/c/d/D.txt"), new File(srcDir, "x/y/z/a/Z.txt"), new File(srcDir, "a/A.txt"))
            .stream()
            .map(testee::map)
            .collect(Collectors.toList());
        //@formatter:on
        assertThat(result).containsExactly(new File(targetDir, "a/__bc__/d/D.txt"),
                new File(targetDir, "__xyz__/a/Z.txt"), new File(targetDir, "a/A.txt"));

    }

    private static String path(File file) {
        return file.toString().replace(File.separatorChar, '/');
    }

}
