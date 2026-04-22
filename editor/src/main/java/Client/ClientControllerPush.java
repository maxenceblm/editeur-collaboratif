package Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.List;

public class ClientControllerPush {

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField textField;

    @FXML
    private MenuItem deleteLineMenuItem;

    private ServerConnectionPush serverConnection;

    @FXML
    public void initialize() {
        try {
            serverConnection = new ServerConnectionPush();
        } catch (IOException e) {
            System.err.println("Impossible de se connecter au serveur : " + e.getMessage());
        }

        handleRefresh();
        // Thread d'écoute des notifications
        new Thread(() -> {
            try {
                String message ;  
                while((message = serverConnection.readLine()) != null) { // tant qu'on a à lire
                    String msg = message ; 
                    Platform.runLater(() -> {
                        String[] parts = msg.split(" ",3); // 3 éléments  
                        switch(parts[0]) {
                            case "LINE" -> {
                                int i = Integer.parseInt(parts[1]) ; // i en deuxième position
                                listView.getItems().set(i-1,parts[2]) ; 
                            }
                            case"RMVL" -> {
                                int i = Integer.parseInt(parts[1]) ;
                                listView.getItems().remove(i-1) ;
                            }
                            case "ADDL" -> {
                                int i = Integer.parseInt(parts[1]) ;
                                listView.getItems().add(i-1,parts[2]);
                            }
                        }
                    });
                }
            }
                catch(IOException e ) {
                    System.err.println("Connexion serveur perdue : " + e.getMessage());
                }
            }).start();
        
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            deleteLineMenuItem.setDisable(newValue == null);
            if (newValue != null) {
                textField.setText(newValue);
            }
        });
    }

    @FXML
    private void handleAddLine() {
        if (serverConnection == null) return;
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        int insertPos;
        if (selectedIndex == -1) {
            insertPos = listView.getItems().size() + 1;
        } else {
            insertPos = selectedIndex + 2;
        }
        serverConnection.addLine(insertPos, "(New Line)");
    }

    @FXML
    private void handleDeleteLine() {
        if (serverConnection == null) return;
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            serverConnection.removeLine(selectedIndex + 1);
        }
    }

    @FXML
    private void handleTextFieldUpdate() {
        if (serverConnection == null) return;
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            String newText = textField.getText();
            serverConnection.modifyLine(selectedIndex + 1, newText);
        }
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
