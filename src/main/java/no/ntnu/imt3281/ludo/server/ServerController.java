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
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.derby.impl.sql.catalog.SYSROUTINEPERMSRowFactory;
import org.apache.derby.tools.sysinfo;

import no.ntnu.imt3281.ludo.logic.Ludo;
import no.ntnu.imt3281.ludo.Logging;

/**
 * Master-server who controls DB, chats & games  
 * 
 */
public class ServerController extends JFrame {

	/** Logs all chat messages*/
	Logger chatlogger = Logger.getLogger("BadgerLogg");  
    /** Sets up file for 'chatlogger' to write to */
	FileHandler fileHandler;
	/** The 'url' to our database (local) */
	private String url = "jdbc:derby:BadgerDB;";
	/** Unique ID for each game, zeroed for each server start */
	private int gameID = 1;
	/** Witch players are waiting for randoom game*/
	private ArrayList<Client> waitingClients = new ArrayList<Client>();
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
		
		Logging.setup();
		
		status = new JTextArea();
        status.setFont(new Font("Arial", Font.PLAIN, 26));
        status.setEditable(false);
        add(new JScrollPane(status));
		
		try {
			db = new Database(url);		// tries to connect to DB	
		} catch(SQLException sqle) {			
			url += "create=true";				
			Logging.log(sqle.getStackTrace());
			try {
				db = new Database(url);	// if DB not found, adds "create=true" to make DB
			} catch(SQLException sqle2) {
				Logging.log(sqle.getStackTrace());
			}
		}
		
		// masterchat id
		int mc = 1;
		if(db.getChatID("MasterChat") == -1) {
			db.addChat("MasterChat");
			mc = db.getChatID("MasterChat");
		}
		
		Chat masterChat = new Chat(mc);	// Sets up master-chat-room
		chats.add(masterChat);
		
