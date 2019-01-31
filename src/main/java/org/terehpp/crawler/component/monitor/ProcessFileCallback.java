package org.terehpp.crawler.component.monitor;

import java.nio.file.Path;

/**
 * Functional interface to process file.
 */
public interface ProcessFileCallback {
    /**
     * Process file
     *
     * @param path Path to file.
     */
    void processFile(Path path);
}
