package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerReplicated {

    private int serverId;
    private int serverPort;
    private SharedDocument document;
    private List<PrintWriter> clients = new ArrayList<>();
    private List<PrintWriter> peers = new ArrayList<>();

    public ServerReplicated(int serverId, int serverPort) {
        this.serverId = serverId;
        this.serverPort = serverPort;
        this.document = new SharedDocument();
    }

    public void start() throws IOException {
        System.out.println("Serveur " + serverId + " démarré sur le port " + serverPort);

        // Connexion aux autres serveurs
        for (int i = 1; i <= 3; i++) {
            if (i != serverId) {
                int peerPort = 12360 + i;
                try {
                    Socket s = new Socket("localhost", peerPort);
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    peers.add(out);
                    System.out.println("Connecté au serveur " + i);
                } catch (IOException e) {
                    System.out.println("Serveur " + i + " pas disponible");
                }
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleConnection(socket)).start();
            }
        }
    }

    private void handleConnection(Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String firstLine = in.readLine();
            if ("SERVER".equals(firstLine)) {
                peers.add(out);
            } else {
                clients.add(out);
                handleRequest(firstLine, out, false);
            }
            String request;
            while ((request = in.readLine()) != null) {
                handleRequest(request, out, peers.contains(out));
            }
            clients.remove(out);
            peers.remove(out);
            socket.close();
        } catch (IOException e) {
            System.err.println("Déconnexion : " + e.getMessage());
        }
    }

    private void handleRequest(String request, PrintWriter out, boolean isPeer) {
        if (request == null) return;
        String[] parts = request.split(" ", 3);
        switch (parts[0]) {
            case "GETD" -> {
                List<String> lines = document.getAllLines();
                for (int i = 0; i < lines.size(); i++)
                    out.println("LINE " + (i + 1) + " " + lines.get(i));
                out.println("DONE");
            }
            case "MDFL" -> {
                int i = Integer.parseInt(parts[1]);
                if (i >= 1 && i <= document.size()) {
                    document.modifyline(i, parts[2]);
                    notifyClients("LINE " + i + " " + parts[2]);
                    if (isPeer == false) notifyPeers("MDFL " + i + " " + parts[2]);
                }
            }
            case "RMVL" -> {
                int i = Integer.parseInt(parts[1]);
                if (i >= 1 && i <= document.size()) {
                    document.deleteline(i);
                    notifyClients("RMVL " + i);
                    if (isPeer == false) notifyPeers("RMVL " + i);
                }
            }
            case "ADDL" -> {
                int i = Integer.parseInt(parts[1]);
                String texte = parts.length > 2 ? parts[2] : "";
                if (i >= 1 && i <= document.size() + 1) {
                    document.addline(i, texte);
                    notifyClients("ADDL " + i + " " + texte);
                    if (isPeer == false) notifyPeers("ADDL " + i + " " + texte);
                }
            }
        }
    }

    private void notifyClients(String message) {
        for (PrintWriter client : clients) client.println(message);
    }

    private void notifyPeers(String message) {
        for (PrintWriter peer : peers) peer.println(message);
    }

    public static void main(String[] args) throws IOException {
        int id = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        new ServerReplicated(id, 12360 + id).start();
    }
}
