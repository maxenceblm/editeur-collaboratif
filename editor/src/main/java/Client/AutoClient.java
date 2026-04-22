package Client;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AutoClient {

    private final List<String> document = new ArrayList<>();
    private final String clientId;

    public AutoClient(String clientId, int nbModifications) throws IOException, InterruptedException {
        this.clientId = clientId;
        ServerConnectionPush connection = new ServerConnectionPush();
        // Chargement initial du document
        List<String> initial = connection.getDocument();
        synchronized (document) {
            document.addAll(initial);
        }
        // Thread d'écoute des notifs en mode  push
        Thread listener = new Thread(() -> {
            try {
                String message;
                while ((message = connection.readLine()) != null) {
                    String[] parts = message.split(" ", 3);
                    synchronized (document) {
                        switch (parts[0]) {
                            case "LINE" -> {
                                int i = Integer.parseInt(parts[1]) - 1;
                                if (i >= 0 && i < document.size())
                                    document.set(i, parts[2]);
                            }
                            case "RMVL" -> {
                                int i = Integer.parseInt(parts[1]) - 1;
                                if (i >= 0 && i < document.size())
                                    document.remove(i);
                            }
                            case "ADDL" -> {
                                int i = Integer.parseInt(parts[1]) - 1;
                                String text = parts.length > 2 ? parts[2] : "";
                                if (i >= 0 && i <= document.size())
                                    document.add(i, text);
                            }
                        }
                    }
                }
            } catch (IOException e) {
            }
        });
        listener.setDaemon(true);
        listener.start();
        long start = System.currentTimeMillis();
        for (int i = 0; i < nbModifications; i++) {
            Thread.sleep(100); // 100ms entre chaque modification pour eviter divergence
            synchronized (document) {
                int size = document.size();
                if (size == 0) {
                    // document vide : on ajoute la ligne 
                    connection.addLine(1, clientId + "_ligne" + i);
                } else {
                    int line = (i % size) + 1; 
                    connection.modifyLine(line, clientId + "_ligne" + i);
                }
            }
        }
        // Attendre que toutes les notifications soient reçues 2 s 
        Thread.sleep(2000);
        // Espace Test Latences 
        long total = System.currentTimeMillis() - start;
        System.out.println("[" + clientId + "] Temps total : " + total + "ms | Débit : " + String.format("%.1f", nbModifications * 1000.0 / total) + " ops/s");

        connection.close();
        listener.join(1000);
    }
    public static void main(String[] args) { // Appel de AutoClient 
        String clientId = args.length > 0 ? args[0] : "Client1";
        int nbModifications = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        try {
            new AutoClient(clientId, nbModifications);
        } catch (Exception e) {
            System.err.println("Erreur AutoClient : " + e.getMessage());
            e.printStackTrace();
        }
        System.exit(0);
    }

}
