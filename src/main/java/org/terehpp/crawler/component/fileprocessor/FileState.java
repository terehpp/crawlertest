package org.terehpp.crawler.component.fileprocessor;

import java.util.*;

/**
 * File state.
 */
public interface FileState {
    /**
     * Init state.
     *
     * @param initState Data of state.
     * @param comands   Commands to execute.
     * @return
     */
    FileState init(FileData initState, List<FileStateMachineCommand> comands);

    /**
     * Start execute commands.
     */
    void execute();

    /**
     * Restore state of state machine from transaction fie.
     *
     * @param transactionFile Transaction file.
     * @param machineCommands Machine commands to execute.
     * @return State.
     */
    Optional<FileState> restore(String transactionFile, List<FileStateMachineCommand> machineCommands);
}
