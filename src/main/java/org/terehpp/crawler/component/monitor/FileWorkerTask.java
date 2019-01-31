package org.terehpp.crawler.component.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terehpp.crawler.component.fileprocessor.FileData;
import org.terehpp.crawler.component.fileprocessor.FileState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Monitor task.
 */
public class FileWorkerTask implements Runnable {
    private final static Log logger = LogFactory.getLog(FileWorkerTask.class);
    private final String sourceDir;
    private final CompletionService service;
    private int freeThreadCount;
    private final Function<FileData, FileState> createMachine;
    private final List<String> restoreTxFiles;
    private final Function<String, Optional<FileState>> restoreMachine;

    /**
     * Constructor.
     *
     * @param monitorDir         Directory to monitor.
     * @param threadCount        Thread count.
     * @param txFilesToRestore   Old transaction files, to restore.
     * @param createStateMachine Callback to create state machine for each file.
     */
    public FileWorkerTask(String monitorDir, int threadCount, List<String> txFilesToRestore,
                          Function<FileData, FileState> createStateMachine,
                          Function<String, Optional<FileState>> restoreStateMachine) {
        service = new ExecutorCompletionService(Executors.newFixedThreadPool(threadCount));
        freeThreadCount = threadCount;
        createMachine = createStateMachine;
        sourceDir = monitorDir;
        restoreTxFiles = txFilesToRestore;
        restoreMachine = restoreStateMachine;
    }

    /**
     * Start task.
     */
    @Override
    public void run() {
        try {
            for (String oldTxFile : restoreTxFiles) {
                restoreProcessFile(oldTxFile);
            }
            restoreTxFiles.clear();
            Path start = Paths.get(sourceDir);
            Files.walkFileTree(start, new FileVisitor(this::startProcessFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process file.
     *
     * @param file File.
     */
    private void startProcessFile(Path file) {
        waitForThread();
        service.submit(() -> {
            createMachine.apply(new FileData(file.toAbsolutePath().toString(), 0, null, null)).execute();
            return true;
        });
    }

    /**
     * Restore of file processing.
     * (Single thread because of file resource racing between restore process and main processing. And compound of restoring process)
     * Anyway it could be refactored to be multithread as main file processing.
     *
     * @param transactionFile Transaction file to restore state machine.
     */
    private void restoreProcessFile(String transactionFile) {
        Optional<FileState> restoredState = restoreMachine.apply(transactionFile);
        if (restoredState.isPresent()) {
            restoredState.get().execute();
        } else {
            File txFile = new File(transactionFile);
            if (txFile.exists()) {
                txFile.delete();
            }
        }
    }

    /**
     * Wait for free thread from pool.
     */
    private void waitForThread() {
        try {
            if (freeThreadCount == 0) {
                service.take();
            } else {
                freeThreadCount--;
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
