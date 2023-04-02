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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ReplacingFileReader}.
 */
public final class ReplacingFileReaderTest {

    private static final String LF = System.lineSeparator();

    @Test
    public void testMap() throws IOException {

        // PREPARE
        final File file = new File("src/test/resources/" + this.getClass().getSimpleName() + ".txt");
        final List<String> lines = new ArrayList<>();

        try (final LineNumberReader testee = new LineNumberReader(
                new ReplacingFileReader.Builder(file).defaultRegExFilenameSelector(".*\\.(txt)")
                        .mapping(new Mapping("${a}", "AAA"), new Mapping("Hello", "Hallo"), new Mapping("world", "Welt")).build())) {

            // TEST
            String line;
            while ((line = testee.readLine()) != null) {
                lines.add(line);
            }
        }

        // VERIFY
        assertThat(lines).containsExactly("This is just", "Some text with a variable AAA", "Hallo, Welt");

    }

    @Test
    public void testFile() throws IOException {

        // PREPARE
        final File srcFile = new File("src/test/resources/" + this.getClass().getSimpleName() + "-original.java");
        final File expectedFile = new File("src/test/resources/" + this.getClass().getSimpleName() + "-expected.java");
        final File destFile = File.createTempFile(this.getClass().getSimpleName(), ".java");

        try (final ReplacingFileReader testee = new ReplacingFileReader.Builder(srcFile).defaultRegExFilenameSelector(".*\\.(java)")
                .mapping(new Mapping("/**" + LF + " * Copyright (C) 2023 Future Invent IT Consulting GmbH. All rights reserved. " + LF
                        + " * http://www.fuin.org/" + LF + " *" + LF
                        + " * This library is free software; you can redistribute it and/or modify it under" + LF
                        + " * the terms of the GNU Lesser General Public License as published by the Free" + LF
                        + " * Software Foundation; either version 3 of the License, or (at your option) any" + LF + " * later version." + LF
                        + " *" + LF + " * This library is distributed in the hope that it will be useful, but WITHOUT" + LF
                        + " * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS" + LF
                        + " * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more" + LF + " * details." + LF + " *"
                        + LF + " * You should have received a copy of the GNU Lesser General Public License" + LF
                        + " * along with this library. If not, see http://www.gnu.org/licenses/." + LF + " */" + LF + "",
                        "${javaDocCopyright}" + LF + ""), new Mapping("Quickstart", "${appName}"),
                        new Mapping("org.fuin.jee7restswagquick", "${pkgName}"))
                .build()) {

            try (final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), StandardCharsets.UTF_8))) {

                IOUtils.copy(testee, writer);

            }

        }

        // VERIFY
        assertThat(destFile).hasSameTextualContentAs(expectedFile);

    }

}
