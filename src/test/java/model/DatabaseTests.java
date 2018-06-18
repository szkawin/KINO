package model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by pwilkin on 19-Apr-18.
 */
public class DatabaseTests {

    protected static Configuration config;
    protected static SessionFactory sessionFactory;

    @BeforeClass
    public static void setup() {
        config = new Configuration().configure("model/hibernate-test.cfg.xml");
        sessionFactory = config.buildSessionFactory();
    }

    @Test
    public void testDbConnection() {
        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            ses.getTransaction().commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        sessionFactory.close();
    }

}
