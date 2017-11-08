package no.ntnu.imt3281.ludo.server;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * 
 * This is the main class for the server. 
 * **Note, change this to extend other classes if desired.**
 * 
 * @author 
 *
 */

public class Server extends JFrame {
    /**
	 * It said so
	 */
	private static final long serialVersionUID = 1L;
	private JTextField enterField;
    private JTextArea displayArea;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;
    private int counter = 1;

    public Server() {
        super("Server");

        enterField = new JTextField();
        enterField.setFont(new Font("Arial", Font.PLAIN, 26));
        enterField.setEditable(false);
        enterField.addActionListener(e -> {
            sendData(e.getActionCommand());
            enterField.setText("");
        });
        add(enterField, BorderLayout.NORTH);

        displayArea = new JTextArea();
        displayArea.setFont(new Font("Arial", Font.PLAIN, 26));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        setSize(600, 300);
        setVisible(true);
    }

    public void runServer() {
        try {
            server = new ServerSocket(12345, 100);

            while (true) {
                try {
                    waitForConnection();
                    getStreams();
                    processConnection();
                } catch (EOFException eofe) {
                    displayMessage("\nServer terminated connection");
                } finally {
                    closeConnection();
                    ++counter;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void waitForConnection() throws IOException {
        displayMessage("Waiting for connection\n");
        connection = server.accept();
        displayMessage("Connection " + counter + " recevied from "
                + connection.getInetAddress().getHostName());
    }

    private void getStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();

        input = new ObjectInputStream(connection.getInputStream());

        displayMessage("\nGot I/O streams\n");
    }

    private void processConnection() throws IOException {
        String message = "Connection successful";
        sendData(message);

        setTextFieldEditable(true);

        do {
            try {
                message = (String) input.readObject();
                displayMessage("\n" + message);
            } catch (ClassNotFoundException cnfe) {
                displayMessage("\nUnknown object type received");
            }
        } while (!message.equals("CLIENT>>> TERMINATE"));
    }

    private void closeConnection() {
        displayMessage("\nTerminating connection\n");
        setTextFieldEditable(false);

        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void sendData(String message) {
        try {
            output.writeObject("SERVER>>> " + message);
            output.flush();
            displayMessage("\nSERVER>>> " + message);
        } catch (IOException ioe) {
            displayMessage("\nError writing object");
        }
    }

    private void displayMessage(String messageToDisplay) {
        SwingUtilities.invokeLater(() -> displayArea.append(messageToDisplay));
    }

    private void setTextFieldEditable(boolean editable) {
        SwingUtilities.invokeLater(() -> enterField.setEditable(editable));
    }
}