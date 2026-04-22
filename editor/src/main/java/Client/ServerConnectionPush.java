package Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerConnectionPush {
    private static final String HOST = "localhost";
    private static final int PORT = 12361;

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public ServerConnectionPush() throws IOException {

        Socket dispatch = new Socket("localhost", 12350);
        PrintWriter dOut = new PrintWriter(dispatch.getOutputStream(), true);
        BufferedReader dIn = new BufferedReader(new InputStreamReader(dispatch.getInputStream()));
        dOut.println("GETSERVER");
        String response = dIn.readLine(); // "SERVER localhost 12347"
        dispatch.close();
        String[] parts = response.split(" ");
        String host = parts[1];
        int port = Integer.parseInt(parts[2]);
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

    public String readLine() throws IOException {
        return in.readLine() ;
    }

    public void close() throws IOException {
        socket.close();
    }
    
    // Demande toutes les lignes du document 
    public synchronized List<String> getDocument() throws IOException {
        out.println("GETD");
        List<String> lines = new ArrayList<>();
        String response;
        while ((response = in.readLine()) != null && !response.equals("DONE")) {
            if (response.startsWith("LINE ")) {
                // format : "LINE i texte"
                String rest = response.substring(5); // après "LINE "
                int spaceIdx = rest.indexOf(' ');
                if (spaceIdx != -1) {
                    lines.add(rest.substring(spaceIdx + 1));
                }
            }
        }
        return lines;
    }
}
