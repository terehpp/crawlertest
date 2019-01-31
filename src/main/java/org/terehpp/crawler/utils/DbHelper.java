package org.terehpp.crawler.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.terehpp.crawler.model.Entry;

/**
 * Db helper utility class.
 */
public class DbHelper {
    private final static Log logger = LogFactory.getLog(DbHelper.class);
    private static SessionFactory sessionFactory;
    /**
     * Each thread has it's own session.
     */
    private static ThreadLocal<Session> session = new ThreadLocal<>();

    private DbHelper() {
    }

    /**
     * Initialize session factory.
     *
     * @param connectionString   Db connection string.
     * @param login              Db login.
     * @param password           Db password.
     * @param connectionPoolSize Pool size.
     */
    public static void initSessionFactory(String connectionString, String login, String password, int connectionPoolSize) {
        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", connectionString);
        configuration.setProperty("hibernate.connection.username", login);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.c3p0.max_size", Integer.toString(connectionPoolSize));
        configuration.addAnnotatedClass(Entry.class);
        sessionFactory = configuration.buildSessionFactory();
    }

    /**
     * Close session factory.
     */
    public static void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    /**
     * Save entity to db.
     *
     * @param entity Enity to save.
     * @param <T>    Entity type.
     * @return Entity.
     */
    public static <T> T save(T entity) {
        Transaction tx = getSession().getTransaction();
        try {
            tx.begin();
            getSession().save(entity);
            tx.commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            tx.rollback();
        }
        return entity;
    }

    /**
     * Check entity exist by id.
     *
     * @param entityType Entity type.
     * @param id         Identifier.
     * @param <T>        Entity type
     * @return Result of check.
     */
    public static <T> boolean exist(Class<T> entityType, long id) {
        return getSession().get(entityType, id) != null;
    }

    /**
     * Get session, if session was not init this method will do it.
     *
     * @return Session for current thread.
     */
    private static Session getSession() {
        if (session.get() == null) {
            session.set(sessionFactory.openSession());
        }
        return session.get();
    }

    /**
     * Execute query, and get single result.
     *
     * @param query Query, to execute.
     * @return Result.
     */
    public static Object getSingleResult(String query) {
        return getSession().createQuery(query).getSingleResult();
    }
}
