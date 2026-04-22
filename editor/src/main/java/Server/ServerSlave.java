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

public class ServerSlave {

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 12347;
        String masterHost = "localhost";
        int masterPort = 12340;
        try (BufferedReader cfg = new BufferedReader(new FileReader("src/main/peers.cfg"))) {
            String line;
            while ((line = cfg.readLine()) != null) {
                if (line.startsWith("master")) {
                    String[] parts = line.split(" ");
                    masterHost = parts[2];
                    masterPort = Integer.parseInt(parts[3]);
                    break;
                }
                if (line.startsWith("peer")) {
                    port = Integer.parseInt(line.split(" ")[3]);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("peers.cfg introuvable, port par défaut : " + port);
        }

        SharedDocument document = new SharedDocument();
        List<PrintWriter> clients = new ArrayList<>();
        try {
            // Connexion au Master
            Socket masterSocket = new Socket(masterHost, masterPort);
            PrintWriter masterOut = new PrintWriter(masterSocket.getOutputStream(), true);
            BufferedReader masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            masterOut.println("SLAVE");
            System.out.println("Connecté au maître sur le port " + masterPort);

            // Thread d'écoute
            new Thread(() -> {
                try {
                    String message;
                    while ((message = masterIn.readLine()) != null) {
                        String[] parts = message.split(" ", 3);
                        switch (parts[0]) {
                            case "MDFL" -> {
                                int i = Integer.parseInt(parts[1]);
                                document.modifyline(i, parts[2]);
                                notifyClients("LINE " + i + " " + parts[2], clients);
                            }
                            case "RMVL" -> {
                                int i = Integer.parseInt(parts[1]);
                                document.deleteline(i);
                                notifyClients("RMVL " + i, clients);
                            }
                            case "ADDL" -> {
                                int i = Integer.parseInt(parts[1]);
                                String texte = parts.length > 2 ? parts[2] : "";
                                document.addline(i, texte);
                                notifyClients("ADDL " + i + " " + texte, clients);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Connexion au maître perdue : " + e.getMessage());
                }
            }).start();

            // ServerSocket pour les clients GUI
            System.out.println("Serveur Slave ouvert sur le port : " + port);
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());
                    new Thread(() -> {
                        try {
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            synchronized (clients) { clients.add(out); }
                            String request;
                            while ((request = in.readLine()) != null) {
                                String[] parts = request.split(" ", 3);
                                switch (parts[0]) {
                                    case "GETD" -> {
                                        List<String> lines = document.getAllLines();
                                        for (int i = 0; i < lines.size(); i++) {
                                            out.println("LINE " + (i + 1) + " " + lines.get(i));
                                        }
                                        out.println("DONE");
                                    }
                                    case "MDFL", "RMVL", "ADDL" -> masterOut.println(request);
                                }
                            }
                            synchronized (clients) { clients.remove(out); }
                            clientSocket.close();
                        } catch (IOException e) {
                            System.err.println("Client déconnecté : " + e.getMessage());
                        }
                    }).start();
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur Slave : " + e.getMessage());
        }
    }

    private static void notifyClients(String message, List<PrintWriter> clients) {
        synchronized (clients) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }
    }
}
