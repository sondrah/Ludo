package no.ntnu.imt3281.ludo.server;


import java.awt.Font;
import java.awt.Label;
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
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.derby.impl.sql.catalog.SYSROUTINEPERMSRowFactory;

import no.ntnu.imt3281.ludo.logic.Ludo;

/**
 * Master-server who controls DB, chats & games  
 * 
 */
public class ServerController extends JFrame {

	/** The 'url' to our database (local) */
	private String url = "jdbc:derby:BadgerDB;";
	/** Unique ID for each game */
	private int gameID = 0;
	/** How many playsers are waiting for randoom game*/
	private int waitingPlayers = 0;
	/** Socket that all communication goes through */
	private ServerSocket serverSocket;
	/** ArrayList wit all logged in clients */
	private ArrayList<Client> clients = new ArrayList<Client>();
	/** Array list of all chats */
	private ArrayList<Chat> chats = new ArrayList<Chat>();
	/** Array list of all games */
	private ArrayList<Game> games = new ArrayList<Game>();
	/** List which holds messages waiting to be sent */
	private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);
    /** Makes threads for different listeners */
	private ExecutorService executorService;
	
    private boolean shutdown = false;
    private JTextArea status;
    private Database db = null;
	
	/** Starts up an object of ServerController */
	public static void main(String[] args) {
		ServerController servercontroller = new ServerController();
	}
	
	/**
	 * Starts up the server with database and different listeners.
	 */
	public ServerController() {		
		
		status = new JTextArea();
        status.setFont(new Font("Arial", Font.PLAIN, 26));
        status.setEditable(false);
        add(new JScrollPane(status));
		
		try {
			db = new Database(url);		// tries to connect to DB	
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
		
		Chat masterChat = new Chat(1);	// Sets up master-chat-room
		chats.add(masterChat);
		
		try {
            serverSocket = new ServerSocket(12345);
            executorService = Executors.newCachedThreadPool();
            startLoginMonitor();		// Handle login requests in a separate thread
            startMessageSender();		// Send same message to all clients, handled in a separate thread
            startMessageListener();		// Check clients for new messages
            executorService.shutdown();
        } catch (IOException ioe) {
        	System.err.println("No ServerSocket"); 
            ioe.printStackTrace();
            System.exit(1);
        }
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 400);
        setVisible(true);
	}
	
	private void startLoginMonitor() {
        executorService.execute(() -> {
            while (!shutdown) {
                try { 						// LOGIN,0,use
                    Socket s = serverSocket.accept();
                    Client newClient = new Client(s);
                    synchronized (clients) {
                    	String msg = newClient.read();
                    	while(msg == null) {
                    		TimeUnit.MILLISECONDS.sleep(1);
                    		msg = newClient.read();
                    	}
                    	System.out.println("Inne i login listener");
                    	
                    	
                    	String[] parts = msg.split(",");   
                    	String type = parts[0];
	                    String operation = parts[1];
	                    String userName = parts[2];
	                    String pwd = parts[3];

	                	if(type.equals("LOGIN")) {
	                		if(operation.equals("0")) {		// Registrer new user 
	                			if(db.addUser(userName, pwd)) {				// sends report back to client: 
	                				int newId = db.getUserID(userName);		// henter ID i db
	                														// Går ikke pga ligger ikke i clients liste messages.put(
	                				try {
	                					newClient.sendText("LOGIN,0,"+newId+",Du er registret og kan nå logge inn");	// !! I18N
		                        	} catch (IOException ioelocal) {
		                        		// !! fiks exception handling
		                        	}
	                			} else { 
	                				try {
	                					newClient.sendText("LOGIN,0,0,Gikk ikke");	// Sender melding utenom buffer messages
	                				} catch (IOException ioelocal) {
		                        		// !! fiks exception handling
		                        	}
	                			}
	                		}
		                	else if(operation.equals("1")) {		// Log in
		                		int idClient = db.checkLogin(userName, pwd);
		                		if(idClient != -1) {
		                			newClient.setId(idClient);
		                			clients.add(newClient);
		                			chats.get(0).addParticipant(newClient);	// Legger Klient til i masterChat 
		                			// newClient.sendText("LOGIN,1,TRUE");		// sends report back to client:s
		                			messages.put("LOGIN,1,"+idClient+","+userName+ " (Du) er logget inn");
		                			// Token 
		                			/*
		                			 * Brukernavn 
		                			 * token, server 
		                			 * Lagres fil på med preferences 
		                			 * Kobler seg til på nytt = ny token 
		                			 * tid 
		                			 * session 
		                			 */
			                		Iterator<Client> i = clients.iterator();
				                    while (i.hasNext()) {					// Send message to all clients that a new person has joined
				                        Client c1 = i.next(); 
				                        try {
				                        	messages.put("CHAT,1,"+idClient+","+userName+" har blitt med i MasterChat");	
				                        } catch (InterruptedException e) {
				                            e.printStackTrace();
				                        }
				                    }								// While slutt, sagt i fra til alle
		                		} else {
		                			messages.put("LOGIN,0,"+idClient+",Du er IKKE logget inn");
		                		}
		                	}	// faktisk Logg inn ferdig
	                	} 	// Logg inn type ferdig
                    	
                    }	// Sync ferdig
                 
                    // DB Loggføring??
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        // displayMessage("CONNECTION ERROR: " + ioe + "\n");
                    } catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            }
        });
    }
	

	private void startMessageListener() {	
		// Listener = gå igjennom alle clientene for å finne ut OM det er en meldingen som er sendt
        executorService.execute(() -> {			// Thread 
            while (!shutdown) {
                try {
                	synchronized (clients) {	// Only one thread at a time might use the clients object 
	                    Iterator<Client> i = clients.iterator();	// Iterate throug all clients
	                    // TODO Stream clients of si for each, bruker da en paralell thread??
	                    while (i.hasNext()) {			
	                        Client curClient = i.next();			// ??SA - hopper over første?
	                        try {
	                        	String msg = curClient.read();		// Leser inn meldingen 
	                        	if (msg != null) {
		                        	String[] parts = msg.split(",");   	// Splitter den opp på , komma
		                    
		                        	int fromClientID = curClient.getId();
		                        	String type = parts[0];
		    	                    int idNr = Integer.parseInt(parts[1]);		//IDnr til rom eller game
		    	                    String info= parts[2];
		    	                    String message = parts[3];
		    	                    											// eks CHAT,3,0,msg
		    	                    											// 	   type idRom/game, info??trengs?, melding
		    	                	if (type.equals("CHAT")) {					// Hvis meldingen er av typen CHAT
		    	                		String userName = db.getUserName(fromClientID);		    	                		
		    	                		if(idNr ==0) {							// New Chat
		    	                			int newChatId = newChat(message);
		    	                			messages.put("CHAT,"+newChatId+","+fromClientID+",0"+message);	
		    	                		}
		    	                		
		    	                		else {			// Allerede eksisterende chat 
		    	                			Iterator<Chat> chatNri = chats.iterator();		// Iterer gjennom alle chatte rom
		    	                			while (chatNri.hasNext()) {					// hvis flere
						                        Chat curChat = chatNri.next();			// Hvilken sjekkes nå
						                        if (idNr==curChat.getId()) {   			// Dersom riktig chatterom  
						                        	// SONDRE Sondre lag unik her else if 
						                        	//curChat.addParticipant(message - unikt tall);
						                        										// Iterere gjennom aktuelle klienter i riktig chat
						                        	Iterator<Client> clientNri = curChat.participants.iterator();
						                        	while (clientNri.hasNext()) {			// For hver client i aktuelt chatte rom
								                        Client curCli = clientNri.next();
								                       // if ( det har endra ) put. en annen melding med "added brukernavn ( message) 
								                        messages.put("CHAT,"+curChat.getId()+","+fromClientID+","+userName+" > " +message);							                      
						                        	}
						                        }
		    	                			}	// While chat slutt, sjekket alle
		    	                		}
			                        }
			                        else if (type.equals("GAME")) {
			                        	if(idNr ==0) {							// New Game
			                        		if (nrOfPlayerWaitingRandomGame(1) > 1) {	// Legger en til i ventelista, dersom da flere enn 1 start
			                        			// TODO wait for more playser 5 sek 
			                        			// If not, start anyway
				                        		
				    	                		int currentGameID  = gameID ++; 		// Tildeler game id 
				    	                		int newChatId = newChat("ChatForGame"+currentGameID);
				    	                		Game newGame = new Game(currentGameID, newChatId);		// Oppretter ny chat i server
				    	                		games.add(newGame);						// Legger denne til i serverens chat liste 
				    	                		messages.put("GAME,"+currentGameID+","+fromClientID+",0 HAR STARTET"+message);	
				    	                								// Sender tilbake riktig chat nr til client som oprettet den
			                        		}
			                        	}
		    	                		else {			// Allerede eksisterende chat 
		    	                			//Iterator<Chat> chatNri = chats.iterator();		// Iterer gjennom alle chatte rom
		    	                			// TODO lag funk som gjør ALT med game
		    	                		} 
			                        }
	                        	} 	// If msg excits end
	    	     
	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
	                        	// clientNri.remove();
	                          
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
                    String id = parts[1];			// LOGIN = 0 ( reg), 1 (login) 
                    								// CHAT = aktuelt chat rom
                    								// GAME = idGame
                    int clientId = Integer.parseInt(parts[2]);	// cast to integer ID client   
                    String message = parts[3];
                    synchronized (clients) {		// Only one thread at a time might use the clients object
                    	
                    	if(type.equals("LOGIN") || type.equals("CHAT")) {
                    		Iterator<Client> i = clients.iterator();
                    		boolean foundClient = false;
                    		// lag funksjon som finner riktig client!! 
							while(i.hasNext() && !foundClient ) {
                    			Client tempCli = i.next();
                    			if (clientId == tempCli.getId())
                    				foundClient = true;				// Fant client, trenger ikke lete mer
	                    			 try {
	                    				 tempCli.sendText(type+","+id+","+clientId+","+message);
	     	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
	     	                        	// i.remove();
	     	                        	// messages.add("LOGOUT:"+c.name);
	     	                        	
	     	                        }
                    		}
                    		
                    	}
                    	else if(type.equals("GAME")) {
                    		Iterator<Game> i = games.iterator();
                    		Game game = null;
                    		
                    		while(i.hasNext()) {
                    			game = i.next();
                    		}
                    		
                    	}
                    	else {
                    		System.out.println("Nå har server melinga gått til *******");
                    	}

                    } // synchronized
               
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        });
    }
    
	/**
	 * Make new chat 
	 * @param chatName
	 * @return chat id or 0 if not success 
	 */
	private int newChat(String chatName) {
		db.addChat(chatName);				// Legger chatten til i db
		int newChatId = db.getChatID(chatName); // Henter ut Chat id
		if (newChatId != -1) {		// Hvis chat fantes i db
			Chat newChat = new Chat(newChatId);		// Oppretter ny chat i server
			chats.add(newChat);						// Legger denne til i serverens chat liste
		}
		else newChatId = 0;
		return newChatId;
	}
	
	/**
	 * nrOfPlayerWaitingRandomGame
	 * If one more waiting send 1
	 * @return waitingPlayers
	 */
	private int nrOfPlayerWaitingRandomGame(int oneMoreORcheck) {
		waitingPlayers = waitingPlayers + oneMoreORcheck;
	
		return waitingPlayers;
	}
	
	
	/**
     * --Borrowed code from okolloen--
     * A new object of this class is created for all new clients.
     * When a socket is created by the serverSockets accept method
     * a new object of this class is created based on that socket.
     * This object will then contain the socket itself, a bufferedReader,
     * a bufferedWriter and the nickname of the user using the connected
     * client.
     */
    class Client {
        private int ID;
        private Socket connectionClient;
        private BufferedReader input;
        private BufferedWriter output;

        /**
         * --Borrowed code from okolloen--
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
            this.connectionClient = connection;
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
         * @throws IOException if an error occurs
         */
        public void close() throws IOException {
            input.close();
            output.close();
            connectionClient.close();
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
	
    
    /**
     * Each object of this class represent a chat-room.
     * 
     *
     */
    class Chat {
    	// private String chatName;
    	private int ID;
    	private Vector<Client> participants;
    	
    	/**
    	 * Construct a new Chat object supplied with chatID
    	 * @param ID 
    	 */
    	public Chat(int ID) {   	 
    		this.ID = ID; 			//participantID.add(ID);		
    		participants = new Vector<>();
    	}
    	
    	/**
    	 * Gets chat's ID
    	 * @return id for this chat
    	 */
    	public int getId() {
    		return ID;
    	}
    	
    	/**
    	 * Add a client to this chat-room
    	 * @param c which client to be added
    	 */
    	public void addParticipant(Client c) {
    		participants.add(c);
    	}
    	
    	/**
    	 * Removes a client from this chat-room
    	 * @param c which client to be removed
    	 */
    	public void removeParticipant(Client c) {
    		participants.removeElement(c);
    	}
    	
    	/**
    	 * Returns all clients in this chat-room in a vector
    	 * @return participants in form of a vector
    	 */
    	public Vector<Client> getParticipants() {
    		return participants; 
    	}
    	
    	/* 	public boolean sendChatMessage(String msg) {
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
    		    }*/
    	}
    	

    /**
     * Each object of this class represents the servers version
     * of a Ludo game. 
     *
     */
    class Game extends Ludo{
    	private int ID;
    	private int relatedChatId;
    	private Socket connectionGame;
        private BufferedReader inputGame;
        private BufferedWriter outputGame;
    	private Vector<Client> participants;
    	
    	/**
    	 * Construct a game of Ludo supplied with a game-ID
    	 * @param ID 
    	 */
    	public Game(int gameId, int chatID ) {
    		super();
    		this.ID = gameId; 	
    		this.relatedChatId = chatID;
    		
    	}
    	/**
         * --Borrowed code from okolloen--
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
        public Game(Socket connection) throws IOException {
            this.connectionGame = connection;
            inputGame = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            outputGame = new BufferedWriter(new OutputStreamWriter(
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
         * @throws IOException if an error occurs
         */
        public void close() throws IOException {
        	inputGame.close();
            outputGame.close();
            connectionGame.close();
        }

        /**
         * Send the given message to the client. Ensures that all messages
         * have a trailing newline and are flushed.
         * 
         * @param text the message to send
         * @throws IOException if an error occurs when sending the message 
         */
        public void sendText(String text) throws IOException {
            outputGame.write(text);
            outputGame.newLine();
            outputGame.flush();
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
            if (inputGame.ready())
                return inputGame.readLine();
            return null;
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
    	
    }
	
	
}