package org.terehpp.crawler.component.analyzer;

/**
 * Model of analyzer result.
 *
 * @param <T> Entity type.
 */
public class AnalyzerResult<T> {
    private boolean error;
    private String errorMsg;
    private T entity;

    public AnalyzerResult(boolean error, String errorMsg, T entity) {
        this.error = error;
        this.errorMsg = errorMsg;
        this.entity = entity;
    }

    public boolean isError() {
        return error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public T getEntity() {
        return entity;
    }
}
