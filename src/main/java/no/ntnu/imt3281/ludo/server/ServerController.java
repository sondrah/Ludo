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
	/** Unique ID for each game, zeroed for each server start */
	private int gameID = 1;
	/** Witch players are waiting for randoom game*/
	private ArrayList<Client> waitingClients = new ArrayList<Client>();
	// TODO Bjonn , også bruke exception waitingClients = new ArrayList<Client>(4);
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
                    	
                    	
                    	String[] parts = msg.split(",");   
                    	String type = parts[0];
	                    String operation = parts[1];
	                    String userName = parts[2];
	                    String pwd = parts[3];
	                    System.out.println("0. Inne i login listener for user: "+ userName);
                    	
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
		                			chats.get(0).addParticipantToChat(newClient);	// Legger Klient til i masterChat 
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
				                        	
				                        	// Melding: CHAT,1,reciverID,'Username' har blitt med i Masterchat
				                        	messages.put("CHAT,1," + c1.getId() + ","
				                        				+ userName + " har blitt med i MasterChat");	
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
	                    // TODO lag egne funksjoner for hver type
	                    // TODO del opp mottatt string inne i funksjonen
	                    
	                    while (i.hasNext()) {			
	                        Client curClient = i.next();			// ??SA - hopper over første?
	                        try {
	                        	String msg = curClient.read();		// Leser inn meldingen 
	                        	if (msg != null) {
		                        	System.out.println("2. I Server Listner: "+ msg);
	                        		String[] parts = msg.split(",");   	// Splitter den opp på , komma
	                        		
		                        	// Henter ut aktuelle variabler
		                        	int fromClientID = curClient.getId();
		                        	
		                        	String type = parts[0];
		    	                    int actionId = Integer.parseInt(parts[1]);		//IDnr til rom 
		    	                    String info = parts[2];
		    	                    String message = parts[3];
		    	                    
		    	                    String userName = db.getUserName(fromClientID);
		    	                    int inviteId = -1;
		    	                    
		    	                    if(parts.length == 5) {		// TODO Sondre, endre til sjekk innhold if chat invite, extract inveted clients Id
		    	                    	inviteId = Integer.parseInt(parts[4]);		
		    	                    }
		    	                    											// 	eks CHAT,3,0,msg   type idRom/game, info??trengs?, melding
		    	                	if (type.equals("CHAT")) {					// Hvis meldingen er av typen CHAT
		    	                		    	                		
		    	                		if(actionId == 0) {							// New Chat
		    	                			Chat newChat = newChat(message);
		    	                			int newChatId = newChat.getId();
		    	                			newChat.addParticipantToChat(curClient);		// addParticipant - seg selv ved oprettelse av ny chat
		    	                			
		    	                			// TODO: fikse melding
		    	                			// CHAT,chatID,reciving clientID, 0Chat opprettet
		    	                			messages.put("CHAT," + newChatId + "," + fromClientID + ",0" + message);	
		    	                		} else {											// Chatte rom eksisterer allerede
		    	                			
		    	                			Iterator<Chat> chatNri = chats.iterator();		// Iterer gjennom alle chatte rom
		    	                			while (chatNri.hasNext()) {						// hvis flere
						                        Chat curChat = chatNri.next();				// Hvilken sjekkes nå
						                        
						                        if (actionId == curChat.getId()) {   		// Dersom riktig chatterom  
						                        	Client addClientinRoom = null;
						                        	
						                        	// if it's a invite to a chat-room
						                        	if(parts.length == 5) {							// TODO Sondre endre til sjekk innhold						
						                        		Iterator<Client> k = clients.iterator();	// looks for client ID with inviteId
						                        		addClientinRoom = k.next();
						                        		while(addClientinRoom.getId() != inviteId) {		// idNr = chatId, info = clientId, message = "", invite = invited clientId
						                        			addClientinRoom = k.next();								// ********"CHAT,idNr,info,message,invite"***********						
						                        		}	// While stop if client username is found
						                        		
						                        		curChat.addParticipantToChat(addClientinRoom);			// adds inveteId client to char-room
						                        	} 	
						                        		// Iterere gjennom alle aktuelle klienter i riktig chat
						                        	Iterator<Client> clientNri = curChat.participantsChat.iterator();
						                        	while (clientNri.hasNext()) {						// For hver client i aktuelt chatte rom
								                        Client curCli = clientNri.next();
								                        int notifyClient = curCli.getId();
								                        
								                        if(curCli.getId() == inviteId) {					// if client was added to chat-room 
								                        	// CHAT,chatID,fromID,void,mottakerid????
								                        	// FIXME
								                        	messages.put("CHAT,"+ curChat.getId() +","+ fromClientID +",void,"+ db.getUserName(curCli.getId()));
								                        }
								                        
								                        // CHAT,chatid,user in given chat,'username' > 'message'
								                        messages.put("CHAT," + curChat.getId() + "," + notifyClient + "," + userName + " > " + message);	
						                        	}
						                        }
		    	                			}	// While chat slutt, sjekket alle
		    	                		}
			                        }
		    	                	else if (type.equals("GAME")) {
		    	                		
			                        	if(actionId == 0) {							// New Game
			                        		playersWaitingForRandomGame(curClient);
			                        		if (waitingClients.size() <= 1) {	// Legger en til i ventelista, dersom da flere enn 1 start
			                        			// TODO wait for more playser 5 sek 
			                        			// If not, start anyway
			                        			System.out.println("Ikke nok spillere enda(" + waitingClients.size() + ") , venter på flere spillere");
			                        			
			                        			// GAME,0,clientID,WAIT
			                        			messages.put("GAME,0,"+curClient.getId()+",Ikke nok spillere enda("+waitingClients.size()+") , venter på flere spillere");
			                        			
			                        		} else {			// New game and enough playser
			                        			System.out.println("Nok spillere ("+waitingClients.size()+") , oppretter new game");
			                        			int currentGameID  = gameID++; 			// Tildeler game id 
				    	                		Chat newChat = newChat("ChatForGame"+currentGameID);
				    	                		int newChatId = newChat.getId();
				    	                		Game newGame = new Game(currentGameID, newChatId);		// Oppretter ny chat i server
				    	                		games.add(newGame);						// Legger denne til i serverens chat liste 
				    	                		
				    	                		
				    	                		System.out.println("Game id:" +currentGameID+ "New Chat id "+ newChatId);
				    	                		System.out.println("ant Games: " +games.size()+ "  ant Chats: "+ chats.size());
				    	                		
				    	                		Iterator<Client> newP = waitingClients.iterator();	// Iterate throug all clients
				    		                    while (newP.hasNext()) {
				    		                    	System.out.println("Chat: Inne i while"+ waitingClients.size());
				    		                        Client newPlayer = newP.next();
				    		                        if (newPlayer != null) {
					    		                        System.out.println("Chat: NewPlayer sin id: "+ newPlayer.getId());
					    		                        
					    		                        newGame.addParticipantToGame(newPlayer);
					    		                        newChat.addParticipantToChat(newPlayer);
					    		                        int notifyClient = newPlayer.getId();
					    		                        // TODO guro, newGame er den gyllene port til ludo.
					    		                        // Melding som skal plukkes opp og fysisk starte board i gui
					    		                        
					    		                        // CHAT,gameID,mottaker,TRUE,message
					    		                        messages.put("GAME,"+currentGameID+","+notifyClient+",99 HAR STARTET"+message);
					    		                        
					    		                        // TODO SONDRE, her er nytt chatt til game vindu - må testes 
						    	                		messages.put("CHAT,"+newChatId+","+notifyClient+", Game id:"+currentGameID+" > " +message);
						    	                		
						    	                		// TODO linje (til master chat" under fjernes når nytt chatterom er oppe å går
						    	                		messages.put("CHAT,1,"+notifyClient+","+userName+" > Game id: "+currentGameID+" opprettet via > " +message);
						    	                								// Sender tilbake riktig chat nr til client som oprettet den
				    		                        }
				    		                    }
				    		                    // TODO nullstille waiting clients
				    	                	}
				                        }
			                        	
			                        	// HANDLE actual game mechanics
				                        else if(actionId !=0 ){
			    	                		Iterator<Game> gameNri = games.iterator();
			                        		boolean foundGame = false;
			                        		// lag funksjon som finner riktig game!! 
			    							while(gameNri.hasNext() && !foundGame ) {
			                        			Game tempGame = gameNri.next();
			                        			if (actionId == tempGame.getId())
			                        				foundGame = true;				// Fant client, trenger ikke lete mer
			                        			System.out.println("Oioi, GAME melding som ikke har funksjon enda");
			                        			// TODO Guro tempGame.doSomething()
			    							}
				                        }
			                        }
	    	                		else if (type.equals("LISTPLAYERS")) {
		                        		StringBuilder sp = new StringBuilder();
		                        		for(Client c: clients) {
		                        			sp.append(db.getUserName(c.getId())+",");
		                        		}
		                        		
		                        		messages.put("LISTPLAYERS,0,"+actionId+","+sp.toString());
			                        } else {			// Allerede eksisterende chat 
		    	                			//Iterator<Chat> chatNri = chats.iterator();		// Iterer gjennom alle chatte rom
		    	                			// TODO lag funk som gjør ALT med game
		    	                		} 
			                        
			                        
	                        	} 	// If msg excits end
	    	     
	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
	                        	// clientNri.remove();
	                        }
	                    }		// while slutt
                	} 	// sync ferdig
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
                    System.out.println("3. Inne i Messagesender med strengen: "+txt);
                    String[] parts = txt.split(",");
                    String type = parts[0];
                    String id = parts[1];			// LOGIN = 0 ( reg), 1 (login) 
                    								// CHAT = aktuelt chat rom
                    								// GAME = idGame
                    int actionId = Integer.parseInt(parts[2]);	// cast to integer ID client   
                    String message = parts[3];
                    synchronized (clients) {		// Only one thread at a time might use the clients object
                    	Iterator<Client> i = clients.iterator();
                		boolean foundClient = false;
                		// lag funksjon som finner riktig client!! 
                		
						while(i.hasNext() && !foundClient ) {
                			Client tempCli = i.next();
                			if (actionId == tempCli.getId()) {
                				foundClient = true;				// Fant client, trenger ikke lete mer
                				if(type.equals("LOGIN") || type.equals("CHAT")) {	
		                    	try {
		                    				 tempCli.sendText(type+","+id+","+actionId+","+message);
		     	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
		     	                        	// i.remove();
		     	                        	// messages.add("LOGOUT:"+c.name);
		     	                        }
	                    		}
                				// TODO Guro, en og samme Messgae sender??
		                    	else if(type.equals("GAME")) {
	                    			 try {
		                    				 tempCli.sendText(type+","+id+","+actionId+","+message);
		     	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
		     	                        	// i.remove();
		     	                        	// messages.add("LOGOUT:"+c.name);
		     	                        }
	                    		}
                			}
                    	}
                    	/*
                    	else if(type.equals("LISTPLAYERS")) {
                    		Iterator<Client> i = clients.iterator();
                    		boolean foundClient = false;
                    		// lag funksjon som finner riktig client!! 
							while(i.hasNext() && !foundClient ) {
                    			Client tempCli = i.next();
                    			if (actionId == tempCli.getId())
                    				foundClient = true;				// Fant client, trenger ikke lete mer
	                    			 try {
	                    				 tempCli.sendText(txt);
	     	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
	     	                        	// i.remove();
	     	                        	// messages.add("LOGOUT:"+c.name);
	     	                        }
                    		}
                    	}
		*/
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
	private Chat newChat(String chatName) {
		Chat newChat = null;
		db.addChat(chatName);					// Legger chatten til i db
		int newChatId = db.getChatID(chatName); // Henter ut Chat id
		if (newChatId != -1) {					// Hvis chat fantes i db
			newChat = new Chat(newChatId);		// Oppretter ny chat i server
			chats.add(newChat);					// Legger denne til i serverens chat liste
		}
		else newChat = null;
		return newChat;
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
			// TODO: handle exception start new game!
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
		
		
		String action = str[1];
		
		try {
			if(action.equals("SAY")) {
				StringBuilder sb = new StringBuilder();
				for(String s : str) {
					sb.append(s + ",");
				}
				
				// since 'SAY's don't need special treament
				// send it straight to 
				messages.put(sb.toString());
			}
		}
		catch (InterruptedException ie) {
			// TODO: log
		}
	}
	
	
	private void handleGameMessage(String[] str) {
		
		// All string arrays that come here have GAME in str[0]
		// possible recieved messages
		// GAME,THROW,gameid,userid					-- Ask server to make a throw
		// GAME,MOVE,gameid,userid,player,from,to	-- 	" 	move a piece
		// GAME,CREATE,userid						-- 	" 	create new game
		
		String action = str[1];
		Game game = null
		
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
					// TODO log
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
				// TODO log
			}
			break;
			
		case "CREATE":
			// sjekk om nok spilere
			// hente 2 - 4 spillere
			// starte et spill med disse
			// games.add(new Game(gameid, players))
			// chatid = db.newChat(chatname)
			// chat = new Chat(cahtid)
			// chats.add(chat)
			
			// if nok spillere
			// messages.put(GAME,CREATE,TRUE,gameid,players)
			// else
			// messages.put(GAME,CREATE,WAIT)
		}
	}
	
	
	private void sendGameMessage(String str) {
		// GAME,THROW,gameid,dice
		// GAME,MOVE,gameid,TRUE,player,from,to
		// GAME,MOVE,gameid,FALSE
		// GAME,CREATE,TRUE,gameid,players
		// GAME,CREATE,WAIT
		
		String[] arr = str.split(",");
		String action = arr[1];
		
		switch(action) {
		case "THROW":
			getGame(Integer.parseInt(arr[2])).getParticipants()
			.parallelStream().forEach(client -> {
				try {
					client.sendText(str);
				} catch (IOException e) {
					// TODO log
				}
			});
			break;
			
		case "MOVE":
			getGame(Integer.parseInt(arr[2]))
			.getParticipants().parallelStream().forEach(client -> {
				try {
					client.sendText(str);
				} catch (IOException e) {
					// TODO log
				}
			});
			break;
			
		case "CREATE":
			getGame(Integer.parseInt(arr[3])).getParticipants()
			.parallelStream().forEach(client -> {
				try {
					client.sendText(str);
				} catch (IOException e) {
					// TODO log
				}
			});
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
     * 
     *
     */
    class Chat {
    	// private String chatName;
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
    	private Vector<Client> participantsGame;
    	
    	/**
    	 * Construct a game of Ludo supplied with a game-ID
    	 * @param ID 
    	 */
    	public Game(int gameId, int chatID ) {
    		super();
    		this.ID = gameId; 	
    		this.relatedChatId = chatID;
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
    	
    	public int getThisGamerelatedChatId() {
    		return relatedChatId; 	
    	}
    	
    	public void addParticipantToGame(Client c) {
    		participantsGame.add(c);
    		
    	}
    	
    	public void removeParticipant(Client c) {
    		participantsGame.removeElement(c);
    		
    	}
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
    	 * Thries to move a playerpice in Ludo
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