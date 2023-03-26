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
import java.util.List;

import org.apache.maven.shared.verifier.VerificationException;
import org.apache.maven.shared.verifier.Verifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link MarchetyperMojo}.
 */
public class MarchetyperMojoTest {

    private static final String FS = File.separator;
    
    // CHECKSTYLE:OFF Test

    private static final String DIV = "===================================";

    private static final File TEST_DIR = new File("target/test-classes/test-project");

    private Verifier verifier;

    @BeforeEach
    public void setup() throws Exception {
        verifier = new Verifier(TEST_DIR.getAbsolutePath());
        verifier.deleteArtifacts("org.fuin.marchetyper", "marchetyper-test-project", "0.0.1");
    }

    @Test
    public void testMojo() throws VerificationException {

        // PREPARE

        // TEST
        verifier.addCliArguments( "org.fuin.marchetyper:marchetyper-maven-plugin:generate", "-X" );
        verifier.execute();

        // VERIFY
        System.out.println(DIV + " PLUGIN OUTPUT BEGIN " + DIV);
        final List<String> lines = verifier.loadFile(verifier.getBasedir(), verifier.getLogFileName(), false);
        for (final String line : lines) {
            System.out.println(line);
        }
        System.out.println(DIV + " PLUGIN OUTPUT END " + DIV);
        verifier.verifyErrorFreeLog();

        verifier.verifyTextInLog("Using config file:");
        verifier.verifyTextInLog("Copy text example" + FS + "pom.xml to archetype" + FS + "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + "pom.xml");
        verifier.verifyTextInLog("Copy text example" + FS + "src" + FS + "test" + FS + "java" + FS + "org" + FS + "fuin" + FS + "examples" + FS + "app" + FS + "ExampleAppTest.java to archetype" + FS + "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + "src" + FS + "test" + FS + "java" + FS + "__pkgPath__" + FS + "__appName__Test.java");
        verifier.verifyTextInLog("Copy text example" + FS + "src" + FS + "main" + FS + "java" + FS + "org" + FS + "fuin" + FS + "examples" + FS + "app" + FS + "ExampleApp.java to archetype" + FS + "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + "src" + FS + "main" + FS + "java" + FS + "__pkgPath__" + FS + "__appName__.java");
        verifier.verifyTextInLog("Copy text example" + FS + "README.md to archetype" + FS + "src" + FS + "main" + FS + "resources" + FS + "archetype-resources" + FS + "README.md");

    }

    // CHECKSTYLE:OFF Test

}
