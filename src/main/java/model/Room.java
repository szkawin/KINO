package model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ROOM")
public class Room { // "room" jak w "screening room"

    public enum Type {NORMAL, IMAX, VIP}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NUMBER", unique = true, nullable = false) // numer nie może się powtarzać
    private Integer number;

    @Column(name = "SEATS", nullable = false) // ilość miejsc wymagana
    private Integer seats;

    @Enumerated
    @Column(name = "TYPE", nullable = false, columnDefinition = "smallint") // typ jest wymagany
    private Type type;

    // sztuczka: dodajemy niepotrzebną listę seansów (umyślnie pole jest prywatne i nie ma getera) tylko po to
    // żeby hibernate wiedziało że przy kasowania sali ma skasować seanse powiązane z tą salą.
    // W przeciwnym wypadku mamy ConstraintViolationException i nie można skasować sali.
    // tu może zostać fetch = FetchType.LAZY bo nikt nie ma dostepu
    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE) // bez tego nie działa kaskadowanie kasownia
    private List<Screening> screenings = new ArrayList<>();

    public Room() {
    }

    public Room(Integer number, Integer seats, Type type) {
        this.number = number;
        this.seats = seats;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(this.number);
        s.append(' ');
        s.append('(');
        s.append(this.type);
        s.append(')');
        return s.toString();
    }
}
