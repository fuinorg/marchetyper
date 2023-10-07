/**
 * Copyright (C) 2023 Future Invent IT Consulting GmbH. All rights
 * reserved. <http://www.fuin.org/>
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
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fuin.marchetyper.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.verifier.VerificationException;
import org.apache.maven.shared.verifier.Verifier;
import org.assertj.core.util.Arrays;
import org.fuin.marchetyper.core.Config;
import org.fuin.marchetyper.core.ConfigImpl;
import org.fuin.marchetyper.core.DirectoryCompare;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test for {@link MarchetyperMojo}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MarchetyperMojoTest {

    private static final String FS = File.separator;

    // CHECKSTYLE:OFF Test

    private static final String DIV = "===================================";

    private static final File TEST_PROJECT_DIR = new File("target/test-classes/test-project");

    @Order(1)
    @Test
    public void testMojo() throws VerificationException, IOException {

        // PREPARE
        final Verifier generateVerifier = new Verifier(TEST_PROJECT_DIR.getAbsolutePath());
        generateVerifier.deleteArtifacts("org.fuin.example-archetype");

        // 1) Build "example" module
        // 2) Generate "archetype" module (by running "marchetyper")
        // 3) Install "archetype" into local repo
        generateVerifier.addCliArguments("clean", "install");

        // TEST
        generateVerifier.execute();

        // VERIFY
        generateVerifier.verifyErrorFreeLog();
        System.out.println(DIV + " PLUGIN OUTPUT BEGIN " + DIV);
        final List<String> lines = generateVerifier.loadFile(generateVerifier.getBasedir(), generateVerifier.getLogFileName(), false);
        for (final String line : lines) {
            System.out.println(Verifier.stripAnsi(line));
        }
        System.out.println(DIV + " PLUGIN OUTPUT END " + DIV);
        generateVerifier.verifyErrorFreeLog();

        assertContainsAllFragmentsInOneLine(lines, "Using config file:",
                "test" + FS + "target" + FS + "test-classes" + FS + "test-project" + FS + "marchetyper" + FS + "marchetyper-config.xml");
        assertContainsAllFragmentsInOneLine(lines, "Applying ARCHETYPE-505 workaround to:",
                "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + ".gitignore");
        assertContainsAllFragmentsInOneLine(lines, "Copy text", "pom.xml to",
                "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + "pom.xml");
        assertContainsAllFragmentsInOneLine(lines, "Copy text",
                "src" + FS + "test" + FS + "java" + FS + "org" + FS + "fuin" + FS + "examples" + FS + "app" + FS
                        + "ExampleAppTest.java to ",
                "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + "src" + FS + "test" + FS + "java" + FS
                        + "__pkgPath__" + FS + "__appName__Test.java");
        assertContainsAllFragmentsInOneLine(lines, "Copy text",
                "src" + FS + "main" + FS + "java" + FS + "org" + FS + "fuin" + FS + "examples" + FS + "app" + FS + "ExampleApp.java to",
                "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + "src" + FS + "main" + FS + "java" + FS
                        + "__pkgPath__" + FS + "__appName__.java");
        assertContainsAllFragmentsInOneLine(lines, "Copy text", "README.md to",
                "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + "README.md");

    }

    @Order(2)
    @Test
    public void testGenerate() throws VerificationException, IOException {

        // PREPARE
        final File baseDir = new File("src/test/resources/test-project");
        final File configFile = new File(baseDir, "marchetyper/marchetyper-config.xml");
        final Config config = ConfigImpl.load(configFile);
        final File srcDir = new File(baseDir, "example");
        final File tmpDir = new File("target/" + this.getClass().getSimpleName());
        if (tmpDir.exists()) {
            FileUtils.deleteDirectory(tmpDir);
        }
        tmpDir.mkdirs();

        final Verifier generateVerifier = new Verifier(tmpDir.getAbsolutePath());
        generateVerifier.setAutoclean(false);

        final List<String> args = new ArrayList<>();
        args.add("archetype:generate");
        args.addAll(config.getArchetype().toArchetypeGenerateArgs(baseDir));
        generateVerifier.addCliArguments(args.toArray(new String[args.size()]));

        // TEST
        generateVerifier.execute();

        // VERIFY
        generateVerifier.verifyErrorFreeLog();
        new DirectoryCompare(config).verify(srcDir, tmpDir);

    }

    private static boolean assertContainsAllFragmentsInOneLine(List<String> lines, String... fragments) {
        for (final String line : lines) {
            if (containsAll(line, fragments)) {
                return true;
            }
        }
        throw new RuntimeException("Cannot find a line that contained all: " + Arrays.asList(fragments));
    }

    private static boolean containsAll(String line, String... fragments) {
        for (final String fragment : fragments) {
            if (!line.contains(fragment)) {
                return false;
            }
        }
        return true;
    }

}
