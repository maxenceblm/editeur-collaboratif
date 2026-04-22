package Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ClientController {

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField textField;

    @FXML
    private MenuItem deleteLineMenuItem;

    private ServerConnection serverConnection;
    private boolean isEditing = false;

    @FXML
    public void initialize() {
        try {
            serverConnection = new ServerConnection();
        } catch (IOException e) {
            System.err.println("Impossible de se connecter au serveur : " + e.getMessage());
        }

        handleRefresh();

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            deleteLineMenuItem.setDisable(newValue == null);
            if (newValue != null) {
                textField.setText(newValue);
            }
        });
        textField.focusedProperty().addListener((obs, oldFocused, focused) -> isEditing = focused);

        Timer timer = new Timer(true); // daemon : s'arrête avec l'appli
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (serverConnection == null || isEditing) return;
                try {
                    List<String> lines = serverConnection.getDocument();
                    Platform.runLater(() -> listView.getItems().setAll(lines));
                } catch (IOException e) {
                    System.err.println("Erreur rafraîchissement auto : " + e.getMessage());
                }
            }
        }, 2000, 2000);
    }

    @FXML
    private void handleAddLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        int insertPos; // position 1-based envoyée au serveur
        if (selectedIndex == -1) {
            // pas de sélection : ajouter en fin de liste
            insertPos = listView.getItems().size() + 1;
            listView.getItems().add("(New Line)");
        } else {
            // insérer juste après la ligne sélectionnée
            insertPos = selectedIndex + 2;
            listView.getItems().add(selectedIndex + 1, "(New Line)");
        }
        if (serverConnection != null) {
            serverConnection.addLine(insertPos, "(New Line)");
        }
    }

    @FXML
    private void handleDeleteLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            listView.getItems().remove(selectedIndex);
            if (serverConnection != null) {
                serverConnection.removeLine(selectedIndex + 1); // 1-based
            }
        }
    }

    @FXML
    private void handleTextFieldUpdate() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            String newText = textField.getText();
            listView.getItems().set(selectedIndex, newText);
            if (serverConnection != null) {
                serverConnection.modifyLine(selectedIndex + 1, newText); // 1-based
            }
        }
        isEditing = false;
        listView.requestFocus();
    }

    @FXML
    private void handleRefresh() {
        if (serverConnection == null) return;
        try {
            List<String> lines = serverConnection.getDocument();
            listView.getItems().setAll(lines);
        } catch (IOException e) {
            System.err.println("Erreur lors du rafraîchissement : " + e.getMessage());
        }
    }
}