		try {
            serverSocket = new ServerSocket(12345);
            executorService = Executors.newCachedThreadPool();
            startLoginMonitor();		// Handle login requests in a separate thread
            startMessageSender1();		// Send same message to all clients, handled in a separate thread
            startMessageListener1();		// Check clients for new messages      
            
            fileHandler = new FileHandler("./src/main/java/no/ntnu/imt3281/ludo/chatlogg.log");	// path to log file
            chatlogger.addHandler(fileHandler);
            
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            
            executorService.shutdown();
        } catch (IOException ioe) {
        	Logging.log(ioe.getStackTrace());
            System.exit(1);
        } catch (SecurityException e) {  
        	Logging.log(e.getStackTrace()); 
        }
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 400);
        setVisible(true);
	}
	
	private void startLoginMonitor() {
        executorService.execute(() -> {
            while (!shutdown) {
                try {
                    Socket s = serverSocket.accept();
                    Client newClient = new Client(s);
                    synchronized (clients) {
                    	String msg = newClient.read();
                    	
                    	while(msg == null) {
                    		TimeUnit.MILLISECONDS.sleep(1);
                    		msg = newClient.read();
                    	}
                    	// REGISTER,username,password
                		// LOGIN,username,token
                		// REGISTER,TRUE
                		// REGISTER,FALSE
                		// LOGIN,TRUE,id,token
                		// LOGIN,FALSE
                    	
                    	String[] parts = msg.split(",");   
                    	String action = parts[0];
	                    String username = parts[1];
	                    String password = parts[2];
	                    System.out.println("0. Inne i login listener for user: "+ username);
	                    
	                    if(action.equals("REGISTER")) {
	                    	String message = "";
	                    	
	                    	System.out.println("0.1 Register");
	                    	
	                    	if(db.addUser(username, password)) {
	                    		message = "REGISTER,TRUE";
	                    	} else {
	                    		message = "REGISTER,FALSE";
	                    	}
	                    	
	                    	newClient.sendText(message);
	                    }
	                    else if(action.equals("LOGIN")) {
	                    	int idClient = db.checkLogin(username, password);
	                    	if(idClient != -1) {
	                    		newClient.setId(idClient);
	                    		clients.add(newClient);
	                    		newClient.sendText("LOGIN,TRUE," + idClient);
	                    		
	                    		getChat(1).addParticipantToChat(newClient);
	                    		getChat(1).getParticipants().forEach(client -> {
	                    			try {
	                    				client.sendText("CHAT,JOIN,1," + idClient + ","
	                    						+ username + " har logget inn!");
	                    			}
	                    			catch(IOException ioe) {
	                    				Logging.log(ioe.getStackTrace());
	                    			}
	                    		});
	                    	}
	                    } // if action
                    } // synch
                } 
                catch (IOException ioe) {
                	Logging.log(ioe.getStackTrace());
				}
                catch (InterruptedException ie) {
                	Logging.log(ie.getStackTrace());
                }
            }
        });
    }

	
	private void startMessageListener1() {
		executorService.execute(() ->{
			while(!shutdown) {
				synchronized(clients) {
					clients.parallelStream().forEach(client -> {
						//System.out.println("MessageListener");
						String msg = "";
						try {
							msg = client.read();
						}
						catch (IOException ioe) {
							Logging.log(ioe.getStackTrace());
						}
						
						if(msg != null) {
							System.out.println("1. MsgListener: " + msg);
						
							String[] str = msg.split(",");
							
							switch(str[0]) {
							case "CHAT":
								handleChatMessage(str);
								break;
								
							case "GAME":
								handleGameMessage(str);
								break;
								
							case "REQUEST":
								handleListRequest(str);
								break;
							}
						}
					});
				}
			}
		});
	}
	
	
	private void startMessageSender1() {
		executorService.execute(() ->{
			while(!shutdown) {
				String str = "";
				
				System.out.println("MessageSender");
				try {
					str = messages.take();
					System.out.println("3. MessageSender: " + str);
				}
				catch(InterruptedException ie) {
					Logging.log(ie.getStackTrace());
				}
				
				if(str.startsWith("CHAT")) {
					sendChatMessage(str);
				}
				else if (str.startsWith("GAME")) {
					sendGameMessage(str);
				}
				else if (str.startsWith("REQUEST")) {
					sendListRequestAnswearMessages(str);
				}
				
			}
		});
	}
	

	


	/**
	 * Make new chat 
	 * @param chatname
	 * @return chat id or 0 if not success 
	 */
	private Chat newChat(String chatname) {
		Chat newChat = null;
		
		// tries to get the chatid
		int chatid = db.getChatID(chatname);
		
		// if no chat is found, add a new one
		if(chatid == -1) {
			db.addChat(chatname);
			chatid = db.getChatID(chatname);
		}
	
		// return an instant with the new chatid
		return new Chat(chatid);
	}
	
	/**
	 * playersWaitingForRandomGame
	 * If one more waiting send 1
	 * @return waitingPlayers
	 */
	private int playersWaitingForRandomGame(Client newClient) {
		try{
			waitingClients.add(newClient);
		} catch (IllegalArgumentException moreThanFourPlayers) {
			Logging.log(moreThanFourPlayers.getStackTrace());
		}
		return waitingClients.size();
	}
	

	private Chat getChat(int chatid) {
		Chat theChat = null;
		for(Chat chat : chats) {
			if(chat.getId() == chatid) theChat = chat;
		}
		
		return theChat;
	}
	
	
	private Client getClient(int clientid) {
		Client theClient = null;
		
		for(Client client : clients) {
			if(client.getId() == clientid) theClient = client;
		}
		
		return theClient;
	}
	
	
	private Game getGame(int gameId) {
		Game theGame = null;
		
		for(Game game : games) {
			if(game.getId() == gameId) theGame = game;
		}
		
		return theGame;
	}
	
	
	
	private void handleChatMessage(String[] str) {
		// All strings in the given array has CHAT in str[0]
		// Possible received messages
		// CHAT,SAY,chatid,userid,message		-- Say something
		// CHAT,JOIN,chatid,userid				-- Join a known chat
		// CHAT,JOIN,chatname,userid			-- Join a new chat
		// CHAT,CREATE,chatname,userid			-- Create a new chat
		
		String chatname = null;
		Client client = null;
		
		try {
			System.out.println("2.1 Handle chat: " + str[1]);
			
			switch(str[1]) {
			case "SAY":
				StringBuilder sb = new StringBuilder();
				StringBuilder logtxt = new StringBuilder();	
				
				sb.append(str[0] + ",");
				sb.append(str[1] + ",");
				sb.append(str[2] + ",");
				sb.append(str[3] + ",");
				sb.append(db.getUserName(Integer.parseInt(str[3])));
				sb.append(" > " + str[4]);
				
				System.out.println("2.1.1: " + sb.toString());
				
				// logs on this format -> "username: message"
				logtxt.append(db.getUserName(Integer.parseInt(str[3])));	
				logtxt.append(": ");
				logtxt.append(str[4]);				
				
				chatlogger.info(logtxt.toString());		
				
				// since 'SAY's don't need special treament
				// send it straight to 
				messages.put(sb.toString());
				break;
				
			case "JOIN":
				chatname = str[2];
				int chatid = db.getChatID(chatname);
				client = getClient(Integer.parseInt(str[3]));
				
				if(chatid != -1) {
					getChat(chatid).addParticipantToChat(client);
				} else {
					// creates a new chat and adds the user
					Chat chat = newChat(chatname);
					chat.addParticipantToChat(client);
					chatid = chat.getId();
					messages.put("CHAT,CREATE," + chatid + "," + chatname + " created!");
				}
				
				messages.put("CHAT,JOIN," + chatid + "," + db.getUserName(client.getId()) + " joined the chat!");
				break;
				
			case "CREATE":
				chatname = str[2];
				client = getClient(Integer.parseInt(str[3]));
				Chat chat = newChat(chatname);
				chat.addParticipantToChat(client);
				chats.add(chat);
				
				messages.put("CHAT,CREATE," + chat.getId() + "," + chatname); // 
			}
		}
		catch (InterruptedException ie) {
			Logging.log(ie.getStackTrace());
		}
	}
	
	
	private void sendChatMessage(String str) {
		
		// CHAT,SAY,chatid,userid,message
		// CHAT,JOIN,chatid,'username' joined the chat!
		// CHAT,CREATE,chatid,'chatname' created!
		
		String[] arr = str.split(",", 4);
		
		System.out.println("3.1: Chat id: " + getChat(Integer.parseInt(arr[2])).getId());
		getChat(Integer.parseInt(arr[2])).getParticipants()
		.parallelStream().forEach(client -> {
			try {
				System.out.println("3.1.1");
				client.sendText(str);
			}
			catch(IOException ioe) {
				Logging.log(ioe.getStackTrace());
			}
		});
	}
	
	
	private void handleGameMessage(String[] str) {
		
		// All string arrays that come here have GAME in str[0]
		// possible recieved messages
		// GAME,THROW,gameid,userid					-- Ask server to make a throw
		// GAME,MOVE,gameid,userid,player,from,to	-- 	" 	move a piece
		// GAME,CREATE,userid						-- 	" 	create new game
		
		Game game = null;
		
		System.err.println("3. Handle Game: " + str[2]);
		
		switch(str[1]) {
		case "THROW":
			game = getGame(Integer.parseInt(str[2]));
			int userid = Integer.parseInt(str[3]);
				
			// if the requesting player is the actual active player
			if(db.getUserName(userid).equals(game.getPlayerName(game.activePlayer()))) {
				int dice = game.throwDice();
				
				// Sent message
				// GAME,THROW,gameid,dice
				try {
					messages.put("GAME,THROW," + game.getId() + "," + dice);
				}
				catch(InterruptedException ie) {
					Logging.log(ie.getStackTrace());
				}
			}
			break;
			
		case "MOVE":
			game = getGame(Integer.parseInt(str[2]));
			
			int player = 	Integer.parseInt(str[4]);
			int from   = 	Integer.parseInt(str[5]);
			int to     = 	Integer.parseInt(str[6]);
			
			try {
				if(game.movePiece(player, from, to)) {
					// GAME,MOVE,gameid,TRUE,player,from,to
					messages.put("GAME,MOVE," + game.getId() + ",TRUE,"
								+ player + "," + from + "," + to);
				} else {
					// GAME,MOVE,gameid,FALSE
					messages.put("GAME,MOVE," + game.getId() + ",FALSE");
				}
			}
			catch (InterruptedException e) {
				Logging.log(e.getStackTrace());
			}
			break;
			
		case "CREATE":
			int clientID = Integer.parseInt(str[2]);
			String message = null;
			Client client = getClient(clientID);
			StringBuilder sb = new StringBuilder();
			
			waitingClients.add(client);
			System.out.println("3.0 Ant ventende: "+ waitingClients.size() );
			if(waitingClients.size() >= 2) {
				System.out.println("3.1 Itererer gjennom  " +waitingClients.size()+ " witingclients: game: " + gameID);
				game = new Game(gameID++);
				String chatName = "Game #" + gameID + " chat";
				Chat chat = newChat(chatName);
				
				int i = 0;
				while(waitingClients.size() >= 1 && i < 4) {
					System.out.println("3.2 for hver spiller: "+ waitingClients.size());
					Client c = waitingClients.remove(0);
					String name = db.getUserName(c.getId());
					chat.addParticipantToChat(c);
					game.addParticipantToGame(c);
					sb.append(name + ":");
					game.addPlayer(name);
					waitingClients.trimToSize();
					i++;
				}
				
				games.add(game);
				chats.add(chat);
				
				message = "GAME,CREATE,TRUE," + game.getId() + "," + chat.getId() + "," + sb.toString();
				
			}
			else {
				message = "GAME,CREATE,FALSE,"+clientID+",0";
			}
			
			try {
				messages.put(message);
			}
			catch (InterruptedException ie) {
				Logging.log(ie.getStackTrace());
			}
		}
	}
	
	
	private void sendGameMessage(String str) {
		// GAME,THROW,gameid,dice
		// GAME,MOVE,gameid,TRUE,player,from,to
		// GAME,MOVE,gameid,FALSE
		// GAME,CREATE,TRUE,gameid,players
		// GAME,CREATE,FALSE,clientId
		
		String[] arr = str.split(",");
		String action = arr[1];
		
		if(action.equals("CREATE")) {
			if(arr[2].equals("TRUE")) {			// Dersom Game er laget
				getGame(Integer.parseInt(arr[3])).getParticipants()
				.parallelStream().forEach(client -> {
					try {
						
							client.sendText(str);
						
					} catch (IOException ioe) {
						Logging.log(ioe.getStackTrace());
					}
				});
			}
			else {
				// MSG = GAME,CREATE,FALSE,clientId
				Client c = getClient(Integer.parseInt(arr[3])); 
				try {
					c.sendText(str);
				} catch (IOException ioe) {
					Logging.log(ioe.getStackTrace());
					
				}
			}
		}
		else {
			getGame(Integer.parseInt(arr[2])).getParticipants()
			.parallelStream().forEach(client -> {
				try {
					client.sendText(str);
				} catch (IOException ioe) {
					Logging.log(ioe.getStackTrace());
				}
			});
		}
	}
	

	/**
	 * @param str
	 */
	private void handleListRequest(String[] str) {
		// This should recieve all special requests
		// and should all have str[0] = 'REQUEST'
		
		// INN:
		// REQUEST,LISTPLAYERS,userid
		// REQUEST,LISTCHATS,userid
		
		// OUT:
		// REQUEST,LISTPLAYERS,userid,players
		// REQUEST,LISTCHATS,userid,chats
		
		try {
			switch(str[1]) {
			case "LISTPLAYERS":
				StringBuilder sb = new StringBuilder();
				
				clients.parallelStream().forEach(client -> {
					sb.append(db.getUserName(client.getId()) + ":");
				});
				
				messages.put("REQUEST,LISTPLAYERS," + Integer.parseInt(str[2]) + "," + sb.toString());
				break;
				
			case "LISTCHATS":
				StringBuilder sb1 = new StringBuilder();
				
				clients.parallelStream().forEach(client -> {
					sb1.append(db.getUserName(client.getId()) + ":");
				});
				
				messages.put("REQUEST,LISTCHATS," + Integer.parseInt(str[2]) + "," + sb1.toString());
				break;
			}
		}
		catch (InterruptedException ie) {
			Logging.log(ie.getStackTrace());
		}
	}
	
	
	/**
	 * @param str
	 */
	private void sendListRequestAnswearMessages(String str) {
		
		// REQUEST,LISTPLAYERS,userid,players
		// REQUEST,LISTCHATS,userid,chats
		
		String[] arr = str.split(",", 4);
		
		try {
			getClient(Integer.parseInt(arr[2])).sendText(str);
		}
		catch (IOException ioe) {
			Logging.log(ioe.getStackTrace());
		}
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
     */
    class Chat {
    	private int ID;
    	private Vector<Client> participantsChat;
    	
    	/**
    	 * Construct a new Chat object supplied with chatID
    	 * @param ID 
    	 */
    	public Chat(int ID) {   	 
    		this.ID = ID; 			//participantID.add(ID);		
    		participantsChat = new Vector<>();
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
    	public void addParticipantToChat(Client c) {
    		participantsChat.add(c);
    	}
    	
    	/**
    	 * Removes a client from this chat-room
    	 * @param c which client to be removed
    	 */
    	public void removeParticipant(Client c) {
    		participantsChat.removeElement(c);
    	}
    	
    	/**
    	 * Returns all clients in this chat-room in a vector
    	 * @return participants in form of a vector
    	 */
    	public Vector<Client> getParticipants() {
    		return participantsChat; 
    	}
    }
    	
   
    /**
     * Each object of this class represents the servers version
     * of a Ludo game. 
     *
     */
    class Game extends Ludo {
    	private int ID;
    	private Vector<Client> participantsGame;
    	
    	/**
    	 * Construct a game of Ludo supplied with a game-ID
    	 * @param ID 
    	 */
    	public Game(int gameId) {
    		super();
    		this.ID = gameId;
    		participantsGame = new Vector<>();
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
    	 * adds a participant to a given game
    	 * @param c the client that is to be added
    	 */
    	public void addParticipantToGame(Client c) {
    		participantsGame.add(c);
    		
    	}
    	
    	/**
    	 * removes a participant from a given game
    	 * @param c the client that is to be removed
    	 */
    	public void removeParticipant(Client c) {
    		participantsGame.removeElement(c);
    		
    	}
    	
    	/**
    	 * gets a list of participants in a given game 
    	 * @return a list of clients
    	 */
    	public Vector<Client> getParticipants() {
    		return participantsGame; 
    	}
    	
    	
    	/**
    	 * Throws a dice in Ludo
    	 * @return The dice value
    	 */
    	public int throwDice() {
    		return super.throwDice();
    	}
    	
    	/**
    	 * Tries to move a player piece in Ludo
    	 * @param player The player index
    	 * @param from The tile to move from
    	 * @param to The tile to move to
    	 * @return True if the piece could move
    	 */
    	public boolean movePieve(int player, int from, int to) {
    		return super.movePiece(player, from, to);
    	}
    
    }
}