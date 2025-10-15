import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                ClientHandler newUser = new ClientHandler(socket);
                clientHandlers.add(newUser);
                newUser.start();
            }
        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    static void broadcast(String message, ClientHandler excludeUser) {
        for (ClientHandler aClient : clientHandlers) {
            if (aClient != excludeUser) {
                aClient.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
        System.out.println("User disconnected");
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;
        private String userName; // Declare userName as an instance variable

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                userName = reader.readLine(); // Initialize userName
                broadcast(userName + " has joined the chat", this);

                String clientMessage;
                while ((clientMessage = reader.readLine()) != null) {
                    broadcast(userName + ": " + clientMessage, this);
                }
            } catch (IOException ex) {
                System.out.println("Error in UserThread: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    System.out.println("Error closing socket: " + ex.getMessage());
                }
                removeClient(this);
                if (userName != null) {
                    broadcast(userName + " has left the chat", this);
                }
                //broadcast(userName + " has left the chat", this); // Now userName is accessible
            }
        }

        void sendMessage(String message) {
            writer.println(message);
        }
    }
}