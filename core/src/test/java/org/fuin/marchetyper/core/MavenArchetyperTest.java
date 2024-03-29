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
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link MavenArchetyper}.
 */
public class MavenArchetyperTest {

    @Test
    public void testGenerate() throws IOException {

        // PREPARE
        final File sourceTestProject = new File("../test/src/test/resources/test-project");
        final File targetTestProject = new File("target/test-project");
        FileUtils.copyDirectory(sourceTestProject, targetTestProject);
        final Config config = ConfigImpl.load(new File("src/test/resources/marchetyper-config.xml"));

        // TEST
        new MavenArchetyper(config).generate(targetTestProject);

        // VERIFY
        final File pomXmlFile = new File(targetTestProject, "archetype/pom.xml");
        final String actualXml = FileUtils.readFileToString(pomXmlFile, StandardCharsets.UTF_8);
        final File customPomFile = new File(targetTestProject, "marchetyper/custom-pom.xml");
        final String expectedXml = FileUtils.readFileToString(customPomFile, StandardCharsets.UTF_8).replace("((ARCHETYPE_VERSION))",
                "0.1.0-SNAPSHOT");

        assertThat(actualXml).isEqualTo(expectedXml);

    }

}
