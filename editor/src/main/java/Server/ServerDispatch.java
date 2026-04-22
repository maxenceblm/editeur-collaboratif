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
import java.util.concurrent.atomic.AtomicInteger;

public class ServerDispatch {
    public static void main(String[] args) throws IOException {
        int port = 12350 ; 
        AtomicInteger index = new AtomicInteger(0); // Pour Round Robin car int marche pas à cause du thread
        List<String> slaves = new ArrayList<>(); 
        try (BufferedReader cfg = new BufferedReader(new FileReader("src/main/peers.cfg"))) {
            String line;
            while ((line = cfg.readLine()) != null) {
                if (line.startsWith("peer")) {
                    String[] parts = line.split(" ");
                    slaves.add(parts[2]+" "+ parts[3]);
                }
            }
        } catch (IOException e) {
            System.err.println("peers.cfg introuvable, port par défaut : " + port);
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept() ; 
                System.out.println("Client demande un serveur ->"+ slaves.get(index.get()%slaves.size()));
                new Thread(() -> {
                    try {
                        PrintWriter out = new PrintWriter(client.getOutputStream(), true); 
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream())) ; 
                        String request = in.readLine() ; 
                        if ("GETSERVER".equals(request)) {
                            int i = index.getAndIncrement() % slaves.size();
                            out.println("SERVER " + slaves.get(i));
                        }
                        client.close();
                    }
                    catch (IOException e) {
                        System.err.println(e.getMessage()) ; 
                    }
                }).start();
            }

        }
    }
}