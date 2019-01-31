package org.terehpp.crawler.component.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Custom file visitor.
 */
public class FileVisitor extends SimpleFileVisitor<Path> {
    private final static Log logger = LogFactory.getLog(FileVisitor.class);
    private final ProcessFileCallback processFileCallback;

    public FileVisitor(ProcessFileCallback callback) {
        processFileCallback = callback;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        processFileCallback.processFile(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException e)
            throws IOException {
        if (e == null) {
            return FileVisitResult.CONTINUE;
        } else {
            logger.error("Error to access file.");
            logger.error(e.getMessage(), e);
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.printf("Visiting failed for %s\n", file);
        return FileVisitResult.SKIP_SUBTREE;
    }
}
