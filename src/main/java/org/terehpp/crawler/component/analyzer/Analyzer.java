package org.terehpp.crawler.component.analyzer;

/**
 * File analyzer.
 *
 * @param <T> Type of entity.
 */
public interface Analyzer<T> {
    /**
     * Analyze.
     *
     * @param file File.
     * @param id   Entity identifier.
     * @return Entity.
     */
    AnalyzerResult<T> analyze(String file, Long id);
}
