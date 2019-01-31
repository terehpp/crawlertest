package org.terehpp.crawler.component.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terehpp.crawler.utils.StringUtils;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transaction manager.
 */
public class TransactionManagerImpl implements TransactionManager {
    private final static Log logger = LogFactory.getLog(TransactionManagerImpl.class);
    private final String tempDir;

    public TransactionManagerImpl(String tempDirectory) {
        tempDir = tempDirectory;
    }

    /**
     * Get log writer for crrent source file.
     *
     * @param file Source file.
     * @return
     * @throws IOException
     */
    private BufferedWriter getWriter(String file) throws IOException {
        File txFile = Paths.get(tempDir, file.replace(File.separator, "_").replace(":", "_")).toFile();
        if (!txFile.exists()) {
            if (!txFile.createNewFile()) {
                throw new IOException(String.format("Could not create file %s", txFile.getName()));
            }
        }
        return new BufferedWriter(new FileWriter(txFile.getAbsoluteFile(), true));
    }

    /**
     * Write action to log.
     *
     * @param file Source file.
     * @param log  Log to write.
     * @throws IOException
     */
    private void writeToLog(String file, String log) throws IOException {
        try (BufferedWriter writer = getWriter(file)) {
            writer.append(log);
            writer.append("\n");
        }
    }

    /**
     * Exec command in transaction.
     *
     * @param file     File.
     * @param actionId Acion identifier.
     * @param action   Action to execute.
     * @param <T>      Type of result.
     * @return Result, if error occured it will be Optional.empty.
     */
    public <T> Optional<T> execInTransaction(String file, String actionId, Function<Void, T> action) {
        try {
            writeToLog(file, String.format("%s %s", TransactionState.OPEN, actionId));
            T result = action.apply(null);
            writeToLog(file, String.format("%s %s", TransactionState.CLOSE, actionId));
            return Optional.of(result);
        } catch (IOException e) {
            String errorMessage = String.format("Error while exec action %s for file %s", actionId, false);
            logger.error(errorMessage);
            logger.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Remove transaction log file.
     *
     * @param file Source file.
     * @return Result of remove.
     */
    public boolean removeLog(String file) {
        try {
            File txFile = Paths.get(tempDir.trim(), file.trim().replace(File.separator, "_").replace(":", "_")).toFile();
            return !txFile.exists() || txFile.delete();
        } catch (InvalidPathException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Get transaction files.
     *
     * @return Files.
     */
    public List<String> getTransactionFiles() {
        File[] files = new File(tempDir).listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(files).map(x -> x.getAbsoluteFile().getAbsolutePath()).collect(Collectors.toList());
    }

    /**
     * Restore old transaction.
     *
     * @param file Transaction log file.
     * @return Result of restore.
     */
    @Override
    public Optional<RestoredState> restore(String file) {
        File txFile = new File(file);
        if (!txFile.exists()) {
            return Optional.empty();
        }

        String lastLine = getLastLine(txFile);
        if (StringUtils.isBlank(lastLine)) {
            return Optional.empty();
        }

        String[] parts = lastLine.split("\\s");

        if (parts.length < 2) {
            return Optional.empty();
        }

        if (parts[0].equals(TransactionState.OPEN.name())) {
            return Optional.of(new RestoredState(TransactionState.OPEN,
                    lastLine.substring(TransactionState.OPEN.name().length() + 1)));
        }

        if (parts[0].equals(TransactionState.CLOSE.name())) {
            return Optional.of(new RestoredState(TransactionState.CLOSE,
                    lastLine.substring(TransactionState.CLOSE.name().length() + 1)));
        }

        return Optional.empty();
    }

    /**
     * Get last line of file.
     *
     * @param file File.s
     * @return Last line.
     */
    private String getLastLine(File file) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            long filePointer = fileLength;
            for (; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }
            }

            byte[] buffer = new byte[(int) (fileHandler.length() - filePointer - 1)];
            fileHandler.read(buffer);

            return new String(buffer, "UTF-8");
        } catch (Exception e) {
            logger.error("Error while getting last transaction");
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }
    }
}
