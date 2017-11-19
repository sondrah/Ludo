package no.ntnu.imt3281.ludo.server;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import javafx.beans.property.adapter.ReadOnlyJavaBeanObjectProperty;
import no.ntnu.imt3281.ludo.client.ChatClient;
import no.ntnu.imt3281.ludo.logic.NotEnoughPlayersException;
import no.ntnu.imt3281.ludo.server.ChatServer.Client;

/*
 * Server for a specific chat. A game chat, master chat or a private chat.
 * @clients list of people connected to this chat
 * @messages all the messages that are in buffer ready to be sent
 * @serverSocket socket which communicates with the different clients' "chat client"
 */
public class TheChatServer extends JFrame {
	
	private JTextArea status;
    private ArrayList<Client> clients = new ArrayList<TheChatServer.Client>();
    private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean shutdown = false;
    
    private String chatname;
    private int ID;

    
    public TheChatServer(String chatname, int ID) {
    	super(chatname);
    	
    	this.chatname = chatname; 		
    	this.ID = ID;
    	
    	//* Dette kan eventuellt fjernes senere når vi vet at ChatServer fungerer
    	// Sets up window which shows logged communication
    	status = new JTextArea();
        status.setFont(new Font("Arial", Font.PLAIN, 26));
        status.setEditable(false);
        add(new JScrollPane(status));
    	//*/
        
    	try {
    		serverSocket = new ServerSocket(12345);
    		executorService = Executors.newCachedThreadPool();
    		startLoginMonitor();
    		startListener();
    		startSender();
    	} catch(IOException ioe) {
    		ioe.printStackTrace();
    	}
    	
    	setSize(600, 400);
        setVisible(true);
    }
    
    
    private void startListener() {
    	executorService.execute(() -> {
    		while(!shutdown) {
    			try {
    				synchronized(clients) {		// Only one thread has access to clients object at a time
    					Iterator<Client> i = clients.iterator();
    					while(i.hasNext()) {
    						Client client = i.next();
    						try {
    							Chat chat = (Chat) client.read();
    							if(this.ID == chat.getID()) {
    								String msg = chat.getMessage();		
    								if (msg != null && !msg.equals(">>>LOGOUT<<<")) {		
    									messages.put(client.name+"> " + msg);
    								} else if(msg != null) {
    									messages.put("LOGOUT:" + client.name);
    		                            messages.put(client.name + " got lost in hyperspace");
    								}
    							} else {
    								System.err.println("This chat id: " + this.ID);
    								System.err.println("Chat object id: " + ID);
    							}						// no connection to client
    						} catch(Exception e) { 		// sent to wrong chat
    							i.remove();
    							messages.put("LOGOUT:" + client.name);		// TODO 
	                            messages.put(client.name + " logged out");	// hvis feil ID, fortsatt log ut?
    						}
    					}
    				}
    			} catch(InterruptedException ie) {
    				ie.printStackTrace();
    			}
    		}
    	});	
    }
    
    
    /*
     * 
     */
    class Client {
        private String name;
        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        private Chat chat;
        
        
        /**
         * Construct a new Client object based on the given socket object.
         * A buffered reader and a buffered writer will be created based on the
         * input stream and output stream of the given socket object. Then
         * the nickname of the user using the connecting client will be read.
         * If no LOGIN:username message can be read from the client
         * an IOException is thrown. 
         * 
         * @param socket the socket object from the server sockets accept call.
         * @throws IOException if any errors occurs during the initial IO operations
         */
        public Client(Socket socket) throws IOException {	//FIXME
            this.socket = socket;
            
            
            // Mangler å hente navn til tilkoblet client
            
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            
            Object obj = null;
			try {
				obj = input.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 
			
            if (obj != null && obj instanceof Chat) {
            	
            }
        }

        /**
         * Closes the buffered reader, the buffered writer and the socket
         * socket.
         * 
         * @throws IOException
         */
        public void close() throws IOException {
            input.close();
            output.close();
            socket.close();
        }

        public Object read() throws Exception {
        	Object obj = input.readObject();
        	
        	if(obj == this) {
    			return true;
    		}
 
            if (obj != null && obj instanceof Chat) {
             	return (Chat) obj;
            } else {
            	throw new Exception("Didn't retrieve a Chat object");
            }
        }
  
        
    }
    
}
