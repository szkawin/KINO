package model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class ScreeningTests {

    private static Configuration config;
    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void setup() {
        config = new Configuration().configure("model/hibernate-test.cfg.xml");
        sessionFactory = config.buildSessionFactory();
    }

    @AfterClass
    public static void tearDown() {
        sessionFactory.close();
    }

    private List<Movie> prepareMovies(Session session, int count) {
        session.beginTransaction();
        List<Movie> movies = session.createQuery("from Movie", Movie.class).list();
        if (movies.size() < count) {
            for (int m = movies.size(); m < count; m++) {
                session.save(new Movie("Movie-" + Integer.toString(m), 2000 + m, Movie.MppaRating.PG13, 60 + m, "Test"));
            }
            movies = session.createQuery("from Movie", Movie.class).list();
        }
        session.getTransaction().commit();
        return movies;
    }

    private List<Room> prepareRooms(Session session, int count) {
        session.beginTransaction();
        List<Room> rooms = session.createQuery("from Room", Room.class).list();
        if (rooms.size() < count) {
            for (int r = rooms.size(); r < count; r++) {
                session.save(new Room(1 + r, 100, Room.Type.NORMAL));
            }
            rooms = session.createQuery("from Room", Room.class).list();
        }
        session.getTransaction().commit();
        return rooms;
    }

    private void cleanScreenings(){
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Number count = (Number)session.createQuery("select count (*) from Screening").getSingleResult();
            if (count.intValue()>0) {
                session.createQuery("delete from Screening").executeUpdate();
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            Assume.assumeNoException("Nie da się wyczyścić tablicy",e);
        }
    }

    @Test
    public void screening_table() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            List<Screening> screenings = session.createQuery("from Screening", Screening.class).list();
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void single_screening() {
        cleanScreenings();
        try (Session session = sessionFactory.openSession()) {
            LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusDays(1);
            List<Movie> movies = prepareMovies(session, 1);
            List<Room> rooms = prepareRooms(session, 1);

            session.beginTransaction();
            Screening screening = new Screening(start, rooms.get(0), movies.get(0));
            session.save(screening);
            session.getTransaction().commit();

            session.beginTransaction();
            List<Screening> screenings = session.createQuery("from Screening", Screening.class).list();
            session.getTransaction().commit();

            Assert.assertEquals(1, screenings.size());
            Assert.assertEquals(screening, screenings.get(0));
            Assert.assertEquals(start, screenings.get(0).getStart());
            Assert.assertEquals(rooms.get(0), screenings.get(0).getRoom());
            Assert.assertEquals(movies.get(0), screenings.get(0).getMovie());

        } catch (Exception e) {
            throw new AssertionError(e);
        }

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            List<Screening> screenings1 = session.createQuery("from Screening", Screening.class).list();
            session.remove(screenings1.get(0));
            session.getTransaction().commit();
            Assert.assertEquals(1, screenings1.size());

            session.beginTransaction();
            List<Screening> screenings2 = session.createQuery("from Screening", Screening.class).list();
            session.getTransaction().commit();
            Assert.assertEquals(0, screenings2.size());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void multiple_screenings() {
        cleanScreenings();
        try (Session session = sessionFactory.openSession()) {
            LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusDays(1);

            List<Movie> movies = prepareMovies(session, 1);
            List<Room> rooms = prepareRooms(session, 2);

            session.beginTransaction();
            session.save(new Screening(start, rooms.get(0), movies.get(0)));
            // Drugi seans w tej samej sali o innej godzinie
            session.save(new Screening(start.plusHours(4), rooms.get(0), movies.get(0)));
            // Drugi seans o tej samej godzinie w innej sali
            session.save(new Screening(start, rooms.get(1), movies.get(0)));
            session.getTransaction().commit();

            session.beginTransaction();
            List<Screening> screenings = session.createQuery("from Screening", Screening.class).list();
            session.getTransaction().commit();
            Assert.assertEquals(3, screenings.size());

        } catch (Exception e) {
            throw new AssertionError(e);
        }

        // Kasujemy seanse
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            List<Screening> screenings = session.createQuery("from Screening", Screening.class).list();
            for (Screening screening : screenings) {
                session.remove(screening);
            }
            session.getTransaction().commit();

            session.beginTransaction();
            int screenings_size = session.createQuery("from Screening", Screening.class).list().size();
            session.getTransaction().commit();
            Assert.assertEquals(0, screenings_size);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test(expected=ConstraintViolationException.class)
    public void room_overbooking() {
        cleanScreenings();
        try (Session session = sessionFactory.openSession()) {
            LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusDays(1);
            List<Movie> movies = prepareMovies(session, 2);
            List<Room> rooms = prepareRooms(session, 2);

            // Dwa seanse o tej samej godzinie w tej samej sali ale różne filmy
            // Nie można mieć dwóch seansów jednocześnie w tej samej sali
            session.beginTransaction();
            session.save(new Screening(start, rooms.get(0), movies.get(0)));
            session.save(new Screening(start, rooms.get(0), movies.get(1)));
            session.getTransaction().commit();

        } catch (Exception e) {
            throw e; // tu powinien być ConstraintViolationException
        }
    }

    @Test
    public void deleting_screening() {
        cleanScreenings();
        try (Session session = sessionFactory.openSession()) {
            LocalDateTime start1 = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusDays(1);
            LocalDateTime start2 = start1.plusHours(3);
            LocalDateTime start3 = start2.plusHours(3);

            List<Movie> movies = prepareMovies(session, 3);
            List<Room> rooms = prepareRooms(session, 3);

            session.beginTransaction();
            session.save(new Screening(start1, rooms.get(0), movies.get(0)));
            session.save(new Screening(start2, rooms.get(0), movies.get(1)));
            session.save(new Screening(start3, rooms.get(0), movies.get(2)));
            session.save(new Screening(start1, rooms.get(1), movies.get(0)));
            session.save(new Screening(start2, rooms.get(1), movies.get(1)));
            session.save(new Screening(start3, rooms.get(1), movies.get(2)));
            session.save(new Screening(start1, rooms.get(2), movies.get(0)));
            session.save(new Screening(start2, rooms.get(2), movies.get(1)));
            session.save(new Screening(start3, rooms.get(2), movies.get(2)));
            session.getTransaction().commit();

            // Czy mamy 9 seansów?
            session.beginTransaction();
            Number count1 = (Number) (session.createQuery("select count(*) from Screening").list().get(0));
            session.getTransaction().commit();
            Assert.assertEquals(9, count1.intValue());

            // Skasowanie seansu nie powino skasować sali ani filmu
            session.beginTransaction();
            List<Screening> screenings = session.createQuery("from Screening",Screening.class).list();
            for(Screening screening:screenings){
                session.remove(screenings.get(0));
            }
            session.getTransaction().commit();

            session.beginTransaction();
            Number movieCount = (Number) session.createQuery("select count(*) from Movie").getSingleResult();
            Number roomCount = (Number) session.createQuery("select count(*) from Room").getSingleResult();
            session.getTransaction().commit();

            Assert.assertEquals(movies.size(), movieCount.intValue());
            Assert.assertEquals(rooms.size(), roomCount.intValue());

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void deleting_movie() {
        cleanScreenings();
        try (Session session = sessionFactory.openSession()) {
            LocalDateTime start1 = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusDays(1);
            LocalDateTime start2 = start1.plusHours(3);
            LocalDateTime start3 = start2.plusHours(3);

            List<Movie> movies = prepareMovies(session, 3);
            List<Room> rooms = prepareRooms(session, 3);

            session.beginTransaction();
            session.save(new Screening(start1, rooms.get(0), movies.get(0)));
            session.save(new Screening(start2, rooms.get(0), movies.get(1)));
            session.save(new Screening(start3, rooms.get(0), movies.get(2)));
            session.save(new Screening(start1, rooms.get(1), movies.get(0)));
            session.save(new Screening(start2, rooms.get(1), movies.get(1)));
            session.save(new Screening(start3, rooms.get(1), movies.get(2)));
            session.save(new Screening(start1, rooms.get(2), movies.get(0)));
            session.save(new Screening(start2, rooms.get(2), movies.get(1)));
            session.save(new Screening(start3, rooms.get(2), movies.get(2)));
            session.getTransaction().commit();

            // Czy mamy 9 seansów?
            session.beginTransaction();
            Number count1 = (Number) session.createQuery("select count(*) from Screening").getSingleResult();
            session.getTransaction().commit();
            Assert.assertEquals(9, count1.intValue());

            // Skasowanie filmu powino skasować seanse
            session.beginTransaction();
            session.remove(movies.get(2));
            Number count2 = (Number) session.createQuery("select count(*) from Screening").getSingleResult();
            session.getTransaction().commit();
            Assert.assertEquals(6, count2.intValue());

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void deleting_room() {
        cleanScreenings();
        try (Session session = sessionFactory.openSession()) {
            LocalDateTime start1 = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusDays(1);
            LocalDateTime start2 = start1.plusHours(3);
            LocalDateTime start3 = start2.plusHours(3);

            List<Movie> movies = prepareMovies(session, 3);
            List<Room> rooms = prepareRooms(session, 3);

            session.beginTransaction();
            session.save(new Screening(start1, rooms.get(0), movies.get(0)));
            session.save(new Screening(start2, rooms.get(0), movies.get(1)));
            session.save(new Screening(start3, rooms.get(0), movies.get(2)));
            session.save(new Screening(start1, rooms.get(1), movies.get(0)));
            session.save(new Screening(start2, rooms.get(1), movies.get(1)));
            session.save(new Screening(start3, rooms.get(1), movies.get(2)));
            session.save(new Screening(start1, rooms.get(2), movies.get(0)));
            session.save(new Screening(start2, rooms.get(2), movies.get(1)));
            session.save(new Screening(start3, rooms.get(2), movies.get(2)));
            session.getTransaction().commit();

            // Czy mamy 9 seansów?
            session.beginTransaction();
            Number count1 = (Number) session.createQuery("select count(*) from Screening").getSingleResult();
            session.getTransaction().commit();
            Assert.assertEquals(9, count1.intValue());

            // Skasowanie sali powino skasować seanse
            session.beginTransaction();
            session.remove(rooms.get(2));
            Number count2 = (Number) session.createQuery("select count(*) from Screening").getSingleResult();
            session.getTransaction().commit();
            Assert.assertEquals(6, count2.intValue());

            // Skasowanie pozostałych sal powino skasować pozostałe seanse
            session.beginTransaction();
            session.createQuery("delete from Room").executeUpdate();
            Number count3 = (Number)session.createQuery("select count(*) from Screening").getSingleResult();
            session.getTransaction().commit();
            Assert.assertEquals(0, count3.intValue());

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
