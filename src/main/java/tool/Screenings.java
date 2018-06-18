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
import javafx.util.converter.LocalDateStringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;
import model.Model;
import model.Movie;
import model.Room;
import model.Screening;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Pattern;

public class Screenings implements ModelController {
    public TableView<Screening> tabelka;
    private final static Pattern HOUR_PATTERN = Pattern.compile("([0-1]?[0-9])|(2[0-3])");
    private final static Pattern MINUTE_PATTERN = Pattern.compile("[0-5]?[0-9]");
    public DatePicker date;
    public ComboBox<Integer> hour;
    public ComboBox<Integer> minute;

    private Model model;
    public ComboBox<Room> room;
    public ComboBox<Movie> movie;

    @Override
    public void setModel(Model model) {
        this.model = model;
        initializeWithModel();
    }

    public void add(ActionEvent actionEvent) {
        try {
            if (date.getValue() == null) {
                date.requestFocus();
            } else if (hour.getValue() == null) {
                hour.requestFocus();
            } else if (minute.getValue() == null) {
                minute.requestFocus();
            } else if (room.getValue() == null) {
                room.requestFocus();
            } else if (movie.getValue() == null) {
                movie.requestFocus();
            } else {
                LocalDate startDate = date.getValue();
                Integer startHour = hour.getValue();
                Integer startMinute = minute.getValue();
                LocalTime startTime = LocalTime.of(startHour, startMinute);
                LocalDateTime start = LocalDateTime.of(startDate, startTime);
                Screening screening = new Screening(start, room.getValue(), movie.getValue());
                model.add(screening);
                tabelka.getItems().add(screening);
                int row = tabelka.getItems().lastIndexOf(screening);
                if (row >= 0) {
                    tabelka.getSelectionModel().select(row);
                    tabelka.getFocusModel().focus(row);
                    // tabelka.edit(row,tabelka.getColumns().get(0));
                    // zamiast tego guzika można by użyć klikania poza kratką jak w
                    // https://stackoverflow.com/questions/44484813/javafx-tableview-edit-not-editing
                }
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
            if (e.getCause().getClass() == ConstraintViolationException.class) {
                // pewnie dwa seanse w tym samym czasie w tej samej sali
                room.requestFocus();
            }
        }
    }

    public void addOnEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            add(new ActionEvent(keyEvent.getSource(), keyEvent.getTarget()));
        }
    }

    @SuppressWarnings({"boxing"})
    public void initialize() {

        date.setConverter(new LocalDateStringConverter()); // domyślny konwerter nic nie robi
        date.setValue(LocalDate.now().plusDays(1)); // dodajemy seanse na jutro

        TextFormatter<Integer> hourFormatter = new TextFormatter<>(
                new IntegerStringConverter(), null,
                c -> HOUR_PATTERN.matcher(c.getControlNewText()).matches() ? c : null);
        hour.getEditor().setTextFormatter(hourFormatter);
        hour.setConverter(new IntegerStringConverter()); // domyślny konwerter nic nie robi
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int h = 9; h <= 23; h++) { // kto chodzi do kina przed 8?
            hours.add(h);
        }
        hour.setItems(hours);
        hour.getSelectionModel().selectFirst(); // jakaś wartość musi być wybrana

        TextFormatter<Integer> minutesFormatter = new TextFormatter<>(
                new IntegerStringConverter(), null,
                c -> MINUTE_PATTERN.matcher(c.getControlNewText()).matches() ? c : null);
        minute.getEditor().setTextFormatter(minutesFormatter);
        minute.setConverter(new IntegerStringConverter()); // domyślny konwerter nic nie robi

        ObservableList<Integer> minutes = FXCollections.observableArrayList(0, 15, 30, 45);
        minute.setItems(minutes);
        minute.getSelectionModel().selectFirst(); // jakaś wartość musi być wybrana
    }

    private void initializeWithModel() {

        room.setItems(model.getRooms()); // TODO: poprawić wyśietlanie!

        movie.setItems(model.getMovies()); // TODO: poprawić wyśietlanie!

        for (TableColumn<Screening, ?> tableColumn : tabelka.getColumns()) {

            if ("start".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Screening, LocalDateTime> column = (TableColumn<Screening, LocalDateTime>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateTimeStringConverter())); // TODO: to trochę kiepskie
                column.setOnEditCommit((val) -> {
                    Screening edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setStart(val.getNewValue());
                    model.update(edited);
                });
            } else if ("room".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Screening, Room> column = (TableColumn<Screening, Room>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(ComboBoxTableCell.forTableColumn(model.getRooms()));
                column.setOnEditCommit((val) -> {
                    Screening edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setRoom(val.getNewValue());
                    model.update(edited);
                });
            } else if ("movie".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Screening, Movie> column = (TableColumn<Screening, Movie>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(ComboBoxTableCell.forTableColumn(model.getMovies()));
                column.setOnEditCommit((val) -> {
                    Screening edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setMovie(val.getNewValue());
                    model.update(edited);
                });
            } else if ("delete".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Screening, Button> column = (TableColumn<Screening, Button>) tableColumn;
                column.setCellFactory(ActionButtonTableCell.forTableColumn("Delete", (Screening item) -> {
                    tabelka.getItems().remove(item);
                    model.remove(item);
                    return item;
                }));
            }
        }

        tabelka.setItems(model.getScreenings());
    }
}

