package tool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.IntegerStringConverter;
import model.Model;
import model.Movie;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.PersistenceException;
import java.util.regex.Pattern;

public class Movies implements ModelController {
    public TableView<Movie> tabelka;
    public TextField title;
    public TextField year;
    public TextField duration;
    public ComboBox<Movie.MppaRating> mppaRating;
    public TextArea description;

    private Model model;
    private final static Pattern DURATION_PATTERN = Pattern.compile("[1-9][0-9]*");
    private final static Pattern YEAR_PATTERN = Pattern.compile("[1-9][0-9]{0,3}");

    @Override
    public void setModel(Model model) {
        this.model = model;
        tabelka.setItems(this.model.getMovies());
    }

    @SuppressWarnings({"unchecked"})
    public void add(ActionEvent actionEvent){
        try {
            if (title.getText().isEmpty()) {
                title.requestFocus(); // film musi mieć tytuł!
            } else {
                Movie movie = new Movie();
                movie.setTitle(title.getText());
                movie.setYear(((TextFormatter<Integer>) year.getTextFormatter()).getValue());
                movie.setDuration(((TextFormatter<Integer>) duration.getTextFormatter()).getValue());
                movie.setMpaaRating(mppaRating.getValue());
                movie.setDescription(description.getText());
                model.add(movie);
                tabelka.getItems().add(movie);
                int row = tabelka.getItems().lastIndexOf(movie);
                if (row >= 0) {
                    tabelka.getSelectionModel().select(row);
                    tabelka.getFocusModel().focus(row);
                    // tabelka.edit(row,tabelka.getColumns().get(0));
                    // zamiast tego guzika można by użyć klikania poza kratką jak w
                    // https://stackoverflow.com/questions/44484813/javafx-tableview-edit-not-editing
                }
            }
        } catch (ConstraintViolationException e) {
            e.printStackTrace();
            // pewnie dwa filmy o tym samym tytule
            title.selectAll();
            title.requestFocus();
        } catch (PersistenceException e) {
            e.printStackTrace();
            if (e.getCause().getClass() == ConstraintViolationException.class) {
                // pewnie dwa pokoje tym samym tytule
                title.selectAll();
                title.requestFocus();
            }
        }
    }

    public void addOnEnter(KeyEvent keyEvent){
        if (keyEvent.getCode() == KeyCode.ENTER) {
            add(new ActionEvent(keyEvent.getSource(), keyEvent.getTarget()));
        }
    }

    public void initialize() {

        TextFormatter<Integer> yearFormatter = new TextFormatter<>(
                new IntegerStringConverter(), null,
                c -> YEAR_PATTERN.matcher(c.getControlNewText()).matches() ? c : null );
        year.setTextFormatter(yearFormatter);

        TextFormatter<Integer> durationFormatter = new TextFormatter<>(
                new IntegerStringConverter(),null,
                c -> DURATION_PATTERN.matcher(c.getControlNewText()).matches() ? c : null );
        duration.setTextFormatter(durationFormatter);

        ObservableList<Movie.MppaRating> mppaRatings = FXCollections.observableArrayList(Movie.MppaRating.values());
        mppaRating.setItems(mppaRatings);
        // TODO: coś zrobić żeby zamiast jawowych identyfikatorów enumów się wyświetlały nadane nazwy czy opisy

        for (TableColumn<Movie, ?> tableColumn : tabelka.getColumns()) {
            if ("title".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Movie, String> column = (TableColumn<Movie, String>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(TextFieldTableCell.forTableColumn());
                column.setOnEditCommit((val) -> {
                    Movie edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setTitle(val.getNewValue());
                    model.update(edited);
                });
            } else if ("year".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Movie, Integer> column = (TableColumn<Movie, Integer>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                column.setOnEditCommit((val) -> {
                    Movie edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setYear(val.getNewValue());
                    model.update(edited);
                });
            } else if ("duration".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Movie, Integer> column = (TableColumn<Movie, Integer>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                column.setOnEditCommit((val) -> {
                    Movie edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setDuration(val.getNewValue());
                    model.update(edited);
                });
            } else if ("mpaaRating".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Movie, Movie.MppaRating> column = (TableColumn<Movie, Movie.MppaRating>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(ComboBoxTableCell.forTableColumn(mppaRatings));
                column.setOnEditCommit((val) -> {
                    Movie edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setMpaaRating(val.getNewValue());
                    model.update(edited);
                });
                // TODO: coś zrobić żeby zamiast jawowych identyfikatorów enumów się wyświetlały nadane nazwy czy opisy
            } else if ("description".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Movie, String> column = (TableColumn<Movie, String>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(TextFieldTableCell.forTableColumn());
                column.setOnEditCommit((val) -> {
                    Movie edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setDescription(val.getNewValue());
                    model.update(edited);
                });
            } else if ("delete".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Movie, Button> column = (TableColumn<Movie, Button>) tableColumn;
                column.setCellFactory(ActionButtonTableCell.forTableColumn("Delete", (Movie item) -> {
                    tabelka.getItems().remove(item);
                    model.remove(item);
                    return item;
                }));

            }
        }
    }
}
