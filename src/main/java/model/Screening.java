package model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table( name = "SCREENING",
        uniqueConstraints = {
                // W jednej sali może się odbywać tylko jeden seans na raz - para start seansu i sala powiny być unikalne
                @UniqueConstraint(columnNames = {"START", "ROOM_ID"})
            }
        )
public class Screening { // To seans filmowy w wybranej sali

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "START", nullable = false)
    private LocalDateTime start; // data i czas rozpoczęcia seansu

    // https://en.wikibooks.org/wiki/Java_Persistence/ManyToOne
    // nie można użyć fetch = FetchType.LAZY bo zamykanie sesji w Model powoduje org.hibernate.LazyInitializationException
    @ManyToOne(optional = false)
    @JoinColumn(name = "ROOM_ID", nullable = false)
    private Room room; // to sala gdzie jest seans

    // tu też nie można użyć fetch = FetchType.LAZY
    @ManyToOne(optional = false)
    @JoinColumn(name = "MOVIE_ID", nullable = false)
    private Movie movie; // a to film wyświetlany w czasie seansu

    public Screening(){}

    public Screening(LocalDateTime start, Room room, Movie movie) {
        this.start = start;
        this.room = room;
        this.movie = movie;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
