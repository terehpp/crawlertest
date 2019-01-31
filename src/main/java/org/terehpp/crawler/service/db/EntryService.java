package org.terehpp.crawler.service.db;

import org.terehpp.crawler.model.Entry;
import org.terehpp.crawler.utils.DbHelper;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Service to work with Entry as db entity.
 *
 */
public class EntryService implements DbService<Entry> {
    private static AtomicLong seq = new AtomicLong(0);

    /**
     * Insert to db.
     *
     * @param entity Entity to insert.
     * @return Entity.
     */
    @Override
    public Entry insert(Entry entity) {
        return DbHelper.save(entity);
    }

    /**
     * Check if entity exist by id.
     *
     * @param id Identifier.
     * @return Result of check.
     */
    @Override
    public boolean exist(long id) {
        return DbHelper.exist(Entry.class, id);
    }

    /**
     * Get next id from sequence.
     *
     * @return Nex identifier.
     */
    @Override
    public long getNextId() {
        if (seq.get() == 0) {
            synchronized (EntryService.class) {
                if (seq.get() == 0) {
                    Long currentId = (Long) DbHelper.getSingleResult("select max(id) from Entry");
                    if (currentId != null) {
                        seq.set(currentId);
                    }
                }
            }
        }
        return seq.incrementAndGet();
    }
}
