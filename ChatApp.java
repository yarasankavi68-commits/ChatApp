import java.io.*;
import java.net.*;
import java.util.*;

public class ChatApp {

    static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter mode (server/client): ");
        String mode = sc.nextLine();

        if (mode.equalsIgnoreCase("server")) {
            startServer();
        } else {
            startClient();
        }
    }

    // ================= SERVER =================
    static void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Chat Server started on port 1234");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");

            ClientHandler client = new ClientHandler(socket);
            clients.add(client);
            new Thread(client).start();
        }
    }

    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    static class ClientHandler implements Runnable {
        Socket socket;
        BufferedReader in;
        PrintWriter out;

        ClientHandler(Socket socket) throws Exception {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void run() {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    System.out.println("Received: " + msg);
                    broadcast(msg, this);
                }
            } catch (Exception e) {
                System.out.println("Client disconnected");
            }
        }

        void sendMessage(String msg) {
            out.println(msg);
        }
    }

    // ================= CLIENT =================
    static void startClient() throws Exception {
        Socket socket = new Socket("localhost", 1234);
        BufferedReader userInput = new BufferedReader(
                new InputStreamReader(System.in));
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

        // Thread to receive messages
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("Message: " + msg);
                }
            } catch (Exception e) {
                System.out.println("Disconnected from server");
            }
        }).start();

        // Send messages
        String text;
        while ((text = userInput.readLine()) != null) {
            out.println(text);
        }
    }
}

