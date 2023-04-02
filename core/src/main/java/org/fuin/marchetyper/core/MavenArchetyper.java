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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.fuin.objects4j.common.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven Archtyper console application.
 */
public final class MavenArchetyper {

    private static final Logger LOG = LoggerFactory.getLogger(MavenArchetyper.class);

    private final Config config;

    private final List<Mapping> mappings;

    /**
     * Constructor with configuration.
     * 
     * @param config
     *            Configuration to use.
     */
    public MavenArchetyper(final Config config) {
        super();
        Contract.requireArgNotNull("config", config);
        this.config = config;
        mappings = new ArrayList<>();
        // Default mappings
        mappings.add(new Mapping("$", "${symbol_dollar}"));
        mappings.add(new Mapping("\\", "${symbol_escape}"));
        mappings.add(new Mapping("#", "${symbol_pound}"));
        // User defined mappings
        mappings.addAll(config.getFileMappings());
    }

    /**
     * Generated the archetype with the given base directory.
     *
     * @param baseDir
     *            Base directory.
     */
    public void generate(File baseDir) {
        LOG.info("baseDir: {}", baseDir);
        final File destDir = config.getDestDir(baseDir);
        try {
            final Path destPath = destDir.toPath();
            if (destPath.getNameCount() < 1) {
                throw new RuntimeException("Cannot delete root directory" + destDir);
            }
            FileUtils.deleteDirectory(destDir);
        } catch (final IOException ex) {
            throw new RuntimeException("Error deleting destination directory " + destDir, ex);
        }
        final File srcDir = config.getSrcDir(baseDir);
        final File customPomFile = config.getCustomPomFile(baseDir);
        final File postGenerateFile = config.getPostGenerateFile(baseDir);
        generate(customPomFile, postGenerateFile, srcDir, destDir);
    }

    /**
     * Generated the archetype with the given base directory.
     *
     * @param customPomFile
     *            Custom POM file or {@literal null} if no custom pom is available.
     * @param postGenerateFile
     *            Post generate Groovy file or {@literal null} if no post generation file is configured.
     * @param srcDir
     *            Source directory.
     * @param destDir
     *            Destination directory.
     */
    public void generate(File customPomFile, final File postGenerateFile, final File srcDir, final File destDir) {

        LOG.info("customPomFile: {}", customPomFile);
        LOG.info("postGenerateFile: {}", postGenerateFile);
        LOG.info("destDir: {}", destDir);
        LOG.info("srcDir: {}", srcDir);

        copyOrCreatePom(customPomFile, destDir);

        final File destSrc = new File(destDir, "src");
        final File destSrcMain = new File(destSrc, "main");
        final File destSrcMainResources = new File(destSrcMain, "resources");
        final File archetypeResources = new File(destSrcMainResources, "archetype-resources");
        final File metaInf = new File(destSrcMainResources, "META-INF");
        final File metaInfMaven = new File(metaInf, "maven");
        final File archetypePostGenerateGroovy = new File(metaInf, "archetype-post-generate.groovy");

        copyPostGenerateFile(postGenerateFile, archetypePostGenerateGroovy);

        PathMapper pathMapper = new SimplePathMapper(srcDir, archetypeResources, config.getPathMappings());
        if (config.isMaskDotFile()) {
            pathMapper = new DotFileMapper(pathMapper);
        }

        final FileCopy fileCopy = new FileCopy.Builder().srcBaseDir(srcDir).destBaseDir(destDir).pathMapper(pathMapper).fileMatcher(config)
                .headerProvider(createHeaderProvider(config)).defaultRegExFilenameSelector(config.getTextFiles()).mappings(mappings)
                .build();
        final FileCopyResult result = fileCopy.copy();

        createArchetypeMetadata(destDir, metaInfMaven, config, archetypeResources, result);

    }

    private static FileCopy.HeaderProvider createHeaderProvider(final Config config) {
        return (writer) -> {
            try {
                writer.write("#set( $symbol_pound = '#' )" + System.lineSeparator());
                writer.write("#set( $symbol_dollar = '$' )" + System.lineSeparator());
                writer.write("#set( $symbol_escape = '\\' )" + System.lineSeparator());

                writer.write("#set( $delim = '.,_-/' )" + System.lineSeparator());
                writer.write("#set( $empty = '' )" + System.lineSeparator());
                writer.write(
                        "#set( $StringUtils = $empty.class.forName('org.codehaus.plexus.util.StringUtils') )" + System.lineSeparator());
                for (Variable v : config.getVariables()) {
                    writer.write(
                            "#set( $" + v.getName() + " = " + v.getTransformation().getCode(v.getSource()) + " )" + System.lineSeparator());
                }
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write header", ex);
            }
        };

    }

    private void copyPostGenerateFile(File srcFile, File destFile) {
        if (srcFile != null) {
            try {
                FileUtils.copyFile(srcFile, destFile);
            } catch (final IOException ex) {
                throw new RuntimeException("Failed to copy '" + srcFile + "' to '" + destFile + "'", ex);
            }
        }
    }

    private void copyOrCreatePom(final File customPomFile, final File destDir) {

        if (customPomFile == null) {

            final VelocityEngine ve = createVelocityEngine();
            final VelocityContext context = new VelocityContext();
            context.put("archetype", config.getArchetype());

            merge(ve, context, "pom.xml", new File(destDir, "pom.xml"));

        } else {

            final File destPomFile = new File(destDir, "pom.xml");
            try {
                FileUtils.copyFile(customPomFile, destPomFile);
            } catch (final IOException ex) {
                throw new RuntimeException("Error copying custom POM from '" + customPomFile + "' to: " + destPomFile, ex);
            }

        }

    }

    private void createArchetypeMetadata(final File destDir, final File metaInfMavenDir, final Config config, final File resourcesDir,
            final FileCopyResult result) {

        final VelocityEngine ve = createVelocityEngine();
        final VelocityContext context = new VelocityContext();
        context.put("archetype", config.getArchetype());
        context.put("textFiles", wrap(result.getRelativizedTextFiles(resourcesDir)));
        context.put("binaryFiles", wrap(result.getRelativizedBinaryFiles(resourcesDir)));

        merge(ve, context, "archetype-metadata.xml", new File(metaInfMavenDir, "archetype-metadata.xml"));

    }

    private static void merge(final VelocityEngine ve, final VelocityContext context, final String template, final File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            final Template t = ve.getTemplate(template);
            try (final Writer writer = new BufferedWriter(new FileWriter(file))) {
                t.merge(context, writer);
            }
        } catch (final IOException ex) {
            throw new RuntimeException("Error writing template: " + template, ex);
        }
    }

    private static VelocityEngine createVelocityEngine() {
        final VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        return ve;
    }

    private static List<VelocityFileWrapper> wrap(List<File> files) {
        return files.stream().map(file -> new VelocityFileWrapper(file)).collect(Collectors.toList());
    }

    public static final class VelocityFileWrapper {

        private final File file;

        public VelocityFileWrapper(File file) {
            this.file = file;
        }

        public String getName() {
            return file.getName();
        }

        public String getParent() {
            if (file.getParent() == null) {
                return "";
            }
            return file.getParent().replace(File.separatorChar, '/');
        }

    }

}
