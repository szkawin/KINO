package model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;


public class MovieTests {

    protected static Configuration config;
    protected static SessionFactory sessionFactory;

    @BeforeClass
    public static void setup() {
        config = new Configuration().configure("model/hibernate-test.cfg.xml");
        sessionFactory = config.buildSessionFactory();
    }

    @AfterClass
    public static void tearDown() {
        sessionFactory.close();
    }

    @Test
    public void test0Movies() {
        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            List re = ses.createQuery("select count(*) from Movie").list();
            Number val = (Number) re.get(0);
            ses.getTransaction().commit();
            Assert.assertEquals(0, val.intValue());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void test2Movies() {
        Movie theProducers1967 = new Movie("The Producers",1967,Movie.MppaRating.PG13,89,"Comedy,musical");
        Movie theProducers2005 = new Movie("The Producers",2005,Movie.MppaRating.PG13,134,"Comedy,musical");

        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            ses.save(theProducers1967);
            ses.save(theProducers2005);
            ses.getTransaction().commit();

            ses.beginTransaction();
            List<Movie> re = ses.createQuery("from Movie as m order by m.year asc", Movie.class).list();
            ses.getTransaction().commit();

            Assert.assertEquals(2,re.size());
            Assert.assertEquals(theProducers1967,re.get(0));
            Assert.assertEquals(theProducers2005,re.get(1));
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            List re = ses.createQuery("select count(*) from Movie").list();
            Number val = (Number) re.get(0);
            ses.getTransaction().commit();
            Assert.assertEquals(2, val.intValue());
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        // Czy nadal tam są?
        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            List<Movie> re = ses.createQuery(
                    "from Movie as m where m.title = 'The Producers' order by m.year asc", Movie.class).list();
            ses.getTransaction().commit();
            Assert.assertEquals(2,re.size());

            Movie m0 = re.get(0);
            // Identyfikacja obiektu przez .equals() działa tylko w obrębie jednej sesji hibernate.
            // Dlatego Assert.assertEquals(theProducers1967,m0) nie działa.
            Assert.assertEquals(theProducers1967.getId(), m0.getId());
            Assert.assertEquals(theProducers1967.getTitle(), m0.getTitle());
            Assert.assertEquals(theProducers1967.getYear(), m0.getYear());
            Assert.assertEquals(theProducers1967.getMpaaRating(), m0.getMpaaRating());
            Assert.assertEquals(theProducers1967.getDuration(), m0.getDuration());
            Assert.assertEquals(theProducers1967.getDescription(), m0.getDescription());

            Movie m1 = re.get(1);
            Assert.assertEquals(theProducers2005.getId(), m1.getId());
            Assert.assertEquals(theProducers2005.getTitle(), m1.getTitle());
            Assert.assertEquals(theProducers2005.getYear(), m1.getYear());
            Assert.assertEquals(theProducers2005.getMpaaRating(), m1.getMpaaRating());
            Assert.assertEquals(theProducers2005.getDuration(), m1.getDuration());
            Assert.assertEquals(theProducers2005.getDescription(), m1.getDescription());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
