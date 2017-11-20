package no.ntnu.imt3281.ludo.server;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * ****************This code is borrowed from okolloen**************************
 * Multithreaded chat server. Runs three different threads, one for login requests,
 * one for reading incoming messages and one for sending messages out again.
 * 
 * All messages received are sent to all different clients. 
 * 
 * All communications are shown in a status area (the only GUI element) of the application.
 * 
 * @author okolloen
 *
 */
public class ChatServer extends JFrame {
    private JTextArea status;
    private ArrayList<Client> clients = new ArrayList<ChatServer.Client>();
    private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);
    private ServerSocket server;
    private ExecutorService executorService;
    private boolean shutdown = false;
    

    /**
     * Sets up the GUI and starts the threads.
     * Adds a textarea to the window and starts the three threads of the application.
     * One thread is monitoring the server socket and creates a new Client object
     * each time a connection is made. This Client object is added to the list of clients.
     * The message sender thread monitors the array blocking queue "messages" and sends
     * all messages on that queue to all the connected clients.
     * The message listener thread goes through the list of clients and checks each one
     * in turn to see if any messages have been received. If a message has been received 
     * that message is put on the queue to be handled by the message sending thread.
     */
    public ChatServer(String chatname, int ID) {
        super("Chat server: "+chatname);
        
        //this.ID = ID;	// nødvendig?

        // Sets up the status area where all communications is logged.
        status = new JTextArea();
        status.setFont(new Font("Arial", Font.PLAIN, 26));
        status.setEditable(false);
        add(new JScrollPane(status));

        try {
            server = new ServerSocket(12345);
            executorService = Executors.newCachedThreadPool();
            startLoginMonitor();		// Handle login requests in a separate thread
            startMessageSender();		// Send same message to all clients, handled in a separate thread
            startMessageListener();		// Check clients for new messages
            executorService.shutdown();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        setSize(600, 400);
        setVisible(true);
    }

    private void startMessageListener() {
        executorService.execute(() -> {
            while (!shutdown) {
                try {
                	synchronized (clients) {	// Only one thread at a time might use the clients object 
	                    Iterator<Client> i = clients.iterator();
	                    while (i.hasNext()) {
	                        Client c = i.next();
	                        try {
		                        String msg = c.read();
		                        if (msg != null && !msg.equals(">>>LOGOUT<<<"))
		                            messages.put(c.name+"> "+msg);
		                        else if (msg != null) {	// >>>LOGOUT<<< received, remove the client
		                            i.remove();
		                            messages.put("LOGOUT:"+c.name);
		                            messages.put(c.name+" logged out");
		                        }
	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
	                        	i.remove();
	                            messages.put("LOGOUT:"+c.name);
	                            messages.put(c.name+" got lost in hyperspace");
	                        }
	                    }
                	}
                } catch (InterruptedException ie) {
                	ie.printStackTrace();
                } 
           }
        });
    }

    private void startMessageSender() {
        executorService.execute(() -> {
            while (!shutdown) {
                try {
                    String message = messages.take();
                    displayMessage("Sending '" + message + "' to "
                            + clients.size() + " clients\n");
                    synchronized (clients) {		// Only one thread at a time might use the clients object
	                    Iterator<Client> i = clients.iterator();
	                    while (i.hasNext()) {
	                        Client c = i.next();
	                        try {
	                        	c.sendText(message);
	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
	                        	i.remove();
	                        	messages.add("LOGOUT:"+c.name);
	                        	messages.add(c.name+" got lost in hyperspace");
	                        }
	                    }
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        });
    }

    private void startLoginMonitor() {
        executorService.execute(() -> {
            while (!shutdown) {
                try {
                    Socket s = server.accept();
                    Client c = new Client(s);
                    messages.add (c.name+" joined the conversation");
                    synchronized (clients) {
                    	clients.add(c);
                    	Iterator<Client> i = clients.iterator();
	                    while (i.hasNext()) {		// Send message to all clients that a new person has joined
	                        Client c1 = i.next();
	                        if (c!=c1)
	                        	try {
	                        		c.sendText("LOGIN:"+c1.name);
	                        	} catch (IOException ioelocal) {
	                        		// Lost connection, but doesn't bother to handle it here
	                        	}
	                    }
                    }
                    displayMessage("CLIENT CONNECTED:" + c.name + "\n");
                    try {
                        messages.put("LOGIN:" + c.name);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException ioe) {
                    displayMessage("CONNECTION ERROR: " + ioe + "\n");
                }
            }
        });
    }

    private void displayMessage(String text) {
        SwingUtilities.invokeLater(() -> status.append(text));
    }

   
    /**
     * A new object of this class is created for all new clients.
     * When a socket is created by the serverSockets accept method
     * a new object of this class is created based on that socket.
     * This object will then contain the socket itself, a bufferedReader,
     * a bufferedWriter and the nickname of the user using the connected
     * client.
     * 
     * @author okolloen
     *
     */
    class Client {
        private String name;
        private Socket connection;
        private BufferedReader input;
        private BufferedWriter output;

        /**
         * Construct a new Client object based on the given socket object.
         * A buffered reader and a buffered writer will be created based on the
         * input stream and output stream of the given socket object. Then
         * the nickname of the user using the connecting client will be read.
         * If no LOGIN:username message can be read from the client
         * an IOException is thrown. 
         * 
         * @param connection the socket object from the server sockets accept call.
         * @throws IOException if any errors occurs during the initial IO operations
         */
        public Client(Socket connection) throws IOException {
            this.connection = connection;
            input = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(
                    connection.getOutputStream()));
            name = input.readLine();
            if (!name.startsWith("LOGIN:"))
                throw new IOException("No login received from client");
            name = name.substring(6);
        }

        /**
         * Closes the buffered reader, the buffered writer and the socket
         * connection.
         * 
         * @throws IOException
         */
        public void close() throws IOException {
            input.close();
            output.close();
            connection.close();
        }

        /**
         * Send the given message to the client. Ensures that all messages
         * have a trailing newline and are flushed.
         * 
         * @param text the message to send
         * @throws IOException if an error occurs when sending the message 
         */
        public void sendText(String text) throws IOException {
            output.write(text);
            output.newLine();
            output.flush();
        }

        /**
         * Non blocking read from the client, if no data is available then null 
         * will be returned. Checks to see if a line can be read from the client
         * and if so reads and returns that line (message). If no message is 
         * available null is returned.
         * 
         * @return a String with message if available, otherwise null
         * @throws IOException if an error occurs during reading
         */
        public String read() throws IOException {
            if (input.ready())
                return input.readLine();
            return null;
        }
    }
} 