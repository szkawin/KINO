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
import model.Room;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.PersistenceException;
import java.util.regex.Pattern;

public class Rooms implements ModelController {
    public TableView<Room> tabelka;
    private final static Pattern NUMBER_PATTERN = Pattern.compile("[1-9][0-9]*");
    private final static Pattern SEATS_PATTERN = Pattern.compile("[1-9][0-9]*");
    public TextField number;

    private Model model;
    public TextField seats;
    public ComboBox<Room.Type> type;

    @Override
    public void setModel(Model model) {
        this.model = model;
        tabelka.setItems(this.model.getRooms());
    }

    @SuppressWarnings({"unchecked"})
    public void add(ActionEvent actionEvent) {
        try {
            if (number.getText().isEmpty()) {
                number.requestFocus(); // sala musi mieć numer
            } else if (seats.getText().isEmpty()) {
                seats.requestFocus(); // sala musi mieć ilość miejsc
            } else if (type.getValue() == null) { // trzeba wybrać typ sali
                type.requestFocus();
            } else {
                Room room = new Room();
                room.setNumber(((TextFormatter<Integer>) number.getTextFormatter()).getValue());
                room.setSeats(((TextFormatter<Integer>) seats.getTextFormatter()).getValue());
                room.setType(type.getValue());
                model.add(room);
                tabelka.getItems().add(room);
                int row = tabelka.getItems().lastIndexOf(room);
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
            // pewnie dwa pokoje z tym samym numerem
            number.selectAll();
            number.requestFocus();
        } catch (PersistenceException e) {
            e.printStackTrace();
            if (e.getCause().getClass() == ConstraintViolationException.class) {
                // pewnie dwa pokoje z tym samym numerem
                number.selectAll();
                number.requestFocus();
            }
        }
    }

    public void addOnEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            add(new ActionEvent(keyEvent.getSource(), keyEvent.getTarget()));
        }
    }

    public void initialize() {

        TextFormatter<Integer> numberFormatter = new TextFormatter<>(
                new IntegerStringConverter(), null,
                c -> NUMBER_PATTERN.matcher(c.getControlNewText()).matches() ? c : null);
        number.setTextFormatter(numberFormatter);

        TextFormatter<Integer> seatsFormatter = new TextFormatter<>(
                new IntegerStringConverter(), null,
                c -> SEATS_PATTERN.matcher(c.getControlNewText()).matches() ? c : null);
        seats.setTextFormatter(seatsFormatter);

        ObservableList<Room.Type> types = FXCollections.observableArrayList(Room.Type.values());
        type.setItems(types);
        // TODO: coś zrobić żeby zamiazt jawowych identyfikatorów enumów się wyświetlały nadane nazwy

        for (TableColumn<Room, ?> tableColumn : tabelka.getColumns()) {

            if ("number".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Room, Integer> column = (TableColumn<Room, Integer>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                column.setOnEditCommit((val) -> {
                    Room edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setNumber(val.getNewValue());
                    model.update(edited);
                });
            } else if ("seats".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Room, Integer> column = (TableColumn<Room, Integer>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                column.setOnEditCommit((val) -> {
                    Room edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setSeats(val.getNewValue());
                    model.update(edited);
                });
            } else if ("type".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Room, Room.Type> column = (TableColumn<Room, Room.Type>) tableColumn;
                column.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
                column.setCellFactory(ComboBoxTableCell.forTableColumn(types));
                column.setOnEditCommit((val) -> {
                    Room edited = val.getTableView().getItems().get(val.getTablePosition().getRow());
                    edited.setType(val.getNewValue());
                    model.update(edited);
                });
                // TODO: coś zrobić żeby zamiazt jawowych identyfikatorów enumów się wyświetlały nadane nazwy
            } else if ("delete".equals(tableColumn.getId())) {
                @SuppressWarnings({"unchecked"})
                TableColumn<Room, Button> column = (TableColumn<Room, Button>) tableColumn;
                column.setCellFactory(ActionButtonTableCell.forTableColumn("Delete", (Room item) -> {
                    tabelka.getItems().remove(item);
                    model.remove(item);
                    return item;
                }));
            }
        }
    }
}
