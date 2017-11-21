package no.ntnu.imt3281.ludo.server;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import no.ntnu.imt3281.ludo.server.ChatServer.Client;

/**
 * Master-server who controls DB, chats & games  
 * 
 */
public class ServerController {

	/** The 'url' to our database (local) */
	private String url = "jdbc:derby:BadgerDB;";
	/** Unique ID for each chat */
	private int chatID = 0;
	private int gameID = 0;
	/** Socket that all communication goes through */
	private ServerSocket serverSocket;
	/** ArrayList wit all logged in clients */
	private ArrayList<Client> clients = new ArrayList<Client>();
	/** Array list of all chats */
	private ArrayList<Chat> chats = new ArrayList<Chat>();
	/** Array list of all games */
	private ArrayList<Game> games = new ArrayList<Game>();
	   
	
	
	private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);
    private ServerSocket server;
    private ExecutorService executorService;
    private boolean shutdown = false;
    private JTextArea status;
    private Database db = null;
	
	/** Starts up an object of ServerController */
	public static void main(String[] args) {
		ServerController servercontroller = new ServerController();
	}
	
	
	public ServerController() {		
		
		try {
			db = new Database(url);			// tries to connect to DB	
		} catch(SQLException sqle) {			
			url += "create=true";				
			sqle.printStackTrace();
			try {
				db = new Database(url);	// if DB not found, adds "create=true" to make DB
			} catch(SQLException sqle2) {
				System.err.println("No DB after 2nd try"); 
				sqle2.printStackTrace();
			}
		}
		
		// ServerController have only one masterChat
		Chat masterChat = new Chat(1);
		chats.add(masterChat);
		
		try {
            serverSocket = new ServerSocket(12345);
            executorService = Executors.newCachedThreadPool();
            startLoginMonitor();		// Handle login requests in a separate thread
            startMessageSender();		// Send same message to all clients, handled in a separate thread
            startMessageListener();		// Check clients for new messages
            executorService.shutdown();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }
		
		
	}
	
	private void startLoginMonitor() {
        executorService.execute(() -> {
            while (!shutdown) {
                try {
                    Socket s = serverSocket.accept();
                    Client newClient = new Client(s);
                    synchronized (clients) {
                    	String msg = newClient.read();
                    	String[] parts = msg.split(",");   
                    	String type = parts[0];
	                    String operation = parts[1];
	                    String userName = parts[2];
	                    String pwd = parts[3];

	                	if(type.equals("LOGIN")) {
	                		if(operation.equals("0")) {		
	                			if(db.addUser(userName, pwd)) {		// sends report back to client: 
	                				newClient.sendText("LOGIN,0,TRUE");
	                			} else { 
	                				newClient.sendText("LOGIN,0,FALSE");
	                			}
	                		}
		                	else if(operation.equals("1")) {		// Log in
		                		int id = db.checkLogin(userName, pwd);
		                		if(id != -1) {
		                			newClient.setId(id);
		                			clients.add(newClient);
		                			chats.get(0).addParticipant(newClient);	// Legger Klient til i masterChat 
		                			newClient.sendText("LOGIN,1,TRUE");		// sends report back to client:s
		                		} else {
		                			newClient.sendText("LOGIN,1,FALSE");
		                		}
		                		
		                		Iterator<Client> i = clients.iterator();
			                    while (i.hasNext()) {					// Send message to all clients that a new person has joined
			                        Client c1 = i.next();
			                        if (newClient!=c1) {      		// TODO sjekk denne          	
			                        	try {
			                        		newClient.sendText("CHAT,1,"+userName+","+userName+"logged inn");	
			                        	} catch (IOException ioelocal) {
			                        		// TODO fiks exception handling
			                        	}
			                        }
			                    }	// While slutt, sagt i fra til alle
		                	}	// faktisk Logg inn ferdig
	                	} 	// Logg inn type ferdig
                    	
                    }	// Sync ferdig
                 
                    displayMessage("CLIENT CONNECTED:" + newClient.ID + "\n");
                    try {
                        messages.put("LOGIN:" + newClient.ID);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException ioe) {
                    displayMessage("CONNECTION ERROR: " + ioe + "\n");
                }
            }
        });
    }
	
	private void sendMessageTo() {
		
	}

	private void startMessageListener() {		// En ny melding har dukket opp på stacken
        executorService.execute(() -> {			// Thread 
            while (!shutdown) {
                try {
                	synchronized (clients) {	// Only one thread at a time might use the clients object 
	                    Iterator<Client> i = clients.iterator();	// Iterate throug all clients
	                    	// TODO spm, må man gå igjennom alle clientene for å finne meldingen som er sendt?? ref Øyvind eks
	                    while (i.hasNext()) {			
	                        Client curClient = i.next();		// TODO SA - hopper over første?
	                        try {
	                        	String msg = curClient.read();		// Leser inn meldingen 
	                        	String[] parts = msg.split(",");   	// Splitter den opp på , komma
	                        	// TODO id til "fra" client ok ?
	                        	int fromClientID = curClient.getId();
	                        	String type = parts[0];
	    	                    int idNr = Integer.parseInt(parts[1]);		//IDnr til rom eller game
	    	                    String info= parts[2];
	    	                    String message = parts[3];
	    	                    // eks CHAT,3,0,msg
	    	                    // 	   type idRom/game, info??trengs?, melding
	    	                	if (type.equals("CHAT")) {					// Hvis meldingen er av typen CHAT
	    	                		// 1. finn riktig chat 
	    	                		// 2. finn riktige  deltagere
	    	                		// 3. send info til disse 
	    	                		Iterator<Chat> chatNri = chats.iterator();		// Iterer gjennom alle chatte rom
				                    while (chatNri.hasNext()) {					// hvis flere
				                        Chat curChat = chatNri.next();			// Hvilken sjekkes nå
				                        if (idNr==curChat.getId()) {   			// Dersom riktig chatterom
				                        	
				                        
				                        	// Iterere gjennom aktuelle klienter i riktig chat
				                        	Iterator<Client> clientNri = curChat.participants.iterator();
				                        	// Det samme ?? Iterator<Client> clientNri = curChat.getParticipants().iterator();
				                        	while (clientNri.hasNext()) {			// For hver client i aktuelt chatte rom
						                        Client curCli = clientNri.next();	
					                        	try {						// Prøv å send en melding
					                        								// Format: CHAT idTilChat, FraClientID, Melding
					                        		curCli.sendText("CHAT,"+curChat.getId()+","+fromClientID+","+message);	
					                        	} catch (IOException ioelocal) {
					                        		// TODO fiks exception handling
					                        	}
				                        	}
				                        }
				                    }	// While chat slutt, sjekket alle
		                        	
		                        }
		                        else if (type.equals("GAME")) {
		                        	// finn riktig chat 
	    	                		// finn aktuele deltagere
	    	                		// send info til disse 
		                        }
		                        
	    	                	
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
                    String txt = messages.take();
                    String[] parts = txt.split(",");
                    String type = parts[0];
                    int id = Integer.getInteger(parts[1]);	// cast to integer
                    String client = parts[2];	
                    
                    String message, command;
                    if(type.equals("CHAT")) {
                    	message = parts[3];	 // riktig nr?
                    }
                    else if(type.equals("GAME")) {
                    	command = parts[3];
                    }
                    
                    synchronized (clients) {		// Only one thread at a time might use the clients object
                    	if(type.equals("CHAT")) {
                    		Iterator<Chat> i = chats.iterator();
                    		Chat chat = null;
                     														// looks for chat object
                    		while(i.hasNext() && id != chat.getId()) {		//   with correct ID
                    			chat = i.next();
                    		}
                    		if(id == chat.getId()) { 
                    			chat.sendChatMessage(client+": "+message);	// sends message to all in chatroom	
                    		}
                    	}
                    	else if(type.equals("GAME")) {
                    		Iterator<Game> i = games.iterator();
                    		Game game = null;
                    		
                    		while(i.hasNext() && id != game.getId()) {
                    			game = i.next();
                    		}
                    		
                    	}
                    } // synchronized
               
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
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
        private int ID;
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
        }
        
        /**
         * Sets ID 
         * @param ID
         */
        public void setId(int ID) {
        	this.ID = ID; 
        }
        
        /**
         * Get Client's id
         * @return
         */
        public int getId() {
        	return ID; 
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
	
    
    class Chat {
    	// private String chatName;
    	private int ID;
    	private Vector<Client> participants;
    	
    	public Chat(int ID) {   	 
    		this.ID = ID; 			//participantID.add(ID);			
    	}
    	
    	/**
    	 * get
    	 * @return id for this chat
    	 */
    	public int getId() {
    		return ID;
    	}
    	
    	public void addParticipant(Client c) {
    		participants.add(c);
    	}
    	public void removeParticipant(Client c) {
    		participants.removeElement(c);
    	}
    	public Vector<Client> getParticipants() {
    		return participants; 
    	}
    	
    	public boolean sendChatMessage(String msg) {
    		Iterator<Client> i = participants.iterator();
    		while(i.hasNext()) {
    			Client client = i.next();
    			try {
    				client.sendText(msg);
    			} catch(IOException ioe) {
    				i.remove();
                	messages.add("LOGOUT:"+client.ID);
                	messages.add(client.ID+" got lost in hyperspace");
    			}
    		}
    	}
    	
    }
    
    class Game {
    	private int ID;
    	private int relatedChatId;
    	private Vector<Client> participants;
    	
    	public Game(int ID) {
    		this.ID = ID; 	
    	}
    	public int getId() {
    		return ID;
    	}
    	public void setThisGamerelatedChatId(int chatID) {
    		this.relatedChatId = chatID; 	
    	}
    	public int getThisGamerelatedChatId() {
    		return relatedChatId; 	
    	}
    	
    	public void addParticipant(Client c) {
    		participants.add(c);
    		
    	}
    	public void removeParticipant(Client c) {
    		participants.removeElement(c);
    		
    	}
    	public Vector<Client> getParticipant() {
    		return participants; 
    	}
    	// TODO, trengs en funskjon for kommunikasjon?
    }
	
	
	
	
	
}