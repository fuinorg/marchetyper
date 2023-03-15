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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.fuin.objects4j.common.Contract;
import org.fuin.utils4j.Utils4J;
import org.fuin.utils4j.fileprocessor.FileHandlerResult;
import org.fuin.utils4j.fileprocessor.FileProcessor;
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
     * Executes the application.
     */
    public final void execute() {

        try {
            final Path path = config.getDestDir().getAbsoluteFile().toPath();
            if (path.getNameCount() < 1) {
                throw new RuntimeException("Cannot delete root directory" + config.getDestDir());
            }
            FileUtils.deleteDirectory(config.getDestDir());
        } catch (final IOException ex) {
            throw new RuntimeException("Error deleting destination directory " + config.getDestDir(), ex);
        }

        final File destSrc = new File(config.getDestDir(), "src");
        final File destSrcMain = new File(destSrc, "main");
        final File destSrcMainResources = new File(destSrcMain, "resources");
        final File archetypeResources = new File(destSrcMainResources, "archetype-resources");
        final File metaInf = new File(destSrcMainResources, "META-INF");
        final File metaInfMaven = new File(metaInf, "maven");

        final Files destFiles = copyFiles(config, mappings, archetypeResources);
        createArchetypeMetadata(config.getDestDir(), metaInfMaven, config, archetypeResources, destFiles);

        if (config.isTest()) {
            testArchetype();
        }

    }

    private void testArchetype() {

        // Install archetype
        new MavenExecutor(config.getDestDir(), "clean", "install").execute();

        // Test archetype
        final File tmpDir = createArchetypeTestProjectDir();
        LOG.info("Create test project with archetype: {}", tmpDir);
        final Properties generatePops = new Properties();
        generatePops.put("archetypeGroupId", config.getArchetype().getGroupId());
        generatePops.put("archetypeArtifactId", config.getArchetype().getArtifactId());
        generatePops.put("archetypeVersion", config.getArchetype().getVersion());
        for (final Property property : config.getArchetype().getProperties()) {
            generatePops.put(property.getName(), property.getTestValue());
        }
        generatePops.put("interactiveMode", "false");
        new MavenExecutor(tmpDir, generatePops, "archetype:generate").execute();

        // Remove archetype
        final Properties purgeProps = new Properties();
        purgeProps.put("manualInclude", config.getArchetype().getGroupId() + ":" + config.getArchetype().getArtifactId());
        new MavenExecutor(config.getDestDir(), purgeProps, "dependency:purge-local-repository").execute();

        // Compare source with test archetype
        final StringBuilder log = new StringBuilder();
        final Property artifactProperty = config.getArchetype().findProperty("artifactId");
        final File targetDir = new File(tmpDir, artifactProperty.getTestValue());
        new DirectoryCompare(config).compare(config.getSrcDir().toPath(), targetDir.toPath(), log);
        if (log.length() != 0) {
            throw new IllegalStateException("Differences found:\n" + log);
        }

    }

    private File createArchetypeTestProjectDir() {
        final File tmpDir = new File(Utils4J.getTempDir(), "maven-archetyper-test");
        if (tmpDir.exists()) {
            try {
                FileUtils.deleteDirectory(tmpDir);
            } catch (final IOException ex) {
                throw new RuntimeException("Error creating temp directory", ex);
            }
        }
        tmpDir.mkdir();
        return tmpDir;
    }

    private void createArchetypeMetadata(final File destDir, final File metaInfMavenDir, final Config config, final File resourcesDir,
            final Files files) {

        final VelocityEngine ve = createVelocityEngine();

        final VelocityContext context = new VelocityContext();
        context.put("archetype", config.getArchetype());
        context.put("textFiles", files.textFiles);
        context.put("binaryFiles", files.binaryFiles);

        merge(ve, context, "archetype-metadata.xml", new File(metaInfMavenDir, "archetype-metadata.xml"));
        merge(ve, context, "pom.xml", new File(destDir, "pom.xml"));

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

    private static Files copyFiles(final Config config, final List<Mapping> mappings, final File archetypeResources) {

        final PathMapper pathMapper = new PathMapper(config.getSrcDir(), archetypeResources, config.getPathMappings());

        final Files files = new Files();

        allFiles(config.getSrcDir()).stream().filter((file) -> {
            if (config.includes(file)) {
                return true;
            }
            return !config.excludes(file);
        }).forEach((srcFile) -> {

            final File destFile = pathMapper.map(srcFile);
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            if (config.isBinary(srcFile)) {
                copyBinaryFile(srcFile, destFile);
                addDestFile(files.binaryFiles, archetypeResources, destFile);
            } else if (config.isText(srcFile)) {
                copyTextFile(srcFile, destFile, config.getTextFiles(), config.getVariables(), mappings);
                addDestFile(files.textFiles, archetypeResources, destFile);
            } else {
                throw new IllegalStateException("File found that is neither binary nor text file: " + srcFile);
            }

        });

        return files;
    }

    private static void addDestFile(final List<File> files, final File archetypeResourcesDir, final File file) {
        final Path filePath = file.toPath();
        final Path parentPathToStrip = archetypeResourcesDir.toPath();
        final File relativeFile = parentPathToStrip.relativize(filePath).toFile();
        files.add(relativeFile);
    }

    private static void copyBinaryFile(final File srcFile, final File destFile) {
        LOG.info("Copy binary {} to {}", srcFile, destFile);
        try {
            FileUtils.copyFile(srcFile, destFile);
        } catch (final IOException ex) {
            throw new RuntimeException("Error copying binary file from " + srcFile + " to " + destFile, ex);
        }
    }

    private static void copyTextFile(final File srcFile, final File destFile, final String textFiles, final List<Variable> variables,
            final List<Mapping> mappings) {
        LOG.info("Copy text {} to {}", srcFile, destFile);
        try (final ReplacingFileReader reader = new ReplacingFileReader(srcFile, 1024, textFiles, mappings)) {
            try (final Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(destFile), Charset.forName("utf-8")))) {
                // Add prefix
                writer.write("#set( $symbol_pound = '#' )\n");
                writer.write("#set( $symbol_dollar = '$' )\n");
                writer.write("#set( $symbol_escape = '\\' )\n");

                writer.write("#set( $delim = '.,_-/' )\n");
                writer.write("#set( $empty = '' )\n");
                writer.write("#set( $StringUtils = $empty.class.forName('org.codehaus.plexus.util.StringUtils') )\n");
                for (Variable v : variables) {
                    writer.write("#set( $" + v.getName() + " = " + v.getTransformation().getCode(v.getSource()) + " )\n");
                }
                // Replace rest
                IOUtils.copy(reader, writer);
            }
        } catch (final IOException ex) {
            throw new RuntimeException("Error copying text file from " + srcFile + " to " + destFile, ex);
        }
    }

    private static List<File> allFiles(final File dir) {
        final List<File> files = new ArrayList<>();
        new FileProcessor((file) -> {
            if (file.isFile()) {
                files.add(file);
            }
            return FileHandlerResult.CONTINUE;
        }).process(dir);
        return files;
    }

    private static class Files {

        public List<File> binaryFiles = new ArrayList<>();

        public List<File> textFiles = new ArrayList<>();

    }

}
