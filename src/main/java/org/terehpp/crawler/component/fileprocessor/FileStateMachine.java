package org.terehpp.crawler.component.fileprocessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terehpp.crawler.component.analyzer.Analyzer;
import org.terehpp.crawler.component.analyzer.AnalyzerResult;
import org.terehpp.crawler.component.transaction.RestoredState;
import org.terehpp.crawler.component.transaction.TransactionManager;
import org.terehpp.crawler.model.DbEntity;
import org.terehpp.crawler.service.db.DbService;
import org.terehpp.crawler.service.file.FileService;
import org.terehpp.crawler.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File state machine.
 */
public class FileStateMachine implements FileState {
    private FileData state;
    private List<FileStateMachineCommand> commands = new ArrayList<>();
    private final static Log logger = LogFactory.getLog(FileStateMachine.class);
    private final TransactionManager tx;
    private final String successDir;
    private final String failDir;
    private final Analyzer analyzer;
    private final DbService dbService;
    private final FileService fileService;
    private int currentCommand = 0;

    /**
     * Constructor.
     *
     * @param successDirectory   Success directory.
     * @param failDirectory      Fail directory.
     * @param fileAnalyzer       File analyzer.
     * @param dbSrv              Db service.
     * @param fileSrv            File service.
     * @param transactionManager Transaction Manager.
     */
    public FileStateMachine(String successDirectory, String failDirectory,
                            Analyzer fileAnalyzer, DbService dbSrv, FileService fileSrv,
                            TransactionManager transactionManager) {
        tx = transactionManager;
        successDir = successDirectory;
        failDir = failDirectory;
        analyzer = fileAnalyzer;
        dbService = dbSrv;
        fileService = fileSrv;
    }

    /**
     * Init state.
     *
     * @param initState       Data of state.
     * @param machineCommands Commands to execute.
     * @return
     */
    @Override
    public FileState init(FileData initState, List<FileStateMachineCommand> machineCommands) {
        currentCommand = 0;
        state = initState;
        commands = machineCommands;
        return this;
    }

    /**
     * Restore state of state machine from transaction fie.
     *
     * @param transactionFile Transaction file.
     * @param machineCommands Machine commands to execute.
     * @return State.
     */
    public Optional<FileState> restore(String transactionFile, List<FileStateMachineCommand> machineCommands) {
        commands = machineCommands;
        Optional<RestoredState> rs = tx.restore(transactionFile);
        if (rs.isPresent()) {
            String[] actionParts = rs.get().getAction().split("\\s");
            if (actionParts.length < 3) {
                return Optional.empty();
            }
            FileStateMachineCommand lastCommand = null;
            for (int i = 0; i < commands.size(); i++) {
                FileStateMachineCommand command = commands.get(i);
                if (command.name().equals(actionParts[0])) {
                    lastCommand = command;
                    currentCommand = i;
                    break;
                }
            }

            if (lastCommand == null) {
                return Optional.empty();
            }

            long id = 0;
            String moveDir = failDir;
            try {
                id = Long.parseLong(actionParts[1]);
                if (dbService.exist(id)) {
                    if (lastCommand == FileStateMachineCommand.INSERT) {
                        if (commands.size() >= (currentCommand + 1)) {
                            currentCommand++;
                            moveDir = successDir;
                        } else {
                            return Optional.empty();
                        }
                    }
                } else {
                    return Optional.empty();
                }
                StringBuilder fileNameBuilder = new StringBuilder();
                for (int i = 2; i < actionParts.length; i++) {
                    fileNameBuilder.append(actionParts[i]).append(" ");
                }
                state = new FileData(fileNameBuilder.toString(), id, null, moveDir);
                return Optional.of(this);
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Start execute commands.
     */
    @Override
    public void execute() {
        if (state == null || commands == null) {
            return;
        }
        if (!fileService.exist(state.getFile())) {
            tx.removeLog(state.getFile());
            return;
        }
        if (!fileService.isFileClosed(state.getFile())) {
            logger.warn(String.format("Could no process file %s. It's already open.", state.getFile()));
            return;
        }
        state.setId(dbService.getNextId());
        for (int i = currentCommand; i < commands.size(); i++) {
            FileStateMachineCommand command = commands.get(i);
            if (command == FileStateMachineCommand.ANALYZE) {
                AnalyzerResult analyzeRes = analyzer.analyze(state.getFile(), state.getId());
                if (!analyzeRes.isError()) {
                    state.setEntity((DbEntity) analyzeRes.getEntity());
                } else {
                    state.setEntity(null);
                    state.setMoveDirectory(failDir);
                    logger.error(String.format("Error while analyzation %s", analyzeRes.getErrorMsg()));
                }
            } else if (command == FileStateMachineCommand.INSERT) {
                if (state.getEntity() != null) {
                    Optional<Boolean> insertRes = tx.execInTransaction(state.getFile(), String.format("INSERT %s %s", state.getId(), state.getFile()), (Void) -> {
                        dbService.insert(state.getEntity());
                        return true;
                    });
                    if (insertRes.isPresent()) {
                        state.setMoveDirectory(successDir);
                    } else {
                        state.setMoveDirectory(failDir);
                    }
                }
            } else if (command == FileStateMachineCommand.MOVE) {
                if (StringUtils.isNotBlank(state.getMoveDirectory())) {
                    fileService.removeFileTo(state.getFile(), state.getMoveDirectory());
                }
                tx.removeLog(state.getFile());
            }
        }
    }
}
