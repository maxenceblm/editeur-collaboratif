package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ServerCentral {
    private static final int port = 12345;
    public static void main(String[] args) {
        SharedDocument document = new SharedDocument(); // Création page
        System.out.println("Serveur ouvert sur le port : "+ port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Attente Client
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());
                Thread thread = new Thread(new ClientHandler(clientSocket,document)) ;  
                thread.start();
            }
        }
        catch (IOException e) {
            System.err.println("Erreur serveur : "+ e.getMessage()) ;
        }
    }
}