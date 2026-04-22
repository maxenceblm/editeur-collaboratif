package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerCentralFederate {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 12345;
        SharedDocument document = new SharedDocument();
        List<PrintWriter> clients = new ArrayList<>(); // liste des clients connectés à notifier
        List<PrintWriter> peers = new ArrayList<>(); // Liste des autres serveurs
        System.out.println("Serveur PUSH ouvert sur le port : " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());
                Thread thread = new Thread(new ClientHandlerFederate(clientSocket, document, clients,peers));
                thread.start();
            }
        }
        catch (IOException e) {
            System.err.println("Erreur Serveur : " + e.getMessage());
        }
    }
}
