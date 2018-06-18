package model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MOVIE")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "TITLE", nullable = false) // tytuł jest wymagany
    private String title;

    @Column(name = "YEAR")
    private Integer year;

    @Enumerated
    @Column(name = "MPAA_RATING", columnDefinition = "smallint")
    private MppaRating mpaaRating;

    @Column(name = "DURATION")
    private Integer duration;

    @Column(name = "DESCRIPTION")
    private String description;

    // sztuczka: dodajemy niepotrzebną listę seansów (umyślnie pole jest prywatne i nie ma getera) tylko po to
    // żeby hibernate wiedziało że przy kasowania sali ma skasować seanse powiązane z tym filmem.
    // W przeciwnym wypadku mamy ConstraintViolationException i nie można skasować filmu.
    // tu może zostać fetch = FetchType.LAZY bo nikt nie ma dostepu
    @OneToMany(mappedBy = "movie", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE) // bez tego nie działa kaskadowanie kasownia
    private List<Screening> screenings = new ArrayList<>();

    public Movie() {
    }

    public Movie(String title, Integer year, MppaRating mpaaRating, Integer duration, String description) {
        this.title = title;
        this.year = year;
        this.mpaaRating = mpaaRating;
        this.duration = duration;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) { this.year = year; }

    public MppaRating getMpaaRating() {
        return mpaaRating;
    }

    public void setMpaaRating(MppaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    public enum MppaRating {
        G("G", "General Audiences – all ages admitted"),
        PG("PG", "Parental Guidance Suggested – some material may not be suitable for children"),
        PG13("PG-13", "Parents Strongly Cautioned – some material may be inappropriate for children under 13"),
        R("R", "Restricted – under 17 requires accompanying parent or adult guardian"),
        NC17("NC-17", "No one 17 and under admitted");

        private final String mppaName;
        private final String description;

        MppaRating(String mppaName, String description) {
            this.mppaName = mppaName;
            this.description = description;
        }

        private String mppaName() {
            return mppaName;
        }

        private String description() {
            return description;
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append('"');
        s.append(this.title);
        s.append('"');
        if (this.year != null) {
            s.append(' ');
            s.append('(');
            s.append(this.year);
            s.append(')');
        }
        return s.toString();
    }
}
