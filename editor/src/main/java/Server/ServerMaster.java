package Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMaster {

    public static void main(String[] args) {
        // Lecture du port depuis peers.cfg
        int port = 12340;
        try (BufferedReader cfg = new BufferedReader(new FileReader("src/main/peers.cfg"))) {
            String line;
            while ((line = cfg.readLine()) != null) {
                if (line.startsWith("master")) {
                    port = Integer.parseInt(line.split(" ")[3]);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("peers.cfg introuvable, port par défaut : " + port);
        }

        SharedDocument document = new SharedDocument();
        List<PrintWriter> slaves = new ArrayList<>();
        System.out.println("Serveur Master ouvert sur le port : " + port);
        final int PORT = port;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket slaveSocket = serverSocket.accept();
                System.out.println("Nouveau slave connecté : " + slaveSocket.getInetAddress());
                new Thread(() -> {
                    try {
                        PrintWriter out = new PrintWriter(slaveSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()));
                        synchronized (slaves) { slaves.add(out); }
                        String request;
                        while ((request = in.readLine()) != null) {
                            String[] parts = request.split(" ", 3);
                            switch (parts[0]) {
                                case "MDFL" -> {
                                    int i = Integer.parseInt(parts[1]);
                                    String texte = parts[2];
                                    if (i >= 1 && i <= document.size()) {
                                        document.modifyline(i, texte);
                                        broadcast("MDFL " + i + " " + texte, slaves);
                                    }
                                }
                                case "RMVL" -> {
                                    int i = Integer.parseInt(parts[1]);
                                    if (i >= 1 && i <= document.size()) {
                                        document.deleteline(i);
                                        broadcast("RMVL " + i, slaves);
                                    }
                                }
                                case "ADDL" -> {
                                    int i = Integer.parseInt(parts[1]);
                                    String texte = parts[2];
                                    if (i >= 1 && i <= document.size() + 1) {
                                        document.addline(i, texte);
                                        broadcast("ADDL " + i + " " + texte, slaves);
                                    }
                                }
                            }
                        }
                        synchronized (slaves) { slaves.remove(out); }
                        slaveSocket.close();
                    } catch (IOException e) {
                        System.err.println("Slave déconnecté : " + e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur Master : " + e.getMessage());
        }
    }

    private static void broadcast(String message, List<PrintWriter> slaves) {
        synchronized (slaves) {
            for (PrintWriter slave : slaves) {
                slave.println(message);
            }
        }
    }
}


