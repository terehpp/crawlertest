package org.terehpp.crawler.service.db;

/**
 * Service to work with entity as db entity.
 *
 * @param <T> Entity type.
 */
public interface DbService<T> {
    /**
     * Insert to db.
     *
     * @param entity Entity to insert.
     * @return Entity.
     */
    T insert(T entity);

    /**
     * Check if entity exist by id.
     *
     * @param id Identifier.
     * @return Result of check.
     */
    boolean exist(long id);

    /**
     * Get next id from sequence.
     *
     * @return Nex identifier.
     */
    long getNextId();
}
