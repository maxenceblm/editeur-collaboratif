package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandlerFederate implements Runnable {

    private SharedDocument document ;
    private Socket socket ;
    private List<PrintWriter> clients ; 
    private List<PrintWriter> peers; 
 

    public ClientHandlerFederate(Socket socket, SharedDocument document, List<PrintWriter> clients, List<PrintWriter> peers) {
        this.socket = socket ;
        this.document = document ;
        this.clients = clients ; 
        this.peers = peers;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String firstmessage = in.readLine();
            boolean isPeer = "SERVER".equals(firstmessage) ; // true si SERVER false si client
            if (isPeer == false) {
                synchronized (clients) {clients.add(out) ;}
                handleRequest(firstmessage, out,isPeer); // traiter premiermessage
            }
            String request ; 
            while ((request = in.readLine()) != null) {
                handleRequest(request, out,isPeer);
            }
            if (isPeer) {
                synchronized (peers) {peers.remove(out) ; }
            }
            else {
                synchronized (clients) { clients.remove(out); }
            }
            socket.close();
        } catch(IOException e) {
            System.err.println("Déconnexion : " + e.getMessage());
        }
    }
    
    private void handleRequest(String request, PrintWriter out, Boolean isPeer) throws IOException {
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
                    if (isPeer == false) notifyPeers("MDFL " + i + " " + texte) ; 
                }
            }
            case "RMVL" -> {
                int i = Integer.parseInt(parts[1]);
                if (i < 1 || i > document.size()) {
                    out.println("ERRL " + i + " ligne inexistante");
                } else {
                    document.deleteline(i);
                    notifyClients("RMVL "+i);
                    if (isPeer == false) notifyPeers("RMVL " + i) ; 
                }
            }
            case "ADDL" -> {
                int i = Integer.parseInt(parts[1]);
                String texte = parts[2];
                if (i < 1 || i > document.size() + 1) {
                    out.println("ERRL " + i + " position invalide");
                } else {
                    document.addline(i, texte);
                    notifyClients("ADDL "+i+" "+texte);
                    if (isPeer == false) notifyPeers("ADDL " + i + " " + texte) ; 
                }
            }
            case "CONNECT" -> {
                String host = parts[1] ;
                int port = Integer.parseInt(parts[2]);
                Socket peerSocket = new Socket(host, port);
                PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(),true) ; 
                peerOut.println("SERVER") ; 
                synchronized(peers) {peers.add(peerOut) ;} 

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

    private void notifyPeers(String message) {
        synchronized(peers) {
            for (PrintWriter peer : peers) {
                peer.println(message) ; 
            }
        }
    }
}
