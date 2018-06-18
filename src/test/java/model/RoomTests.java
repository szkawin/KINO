package model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class RoomTests {

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
    public void test0Rooms() {
        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            List re = ses.createQuery("select count(*) from Room").list();
            Number val = (Number) re.get(0);
            ses.getTransaction().commit();
            Assert.assertEquals(0, val.intValue());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void test2Rooms() {
        Room room1 = new Room(1,100,Room.Type.NORMAL);
        Room room2 = new Room(2,30,Room.Type.VIP);

        try (Session ses = sessionFactory.openSession()) {

            ses.beginTransaction();
            ses.save(room1);
            ses.save(room2);
            ses.getTransaction().commit();

            ses.beginTransaction();
            List<Room> re = ses.createQuery("from Room as r order by r.number asc", Room.class).list();
            ses.getTransaction().commit();

            Assert.assertEquals(2,re.size());
            Assert.assertEquals(room1,re.get(0));
            Assert.assertEquals(room2,re.get(1));

        } catch (Exception e) {
            throw new AssertionError(e);
        }

        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            List re = ses.createQuery("select count(*) from Room").list();
            Number val = (Number) re.get(0);
            ses.getTransaction().commit();
            Assert.assertEquals(2, val.intValue());
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        try (Session ses = sessionFactory.openSession()) {

            ses.beginTransaction();
            List<Room> re = ses.createQuery("from Room as r order by r.number asc", Room.class).list();
            ses.getTransaction().commit();

            Assert.assertEquals(2,re.size());

            Room r1 = re.get(0);
            Assert.assertEquals(room1.getId(), r1.getId());
            Assert.assertEquals(room1.getNumber(), r1.getNumber());
            Assert.assertEquals(room1.getSeats(), r1.getSeats());
            Assert.assertEquals(room1.getType(), r1.getType());

            Room r2 = re.get(1);
            Assert.assertEquals(room2.getId(), r2.getId());
            Assert.assertEquals(room2.getNumber(), r2.getNumber());
            Assert.assertEquals(room2.getSeats(), r2.getSeats());
            Assert.assertEquals(room2.getType(), r2.getType());

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}
