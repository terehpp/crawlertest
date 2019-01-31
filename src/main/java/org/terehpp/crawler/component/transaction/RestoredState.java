package org.terehpp.crawler.component.transaction;

/**
 * Transaction restored state model.
 */
public class RestoredState {
    private TransactionState transactionState;
    private String action;

    public RestoredState(TransactionState transactionState, String action) {
        this.transactionState = transactionState;
        this.action = action;
    }

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public String getAction() {
        return action;
    }
}
