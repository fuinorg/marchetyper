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
import java.util.stream.Collectors;

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
        final Files destFiles = copyFiles(srcDir, destDir, config, mappings, archetypeResources);
        createArchetypeMetadata(destDir, metaInfMaven, config, archetypeResources, destFiles);

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
            final Files files) {

        final VelocityEngine ve = createVelocityEngine();
        final VelocityContext context = new VelocityContext();
        context.put("archetype", config.getArchetype());
        context.put("textFiles", wrap(files.textFiles));
        context.put("binaryFiles", wrap(files.binaryFiles));

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

    private static Files copyFiles(final File srcDir, final File destDir, final Config config, final List<Mapping> mappings,
            final File archetypeResources) {

        final PathMapper pathMapper = new PathMapper(srcDir, archetypeResources, config.getPathMappings());

        final Files files = new Files();

        allFiles(srcDir).stream().filter((file) -> {
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
                final File targetFile = copyBinaryFile(config, srcDir, srcFile, destDir, destFile);
                addDestFile(files.binaryFiles, archetypeResources, targetFile);
            } else if (config.isText(srcFile)) {
                final File targetFile = copyTextFile(config, srcDir, srcFile, destDir, destFile,
                        config.getTextFiles(), config.getVariables(), mappings);
                addDestFile(files.textFiles, archetypeResources, targetFile);
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

    private static File maskDotFile(final Config config, final File destDir, final File destFile) {
        if (config.isMaskDotFile() && destFile.getName().startsWith(".")) {
            LOG.info("Applying ARCHETYPE-505 workaround to: {}", Utils4J.getRelativePath(destDir, destFile));
            return new File(destFile.getParentFile(), "_" + destFile.getName());
        }
        return destFile;
    }

    private static File copyBinaryFile(final Config config, final File srcDir, final File srcFile, final File destDir, final File destFileX) {
        final File destFile = maskDotFile(config, destDir, destFileX);
        LOG.info("Copy binary {} to {}", Utils4J.getRelativePath(srcDir, srcFile), Utils4J.getRelativePath(destDir, destFile));
        try {
            FileUtils.copyFile(srcFile, destFile);
            return destFile;
        } catch (final IOException ex) {
            throw new RuntimeException("Error copying binary file from " + srcFile + " to " + destFile, ex);
        }
    }

    private static File copyTextFile(final Config config, final File srcDir, final File srcFile, final File destDir, final File destFileX,
                                     final String textFiles, final List<Variable> variables, final List<Mapping> mappings) {
        final File destFile = maskDotFile(config, destDir, destFileX);
        LOG.info("Copy text {} to {}", Utils4J.getRelativePath(srcDir, srcFile), Utils4J.getRelativePath(destDir, destFile));
        try (final ReplacingFileReader reader = new ReplacingFileReader(srcFile, 1024, textFiles, mappings)) {
            try (final Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(destFile), Charset.forName("utf-8")))) {
                // Add prefix
                writer.write("#set( $symbol_pound = '#' )" + System.lineSeparator());
                writer.write("#set( $symbol_dollar = '$' )" + System.lineSeparator());
                writer.write("#set( $symbol_escape = '\\' )" + System.lineSeparator());

                writer.write("#set( $delim = '.,_-/' )" + System.lineSeparator());
                writer.write("#set( $empty = '' )" + System.lineSeparator());
                writer.write("#set( $StringUtils = $empty.class.forName('org.codehaus.plexus.util.StringUtils') )" + System.lineSeparator());
                for (Variable v : variables) {
                    writer.write("#set( $" + v.getName() + " = " + v.getTransformation().getCode(v.getSource()) + " )" + System.lineSeparator());
                }
                // Replace rest
                IOUtils.copy(reader, writer);
                return destFile;
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

    private static List<VelocityFileWrapper> wrap(List<File> files) {
        return files.stream().map(file -> new VelocityFileWrapper(file)).collect(Collectors.toList());
    }

    private static class Files {

        public List<File> binaryFiles = new ArrayList<>();

        public List<File> textFiles = new ArrayList<>();

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
