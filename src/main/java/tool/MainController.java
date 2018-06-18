package tool;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.Model;

import java.io.IOException;

public class MainController {
    public TabPane tabPane;
    public Tab movies;
    public Tab rooms;
    public Tab screenings;

    private Model model;

    public MainController() {
        model = new Model();
        // TODO może wyłączyć to dodawanie przykładów albo dodać jakiś inny warunek?
        // Dodaje filmy i sale jeśli brakuje.
        if (model.getMovies().isEmpty() || model.getRooms().isEmpty()) {
            model.addExamples();
        }
    }

    public void initialize() {

        // https://stackoverflow.com/questions/12146362/how-to-load-content-to-javafx-tabs-dynamically
        // It is important to call it before adding ChangeListener to the tabPane to avoid NPE and
        // to be able fire the manual selection event below. Otherwise the 1st tab will be selected
        // with empty content.
        tabPane.getSelectionModel().clearSelection();

        // Add a Tab ChangeListener
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                changedTab(oldValue, newValue);
            }
        });

        // By default, select 1st tab and load its content.
        tabPane.getSelectionModel().selectFirst();
    }

    private void changedTab(Tab oldTab, Tab newTab) {
        System.out.println("newTab: " + newTab.getId());

        // Zapominamy zawartość starej zakładki ale za pierwszym razem oldTab jest null.
        if (oldTab != null) {
            oldTab.setContent(null);
        }

        // Ładujemy kontrolki do nowego taba
        try {
            String fxml = newTab.getId() + ".fxml";
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = childLoader.load();
            newTab.setContent(root);

            ModelController childController = childLoader.getController();
            childController.setModel(model); // przekazujemy model kina do podkontrolera nowej zakładki

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
