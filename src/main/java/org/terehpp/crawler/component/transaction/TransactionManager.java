package org.terehpp.crawler.component.transaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Transaction manager.
 */
public interface TransactionManager {
    /**
     * Exec command in transaction.
     *
     * @param file     File.
     * @param actionId Acion identifier.
     * @param action   Action to execute.
     * @param <T>      Type of result.
     * @return Result, if error occured it will be Optional.empty.
     */
    <T> Optional<T> execInTransaction(String file, String actionId, Function<Void, T> action);

    /**
     * Remove transaction log file.
     *
     * @param file Source file.
     * @return Result of remove.
     */
    boolean removeLog(String file);

    /**
     * Restore old transaction.
     *
     * @param file Transaction log file.
     * @return Result of restore.
     */
    Optional<RestoredState> restore(String file);

    /**
     * Get transaction files.
     *
     * @return Files.
     */
    List<String> getTransactionFiles();
}
