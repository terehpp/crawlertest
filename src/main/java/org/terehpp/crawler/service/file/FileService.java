package org.terehpp.crawler.service.file;

import java.util.Optional;

/**
 * Service to work with files.
 */
public interface FileService {

    /**
     * Check if file is already open.
     *
     * @param file File.
     * @return Result of check.
     */
    boolean isFileClosed(String file);

    /**
     * Remove file to another directory.
     *
     * @param sourceFile File to remove.
     * @param destDir    Destination directory.
     * @return Check if removing was successfully.
     */
    boolean removeFileTo(String sourceFile, String destDir);

    /**
     * Check if file is exist.
     *
     * @param fileName File.
     * @return Result of check.
     */
    boolean exist(String fileName);
}
