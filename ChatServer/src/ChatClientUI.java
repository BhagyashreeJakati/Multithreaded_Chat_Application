
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClientUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatClientUI(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // Send a username immediately
            String username = JOptionPane.showInputDialog("Enter your name:");
            out.println(username);  // Send it to the server
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to server");
            System.exit(0);
        }

        // GUI setup
        frame = new JFrame("Chat Client");
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        inputField = new JTextField(30);
        sendButton = new JButton("Send");

        JPanel panel = new JPanel();
        panel.add(inputField);
        panel.add(sendButton);

        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Send message on button click
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // Start thread to listen for incoming messages
        new Thread(this::receiveMessages).start();
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                chatArea.append(message + "\n");
            }
        } catch (IOException e) {
            chatArea.append("Disconnected from server.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientUI("localhost", 12345));
    }
}
