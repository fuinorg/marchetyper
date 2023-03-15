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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ReplacingFileReader}.
 */
public final class ReplacingFileReaderTest {

    @Test
    public void testMap() throws IOException {

        // PREPARE
        final File file = new File("src/test/resources/" + this.getClass().getSimpleName() + ".txt");
        final List<String> lines = new ArrayList<>();

        try (final LineNumberReader testee = new LineNumberReader(
                new ReplacingFileReader(file, 1024, ".*\\.(txt)", new Mapping("${a}", "AAA"),
                        new Mapping("Hello", "Hallo"), new Mapping("world", "Welt")))) {

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
        final File destFile = File
                .createTempFile(this.getClass().getSimpleName(), ".java");

        try (final ReplacingFileReader testee = new ReplacingFileReader(srcFile, 1024, ".*\\.(java)",
                new Mapping(
                        "/**\n * Copyright (C) 2023 Future Invent IT Consulting GmbH. All rights reserved. \n * http://www.fuin.org/\n *\n * This library is free software; you can redistribute it and/or modify it under\n * the terms of the GNU Lesser General Public License as published by the Free\n * Software Foundation; either version 3 of the License, or (at your option) any\n * later version.\n *\n * This library is distributed in the hope that it will be useful, but WITHOUT\n * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\n * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more\n * details.\n *\n * You should have received a copy of the GNU Lesser General Public License\n * along with this library. If not, see http://www.gnu.org/licenses/.\n */\n",
                        "${javaDocCopyright}\n"),
                new Mapping("Quickstart", "${appName}"),
                new Mapping("org.fuin.jee7restswagquick", "${pkgName}"))) {

            try (final Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(destFile), Charset.forName("utf-8")))) {

                IOUtils.copy(testee, writer);

            }

        }

        // VERIFY
        assertThat(destFile).hasSameContentAs(expectedFile);

    }

}
