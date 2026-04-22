package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerCentralPush {
    private static final int port = 12345;
    public static void main(String[] args) {
        SharedDocument document = new SharedDocument();
        List<PrintWriter> clients = new ArrayList<>(); // liste des clients connectés à notifier
        System.out.println("Serveur PUSH ouvert sur le port : " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());
                Thread thread = new Thread(new ClientHandlerPush(clientSocket, document, clients));
                thread.start();
            }
        }
        catch (IOException e) {
            System.err.println("Erreur Serveur : " + e.getMessage());
        }
    }
}
