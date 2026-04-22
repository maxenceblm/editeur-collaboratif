package Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 12361;

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public ServerConnection() throws IOException {
        socket = new Socket(HOST, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Demande toutes les lignes du document
    public synchronized List<String> getDocument() throws IOException {
        out.println("GETD");
        List<String> lines = new ArrayList<>();
        String response;
        while ((response = in.readLine()) != null && !response.equals("DONE")) {
            if (response.startsWith("LINE ")) {
                // format : LINE i texte
                String rest = response.substring(5); // après "LINE "
                int spaceIdx = rest.indexOf(' ');
                if (spaceIdx != -1) {
                    lines.add(rest.substring(spaceIdx + 1));
                }
            }
        }
        return lines;
    }

    // Demande la ligne i à partir de 1 
    public synchronized String getLine(int i) throws IOException {
        out.println("GETL " + i);
        String response = in.readLine();
        if (response != null && response.startsWith("LINE ")) {
            String rest = response.substring(5);
            int spaceIdx = rest.indexOf(' ');
            if (spaceIdx != -1) {
                return rest.substring(spaceIdx + 1);
            }
        }
        return null;
    }

    // Modifie la ligne i 
    public synchronized void modifyLine(int i, String text) {
        out.println("MDFL " + i + " " + text);
    }

    // Supprime la ligne i 
    public synchronized void removeLine(int i) {
        out.println("RMVL " + i);
    }

    // Ajoute une ligne à la position i 
    public synchronized void addLine(int i, String text) {
        out.println("ADDL " + i + " " + text);
    }

    public void close() throws IOException {
        socket.close();
    }
}
