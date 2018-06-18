package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Model {

    private final SessionFactory sessionFactory;
    private ObservableList<Movie> movies;
    private ObservableList<Room> rooms;
    private ObservableList<Screening> screenings;

    public Model(SessionFactory aSessionFactory) {
        sessionFactory = aSessionFactory;
        movies = load(Movie.class);
        rooms = load(Room.class);
        screenings = load(Screening.class);
    }

    public Model() {
        this(new Configuration().configure("model/hibernate.cfg.xml").buildSessionFactory());
    }

    public ObservableList<Movie> getMovies() { return movies; }
    public ObservableList<Room> getRooms() { return rooms; }
    public ObservableList<Screening> getScreenings() { return screenings; }

    public void add(Movie entity){ _add(entity); }
    public void add(Room entity){ _add(entity); }
    public void add(Screening entity){ _add(entity); }

    public void remove(Movie entity) {
        _remove(entity);
        screenings = load(Screening.class);
    }

    public void remove(Room entity) {
        _remove(entity);
        screenings = load(Screening.class);
    }
    public void remove(Screening entity){ _remove(entity); }

    public void update(Movie entity){ _update(entity); }
    public void update(Room entity){ _update(entity); }

    public void update(Screening entity) {
        _update(entity);
    }

    public void addExamples() {

        add(new Room(1, 180, Room.Type.NORMAL));
        add(new Room(2, 180, Room.Type.NORMAL));
        add(new Room(3, 120, Room.Type.NORMAL));
        add(new Room(4, 120, Room.Type.NORMAL));
        add(new Room(5, 110, Room.Type.IMAX));
        add(new Room(6, 90, Room.Type.VIP));

        add(new Movie("The Producers", 1967, Movie.MppaRating.PG13, 89, "Comedy,musical"));
        add(new Movie("The Producers", 2005, Movie.MppaRating.PG13, 134, "Comedy,musical"));
        add(new Movie("Star Wars", 1977, Movie.MppaRating.PG13, 121, "bla bla"));
        add(new Movie("The Empire Strikes Back", 1980, Movie.MppaRating.PG13, 124, "bla bla"));
        add(new Movie("Return of the Jedi", 1983, Movie.MppaRating.PG13, 132, "bla bla"));
        add(new Movie("Star Wars: The Force Awakens", 2015, Movie.MppaRating.PG13, 135, "bla bla"));
        add(new Movie("Star Wars: The Last Jedi", 2017, Movie.MppaRating.PG13, 152, "bla bla"));
        add(new Movie("Rogue One: A Star Wars Story", 2016, Movie.MppaRating.PG13, 133, "bla bla"));
        add(new Movie("Solo: A Star Wars Story", 2018, Movie.MppaRating.PG13, 135, "bla bla"));

        movies = load(Movie.class);
        rooms = load(Room.class);
    }

    private <T> ObservableList<T> load(Class<T> aClass) {
        ObservableList<T> collection = FXCollections.observableArrayList();
        // TODO: czy zamykanie sesji jest dobre - psuje leniwe ładowanie i daje org.hibernate.LazyInitializationException
        // https://javarevisited.blogspot.com/2014/04/orghibernatelazyinitializationException-Could-not-initialize-proxy-no-session-hibernate-java.html
        // Na razie fetch = FetchType.EAGER zamiast fetch = FetchType.LAZY
        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            // TODO: createCriteria jest przestarzałe. Może użyć java.persistance i javax.persistence.EntityManager'a?
            // https://docs.jboss.org/hibernate/orm/3.6/quickstart/en-US/html/hibernate-gsg-tutorial-jpa.html
            collection.addAll(ses.createCriteria(aClass).list());
            ses.getTransaction().commit();
        } catch (HibernateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.show();
        }
        return collection;
    }

    private <T> void _add(T entity) {
        ObservableList<T> collection = FXCollections.observableArrayList();
        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            ses.persist(entity);
            ses.getTransaction().commit();
        } catch (HibernateException e) { // Czy to w ogóle się tu pojawia? Raczej PersistenceException
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.show();
        }
    }

    private <T> void _remove(T entity) {
        ObservableList<T> collection = FXCollections.observableArrayList();
        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            ses.remove(entity);
            ses.getTransaction().commit();
        } catch (HibernateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.show();
        }
    }

    private <T> void _update(T entity) {
        ObservableList<T> collection = FXCollections.observableArrayList();
        try (Session ses = sessionFactory.openSession()) {
            ses.beginTransaction();
            ses.merge(entity);
            ses.getTransaction().commit();
        } catch (HibernateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.show();
        }
    }

}
