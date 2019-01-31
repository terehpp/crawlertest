package org.terehpp.crawler.service.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.file.Paths;

/**
 * Service to work with files.
 */
public class FileServiceImpl implements FileService {
    private final static Log logger = LogFactory.getLog(FileServiceImpl.class);

    /**
     * Check if file is already open.
     *
     * @param fileName File.
     * @return Result of check.
     */
    @Override
    public boolean isFileClosed(String fileName) {
        synchronized (FileServiceImpl.class) {
            File file = new File(fileName);
            File renameFile = new File(fileName + "crawlerLock");
            boolean result = file.renameTo(renameFile);
            return result && renameFile.renameTo(file);
        }
    }

    /**
     * Check if file is exist.
     *
     * @param fileName File.
     * @return Result of check.
     */
    @Override
    public boolean exist(String fileName) {
        return new File(fileName).exists();
    }

    /**
     * Remove file to another directory.
     *
     * @param sourceFile File to remove.
     * @param destDir    Destination directory.
     * @return Check if removing was successfully.
     */
    public boolean removeFileTo(String sourceFile, String destDir) {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            File source = new File(sourceFile);
            File dest = new File(String.valueOf(Paths.get(destDir.trim(), source.getName().trim())));

            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();

            return source.delete();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
