package model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModelTests {

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
    public void test_create(){
        Model model = new Model(sessionFactory);
        Assert.assertNotNull(model);
    }

    @Test
    public void test_persistence(){
        Model model = new Model(sessionFactory);
        Assert.assertEquals(0, model.getMovies().size());
        Movie movie = new Movie("The Producers",1967,Movie.MppaRating.PG13,89,"Comedy,musical");
        model.add(movie);

        model = new Model(sessionFactory);
        Assert.assertEquals(1, model.getMovies().size());
        model.remove(model.getMovies().get(0));
    }

    @Test
    public void test_movie_operations(){

        try(Session ses=sessionFactory.openSession()) {
            Number count = (Number)ses.createQuery(
                    "select count(*) from Movie as m where m.year = 2005 and m.title = 'The Producers'").getSingleResult();
            Assert.assertEquals(0,count.intValue());
        }
        catch(Exception e){
            throw new AssertionError(e);
        }

        Model model = new Model(sessionFactory);
        Movie movie = new Movie("The Producers", 1967, Movie.MppaRating.PG13, 89, "Comedy,musical");
        model.add(movie);
        movie.setYear(2005);
        movie.setDuration(134);
        model.update(movie);

        try(Session ses=sessionFactory.openSession()) {
            Number count = (Number)ses.createQuery(
                    "select count(*) from Movie as m where m.year = 2005 and m.title = 'The Producers'").getSingleResult();
            Assert.assertEquals(1,count.intValue());
        }
        catch(Exception e){
            throw new AssertionError(e);
        }

        model.remove(movie);

        try(Session ses=sessionFactory.openSession()) {
            Number count = (Number)ses.createQuery(
                    "select count(*) from Movie as m where m.year = 2005 and m.title = 'The Producers'").getSingleResult();
            Assert.assertEquals(0,count.intValue());
        }
        catch(Exception e){
            throw new AssertionError(e);
        }
    }
}
