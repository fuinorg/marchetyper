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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FileUtils;
import org.fuin.objects4j.common.ConstraintViolationException;
import org.fuin.objects4j.common.Contract;

/**
 * Compares two directories.
 */
public final class DirectoryCompare {

    private final Config config;

    /**
     * Constructor with mandatory data.
     * 
     * @param config
     *            Configuration.
     */
    public DirectoryCompare(final Config config) {
        super();
        this.config = config;
    }

    /**
     * Compares multiple directory pairs.
     * 
     * @param srcPaths
     *            Source path to compare.
     * @param destPaths
     *            Destination path to compare.
     * @param log
     *            Result log.
     */
    public final void compare(final Path[] srcPaths, final Path[] destPaths, final StringBuilder log) {

        Contract.requireArgNotNull("srcPaths", srcPaths);
        Contract.requireArgNotNull("destPaths", destPaths);
        Contract.requireArgNotNull("log", log);
        if (srcPaths.length != destPaths.length) {
            throw new ConstraintViolationException(
                    "The number of 'srcPaths' (" + srcPaths.length + ") does not match the 'destPaths' count (" + destPaths.length + ")");
        }

        for (int i = 0; i < srcPaths.length; i++) {
            final Path srcDir = srcPaths[i];
            final Path destDir = destPaths[i];
            compare(srcDir, destDir, log);
        }
    }

    /**
     * Compares a single directory pair.
     * 
     * @param srcPathRelative
     *            Source directory.
     * @param destPathRelative
     *            Destination directory.
     * @param log
     *            Result log.
     */
    public final void compare(final Path srcPathRelative, final Path destPathRelative, final StringBuilder log) {

        Contract.requireArgNotNull("srcPathRelative", srcPathRelative);
        Contract.requireArgNotNull("destPathRelative", destPathRelative);
        Contract.requireArgNotNull("log", log);

        try {

            final Path srcPath = srcPathRelative.toFile().getCanonicalFile().toPath();
            final Path destPath = destPathRelative.toFile().getCanonicalFile().toPath();

            Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    if (Files.isHidden(dir) || !Files.isReadable(dir) || dir.getFileName() == null) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path srcFile, final BasicFileAttributes attrs) throws IOException {
                    try {
                        if (Files.isHidden(srcFile) || !Files.isReadable(srcFile) || !include(srcFile)) {
                            return FileVisitResult.CONTINUE;
                        }

                        final Path relativePath = srcPath.relativize(srcFile);
                        final Path destFile = destPath.resolve(relativePath);
                        if (Files.exists(destFile)) {
                            if (Files.size(srcFile) == Files.size(destFile)) {
                                if (!FileUtils.contentEquals(srcFile.toFile(), destFile.toFile())) {
                                    log.append("CONTENT DIFF: Source File=" + srcFile + ", Dest File=" + destFile + "\n");
                                }
                            } else {
                                log.append("SIZE DIFF: Source=" + Files.size(srcFile) + ", Dest=" + Files.size(destFile) + " [Source File="
                                        + srcFile + ", Dest File=" + destFile + "]\n");
                            }
                        } else {
                            log.append("DEST FILE NOT FOUND: Source File=" + srcFile + ", Dest File=" + destFile + "\n");
                        }
                        return FileVisitResult.CONTINUE;
                    } catch (final RuntimeException ex) {
                        log.append("ERROR PROCESSING SRC FILE: " + srcFile + "\n");
                        return FileVisitResult.CONTINUE;
                    }
                }
            });

        } catch (final IOException ex) {
            throw new RuntimeException("Error comparing directories", ex);
        }

    }

    private boolean include(final Path filePath) {
        final File file = filePath.toFile();
        if (config.includes(file)) {
            return true;
        }
        return !config.excludes(file);
    }

    /**
     * Test method.
     * 
     * @param args
     *            List of directories to compare.
     * 
     * @throws IOException
     *             Error reading the file system.
     */
    public static void main(final String[] args) throws IOException {

        final FileSystem fs = FileSystems.getDefault();
        if (args == null || args.length == 0) {
            System.err.println("Please provide directories to compare");
            System.exit(0);
        }
        if (args.length % 2 != 0) {
            System.err.println("Please provide directories to compare as 'SRC_DIR' 'DEST_DIR' combinations");
            System.exit(1);
        }

        int count = 0;
        final Path[] srcPaths = new Path[args.length / 2];
        final Path[] destPaths = new Path[args.length / 2];
        for (int i = 0; i < args.length; i = i + 2) {
            srcPaths[count] = fs.getPath(args[i]);
            destPaths[count] = fs.getPath(args[i + 1]);
            count++;
        }

        final StringBuilder log = new StringBuilder();
        new DirectoryCompare(ConfigImpl.load(new File(".jee7-rest-swagger-quickstart.xml"))).compare(srcPaths, destPaths, log);
        if (log.length() == 0) {
            System.out.println("No differences");
        } else {
            System.out.println("Differences found:");
            System.out.println(log);
        }

    }

}
