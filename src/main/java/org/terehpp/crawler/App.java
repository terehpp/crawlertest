package org.terehpp.crawler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terehpp.crawler.component.analyzer.Analyzer;
import org.terehpp.crawler.component.analyzer.XMLAnalyzerImpl;
import org.terehpp.crawler.component.fileprocessor.FileData;
import org.terehpp.crawler.component.fileprocessor.FileState;
import org.terehpp.crawler.component.fileprocessor.FileStateMachine;
import org.terehpp.crawler.component.fileprocessor.FileStateMachineCommand;
import org.terehpp.crawler.component.monitor.FileWorkerTask;
import org.terehpp.crawler.component.transaction.TransactionManager;
import org.terehpp.crawler.component.transaction.TransactionManagerImpl;
import org.terehpp.crawler.constants.AppPropName;
import org.terehpp.crawler.model.Entry;
import org.terehpp.crawler.service.db.EntryService;
import org.terehpp.crawler.service.file.FileServiceImpl;
import org.terehpp.crawler.utils.DbHelper;
import org.terehpp.crawler.utils.PropertyHelper;
import org.terehpp.crawler.utils.PropertyHelperException;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Crawler application.
 */
public class App {
    private final static Log logger = LogFactory.getLog(App.class);
    private final static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        Properties properties = new Properties();
        if (!PropertyHelper.initProperties(args, properties)) {
            return;
        }
        try {
            start(properties);
        } catch (PropertyHelperException e) {
            logger.error(e.getMessage(), e);
            DbHelper.closeSessionFactory();
        }
    }

    /**
     * Start application.
     *
     * @param properties Properties.
     * @throws PropertyHelperException Occurred if property does not exist.
     */
    private static void start(final Properties properties) throws PropertyHelperException {
        // Initializing services and properties, all of this could be done with IOC container
        int delay = PropertyHelper.getPositiveIntProperty(properties, AppPropName.MONITOR_SCHEDULE_TIMEOUT);
        int threadCount = PropertyHelper.getPositiveIntProperty(properties, AppPropName.PROCESSOR_MAX_THREAD_COUNT);
        String monitorDir = PropertyHelper.getPathProperty(properties, AppPropName.MONITOR_DIR, true, true, true);
        String xsdSchemaFile = PropertyHelper.getPathProperty(properties, AppPropName.ENTRY_XSD_SCHEMA_FILE, false, true, false);

        TransactionManager tx = initTxManager(properties);
        initSessionFactory(properties, threadCount);
        EntryService entryService = new EntryService();
        Analyzer<Entry> analyzer = new XMLAnalyzerImpl<>(Entry.class, xsdSchemaFile);
        List<FileStateMachineCommand> commands = Arrays.asList(FileStateMachineCommand.ANALYZE,
                FileStateMachineCommand.INSERT, FileStateMachineCommand.MOVE);

        Function<FileData, FileState> createMachine = getCreateMachineStateFunc(properties, tx, entryService, analyzer, commands);

        Function<String, Optional<FileState>> restoreMachine = getRestoreMachineStateFunc(properties, tx, entryService, analyzer, commands);

        // Monitoring task
        FileWorkerTask task = new FileWorkerTask(monitorDir, threadCount, tx.getTransactionFiles(), createMachine, restoreMachine);

        // Schedule monitoring
        executorService.scheduleAtFixedRate(task, 0, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Get function to create processor, to work on files.
     *
     * @param properties   Properties for initialization.
     * @param tx           Tx manager, to make all process transactional.
     * @param entryService Db entry service.
     * @param analyzer     File analyzer.
     * @return Processor.
     * @throws PropertyHelperException Occurred when property does not exist.
     */
    private static Function<FileData, FileState> getCreateMachineStateFunc(final Properties properties, final TransactionManager tx,
                                                                           final EntryService entryService, final Analyzer<Entry> analyzer,
                                                                           List<FileStateMachineCommand> commands) throws PropertyHelperException {
        String successDir = PropertyHelper.getPathProperty(properties, AppPropName.PROCESSOR_SUCCESS_DIR, true, true, true);
        String failDir = PropertyHelper.getPathProperty(properties, AppPropName.PROCESSOR_FAIL_DIR, true, true, true);
        return (fileData) -> {
            FileState machineState = new FileStateMachine(successDir, failDir, analyzer, entryService, new FileServiceImpl(), tx);
            return machineState.init(fileData, commands);
        };
    }

    /**
     * Get function to restore processor, to work on files.
     *
     * @param properties   Properties for initialization.
     * @param tx           Tx manager, to make all process transactional.
     * @param entryService Db entry service.
     * @param analyzer     File analyzer.
     * @return Processor.
     * @throws PropertyHelperException Occurred when property does not exist.
     */
    private static Function<String, Optional<FileState>> getRestoreMachineStateFunc(final Properties properties, final TransactionManager tx,
                                                                                    final EntryService entryService, final Analyzer<Entry> analyzer,
                                                                                    List<FileStateMachineCommand> commands) throws PropertyHelperException {
        String successDir = PropertyHelper.getPathProperty(properties, AppPropName.PROCESSOR_SUCCESS_DIR, true, true, true);
        String failDir = PropertyHelper.getPathProperty(properties, AppPropName.PROCESSOR_FAIL_DIR, true, true, true);
        return (txFile) -> {
            FileState machineState = new FileStateMachine(successDir, failDir, analyzer, entryService, new FileServiceImpl(), tx);
            return machineState.restore(txFile, commands);
        };
    }

    /**
     * Init Hibernate Session Factory.
     *
     * @param properties  Properties.
     * @param threadCount Count of thread to connect to db.
     * @throws PropertyHelperException Occurred when property does not exist.
     */
    private static void initSessionFactory(final Properties properties, int threadCount) throws PropertyHelperException {
        String connectionStr = PropertyHelper.getStrProperty(properties, AppPropName.CONNECTION_STRING);
        String login = PropertyHelper.getStrProperty(properties, AppPropName.DB_LOGIN);
        String pass = PropertyHelper.getStrProperty(properties, AppPropName.DB_PASS);
        DbHelper.initSessionFactory(connectionStr, login, pass, threadCount);
    }

    /**
     * Init tx manager.
     *
     * @param properties Properties.
     * @return
     * @throws PropertyHelperException Occurred when property does not exist.
     */
    private static TransactionManager initTxManager(final Properties properties) throws PropertyHelperException {
        String tempDir = PropertyHelper.getPathProperty(properties, AppPropName.PROCESSOR_TEMP_DIR, true, true, true);
        return new TransactionManagerImpl(tempDir);
    }
}
