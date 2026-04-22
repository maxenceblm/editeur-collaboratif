package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandlerPush implements Runnable {

    private SharedDocument document ;
    private Socket socket ;
    private List<PrintWriter> clients ; 


    public ClientHandlerPush(Socket socket, SharedDocument document, List<PrintWriter> clients) {
        this.socket = socket ;
        this.document = document ;
        this.clients = clients ; 
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            synchronized (clients) { clients.add(out); }
            String request;
            while ((request = in.readLine()) != null) {
                handleRequest(request, out);
            }
            synchronized (clients) { clients.remove(out); }
            socket.close();
        } catch(IOException e) {
            System.err.println("Client déconnecté : " + e.getMessage());
        }
    }
    
    private void handleRequest(String request, PrintWriter out) {
        String[] parts = request.split(" ", 3);
        String command = parts[0];
        switch (command) {
            case "GETD" -> {
                List<String> lines = document.getAllLines();
                for (int i = 0; i < lines.size(); i++) {
                    out.println("LINE " + (i + 1) + " " + lines.get(i));
                }
                out.println("DONE");
            }
            case "GETL" -> {
                int i = Integer.parseInt(parts[1]);
                if (i < 1 || i > document.size()) {
                    out.println("ERRL " + i + " ligne inexistante");
                } else {
                    out.println("LINE " + i + " " + document.getline(i));
                }
            }
            case "MDFL" -> {
                int i = Integer.parseInt(parts[1]);
                String texte = parts[2];
                if (i < 1 || i > document.size()) {
                    out.println("ERRL " + i + " ligne inexistante");
                } else {
                    document.modifyline(i, texte);
                    notifyClients("LINE "+i+ " "+ texte);
                }
            }
            case "RMVL" -> {
                int i = Integer.parseInt(parts[1]);
                if (i < 1 || i > document.size()) {
                    out.println("ERRL " + i + " ligne inexistante");
                } else {
                    document.deleteline(i);
                    notifyClients("RMVL "+i);
                }
            }
            case "ADDL" -> {
                int i = Integer.parseInt(parts[1]);
                String text = parts[2];
                if (i < 1 || i > document.size() + 1) {
                    out.println("ERRL " + i + " position invalide");
                } else {
                    document.addline(i, text);
                    notifyClients("ADDL "+i+" "+text);
                }
            }
            default -> out.println("ERRL commande non reconnue : " + command);
        }
    }
    private void notifyClients(String message) { // Méthode pour notifier chaque changement
        synchronized (clients) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }
    }
}
