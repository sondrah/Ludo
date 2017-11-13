package no.ntnu.imt3281.ludo.client;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * This is the client in the multiuser chat system. It is a fairly basic chat
 * system with only one room but an unlimited number of simultaneous users. When
 * started the client asks the user for a nickname and that is used throughout
 * the session.
 * 
 * All messages sent is displayed in all connected clients and the nickname is
 * prepended to the message so that it is easy to see who is saying what.
 * 
 * @author okolloen
 *
 */
public class ChatClient extends JFrame {
    private JTextArea dialog;
    private JTextField textToSend;
    private JList<String> participants;
    private DefaultListModel<String> participantsModel;
    private String myName;
    private BufferedWriter output;
    private BufferedReader input;
    private Socket connection;

    /**
     * Constructor that sets up the GUI. The GUI consists of a textarea where
     * all messages is displayed, a list with all connected users and a
     * textfield where the user can enter the text to send. To actually send a
     * message the user must press enter in the textfield.
     */
    public ChatClient() {
        super("Chat client");

        // Set up the textarea used to display all messages
        dialog = new JTextArea();
        dialog.setEditable(false);
        dialog.setFont(new Font("Arial", Font.PLAIN, 26));
        add(new JScrollPane(dialog), BorderLayout.CENTER);

        // Set up the list of participants
        participants = new JList<String>(
                participantsModel = new DefaultListModel<String>());
        participants.setFixedCellWidth(160);
        participants.setFont(new Font("Arial", Font.PLAIN, 26));
        add(new JScrollPane(participants), BorderLayout.EAST);

        // Set up the textfield used to enter text to send
        textToSend = new JTextField();
        textToSend.setFont(new Font("Arial", Font.PLAIN, 26));
        add(textToSend, BorderLayout.SOUTH);
        // Add an actionlistener to the textfield
        textToSend.addActionListener(e -> {
            sendText(e.getActionCommand());
            textToSend.setText("");
        });
        textToSend.requestFocus();

        // Add a window listener, this sends a message indicating
        // to the server that the user is leaving (logging out)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendText(">>>LOGOUT<<<");
            }
        });
        setSize(600, 400);
        setVisible(true);
    }

    /**
     * Connects to the server, on port 12345 and localhost. This would be
     * changed for a production version. Once the socket connection is
     * established a bufferedReader and a bufferedWriter is created, this is our
     * input and output.
     * 
     * Once connected the user is asked to provide a nickname to be used
     * throughout the session. A login message is then sent to the server with
     * that nickname.
     */
    public void connect() {
        try {
            connection = new Socket("localhost", 12345);
            output = new BufferedWriter(new OutputStreamWriter(
                    connection.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            myName = JOptionPane.showInputDialog(this, "Your nickname?");
            if (myName == null || myName.equals("")) {
                JOptionPane.showMessageDialog(this, "No nick given");
                System.exit(1);
            }
            sendText("LOGIN:" + myName);
        } catch (IOException ioe) { // If we are unable to connect, alert the
                                    // user and exit
            JOptionPane.showMessageDialog(this, "Error connecting to server: "
                    + ioe);
            System.exit(1);
        }
    }

    /**
     * This method handles the communication from the server. Note that this
     * method never returns, messages from the server is read in a loop that
     * never ends. All other user interaction is handled in the GUI thread.
     * 
     * Login and logout messages is used to add/remove users to/from the list of
     * participants while all other messages are displayed.
     */
    public void processConnection() {
        while (true) {
            try {
                String tmp = input.readLine();
                if (tmp.startsWith("LOGIN:")) { // User is logging in
                    addUser(tmp.substring(6));
                } else if (tmp.startsWith("LOGOUT:")) { // User is logging out
                    removeUser(tmp.substring(7));
                } else { // All other messages
                    displayMessage(tmp + "\n");
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Error receiving data: "
                        + ioe);
            }
        }
    }

    /**
     * Used to add messages to the message area in a thread safe manner
     * 
     * @param text
     *            the text to be added
     */
    private void displayMessage(String text) {
        SwingUtilities.invokeLater(() -> dialog.append(text));
    }

    /**
     * Used to remove a user from the user list in a thread safe manner
     * 
     * @param username
     *            the name of the user to remove from the list
     */
    private void removeUser(String username) {
        SwingUtilities.invokeLater(() -> participantsModel
                .removeElement(username));
    }

    /**
     * Used to add a user to the user list in a thread safe manner
     * 
     * @param username
     *            the name of the user to add to the list
     */
    private void addUser(String username) {
        SwingUtilities
                .invokeLater(() -> participantsModel.addElement(username));
    }

    /**
     * Method used to send a message to the server. Handled in a separate method
     * to ensure that all messages are ended with a newline character and are
     * flushed (ensure they are sent.)
     * 
     * @param textToSend
     *            the message to send to the server
     */
    private void sendText(String textToSend) {
        try {
            output.write(textToSend);
            output.newLine();
            output.flush();
        } catch (IOException ioe) {
            JOptionPane
                    .showMessageDialog(this, "Error sending message: " + ioe);
        }
    }

    /**
     * Starts the client.
     * 
     * @param args
     *            not used
     */
    public static void main(String[] args) {
        ChatClient application = new ChatClient();
        application.setDefaultCloseOperation(EXIT_ON_CLOSE);
        application.connect(); // Connect to the server
        application.processConnection(); // Start processing messages from the
                                         // server
    }
}
