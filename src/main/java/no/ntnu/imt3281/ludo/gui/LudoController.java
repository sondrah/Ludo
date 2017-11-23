package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.derby.impl.sql.catalog.SYSUSERSRowFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;	
import javafx.stage.Stage;
import javafx.stage.Window;
import no.ntnu.imt3281.i18n.I18N;
import no.ntnu.imt3281.ludo.server.ServerController;

/**
 * Controlls all actions on the homepage
 * Has the masterchat, friendlists and other
 * functionalities like starting new games
 */
public class LudoController {
    
    @FXML private MenuBar menuBar;
    @FXML private Menu file;
    @FXML private MenuItem connect;
    @FXML private MenuItem close;
    @FXML private Menu game;
    @FXML private MenuItem challenge;
    @FXML private MenuItem random;
    @FXML private Menu chat;
    @FXML private MenuItem join;
    @FXML private MenuItem listRooms;
    @FXML private Menu help;
    @FXML private MenuItem about;
    @FXML private TextArea masterChat;
    @FXML private Button say;
    @FXML private Button cancel;
    @FXML private Button send; 
    @FXML private Label errorMessage;
    @FXML private Label userName;
    @FXML private TextField toSay;
    @FXML private TextField chatName;
    @FXML private TextArea chatArea;
    @FXML private TabPane tabbedPane;
    @FXML private TitledPane joinOrChallenge;

    /** Maps chatId -> tab */
    HashMap<Integer, Integer> chatToTab = new HashMap<>();
    /** Maps gameId -> tab */
    HashMap<Integer, Integer> gameToTab = new HashMap<>();
    
    
    /** Which players are waiting for random game*/
	private ArrayList<GameBoardController> gameBoards = new ArrayList<GameBoardController>();
	/** Which players are waiting for random game*/
	private ArrayList<ChatController> chatWindows = new ArrayList<ChatController>();
    private Stage root;
    private int clientId;
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    
    private DefaultListModel<String> participantsModel;
    private ExecutorService executorService;
    private boolean shutdown = false;
    private int userId;
    private int listTab = 0;
    
    // public LudoController() {} // tom constructor for load fxml
    
    /**
     * TODO
     * Sets up connection with socket,
     * thread to listen to incoming messages.
     * @param socket 
     * @param id
     * @param stageroot
     */
    public void setUpController(Socket socket, int userid, Stage stageroot) {
		setRoot(stageroot);
    	setConnection(socket);
    	setUserId(userid);
    	chatToTab.put(1, 0);	// Master Chat ligger der fra start
    	executorService = Executors.newCachedThreadPool();
        processConnection();		// Handle login requests in a separate thread
        executorService.shutdown();
    }
   
    /**
     * Sets up socket connection to server. 
     * @param socket socket to connect with
     */
    public void setConnection(Socket socket) {
    	try {
			this.socket = socket;
			input = new BufferedReader(new InputStreamReader(
			        socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(
			        socket.getOutputStream()));
			
    	} catch(IOException ioe) {
    		System.err.println("fikk ikke connection, i ludocontroller");
    		ioe.printStackTrace();
    		
    	}
    }

    /**
     * Sets username.
     * @param usrN username to be set 
     */
    public void setUserName(String usrN) {
    	userName.setText(usrN);
    }

    /**
     * Set user id in constructor
     * @param id
     */
    public void setUserId(int id) {
    	this.clientId = id;
    }
    
    /**
     * This method handles the communication from the server. Note that this
     * method never returns, messages from the server is read in a loop that
     * never ends. All other user interaction is handled in the GUI thread.
     * 
     * Login and logout messages is used to add/remove users to/from the list of
     * participants while all other messages are displayed.
     * @throws InterruptedException 
     */
    public void processConnection()  {
    	executorService.execute(() -> {
            while (!shutdown) {
		        while (true) {  // Sjekker hele tiden etter innkommende meldinger 
		        	
		            try {
		                if (input.ready()) {					// TimeUnit.MILLISECONDS.sleep(10);
			                
			                String retMessage = input.readLine();	
							String[] returnMessage = retMessage.split(",");
							String type = returnMessage[0];
							int actionId = Integer.parseInt(returnMessage[1]);	
							String receivedClientId = returnMessage[2];	
							String message = returnMessage[3];
							String inviteName = null;
							System.out.println("4. Client listener innkommende melding" + retMessage);
							
							if(returnMessage.length == 5) {
								inviteName = returnMessage[4];
							}
							
			                if (type.equals("CHAT")) { 				// Message er av typen CHAT:
			                	if (message.startsWith("99NEWCHAT") && actionId !=0) {		// Nyopprettet chat, med suksess
			                		addNewTabToChatMapping(actionId); 				// Legg den til i mapping 
			                	}
			                	else if(inviteName != null) {						// informs that client with name 'inviteName' joined chat = 'actionId'
			                		routeChatMessage("Joined chat: "+inviteName, actionId);			
			                	} else {
			                		routeChatMessage(message, actionId);			// message in chat-room
			                	}
			                } 
			                else if (type.equals("GAME")) {			// Message er av typen GAME:
			                	
			                	if (message.startsWith("99NOTENOUGH") && actionId == 0) {		// Nyopprettet forespørsel game, UTEN suksess
			                		// Innkommende melding 
			                		// "GAME,0,"+curClient.getId()+",Ikke nok spillere enda("+waitingClients.size()+") , venter på flere spillere"
			                		// TODO Snorre, innkommende melding er du må vente litt, vises " i en popup??"
			                		Alert alert = new Alert(AlertType.INFORMATION);
			                		alert.setTitle("FYI");
			                		alert.setHeaderText("You've been placed in queue");
			                		alert.setContentText("A new game will begin after three other players have joined the same game");

			                		alert.showAndWait();
			                	}
			                	else if (message.startsWith("99BEGINGAME") && actionId !=0) { // Nyopprettet forespørsel game, MED suksess
			                		makeNewGameTab(actionId);
			                	// Mappe game id til fane
			                	}
			                	// Innkommende ("GAME,"+currentGameID+","+notifyClient+",99 HAR STARTET"+message);
			                	routeGameMessage(actionId, receivedClientId, message );
			                	
			                }
			                else if (type.equals("LISTPLAYERS")) {
			                	 String[] returntemp = retMessage.split(",", 3);
			                	 String[] names = returntemp[2].split(",");
			                	 
			                	 System.err.println(names[0]);
			                	 
			                	 AnchorPane pane = (AnchorPane) tabbedPane.getTabs().get(listTab).getContent();
			                	 ListView<String> list = (ListView<String>) pane.lookup("#listPlayers");
			                	 
			                	 list.setItems(FXCollections.observableArrayList(names));
			                	 
			                } else { // All other messages
			                    System.out.println("Noe feil har skjedd i prosessConnection ");
			                }
		                }
		            } catch (IOException ioe) {
		            	System.err.println("Error receiving data: ");
		            }
		        }		// While true end
            }
    	});
    }
    
   

	

	/**
     * Used to add messages to the message area in a thread safe manner
     * 
     * @param text the text to be added
     */
    private void routeChatMessage(String message, int chatId) {
    	Integer tabId = chatToTab.get(chatId);
   	
    	if(tabId != null) {	
    		
    				// Henter ut riktig Anchor Pane for riktig chatterom
	    	AnchorPane tabRoot = (AnchorPane) tabbedPane.getTabs().get(tabId).getContent();
	    				// Finner alle elementene i dette chattevinduet 
	    	if (tabRoot != null) {
		    	TextArea textA = (TextArea)tabRoot.lookup("#chatArea");
		    	
		    	System.out.println("5. CHAT routeChatMes : tabID: "+tabId+ " melding " +message);
		    	textA.appendText(message+ "\n");		// Legg til meldingen 
		    }
	    	else System.out.println("5. CHAT routeChatMes ERR FANT IKKE TAB ROOT!!");
	    		
    	}
    } 
    
    /**
     * routeGameMessage
     * @param actionId
     * @param receivedClientId
     * @param message
     */
    private void routeGameMessage(int gameId, String receivedClientId, String message) {
    	
    	Integer tabId = gameToTab.get(gameId);
    	System.out.println("6. GAME routeGame M: " +message+ " Tab id til Game: " + tabId );
    	if(tabId != null) {	 
    				// Henter ut riktig Anchor Pane for riktig chatterom
	    	AnchorPane tabRoot = (AnchorPane) tabbedPane.getTabs().get(tabId).getContent();
	    	if (tabRoot != null) {
	    			// Finner alle elementene i dette chattevinduet 
	    		TextArea textA = (TextArea)tabRoot.lookup("#ChatArea");  // "#gameChatArea" eller likt??
	    	
	    	// Her må vi antagelig ha flere mtp chat og board
	    	
	    		textA.appendText(message+ "\n");		// Legg til meldingen 
	    	}
	    	else System.out.println("5. CHAT routeGameMes ERR FANT IKKE TAB ROOT!!");
    	}
		
	}
    /** 
     * Closes the application
     * @param e button click caused by the close menu item
     */
    @FXML
    public void close(ActionEvent e) {
    	Platform.exit();
    }
    
    
    
    /**
     * Loads a new tab containing a Vbox with a list of current connected players, a textfield to input one of these,
     * and a button to confirm player to challenge.
     * @param e button click caused by the challenge player menu item
     */
    @FXML
    public void challengePlayer(ActionEvent e) {
    	
    	Stage dialog = new Stage();
        dialog.initOwner(root);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("JoinOrChallenge.fxml"));
    	loader.setResources(I18N.getRsb());
        
    	try {
    		VBox vbox = loader.load();
    		String rq = "LISTPLAYERS," + clientId + ", , ";
    		output.write(rq); 
    		output.newLine();
    		output.flush();
    		
    		String d = (I18N.tr("ludo.challengePlayer"));
    		JoinOrChallengeController contr = loader.getController();
    		contr.setDesc(d);
    		
    		Tab tab = new Tab("Player list");
    		tab.setContent(vbox);
    		tabbedPane.getTabs().add(tab);
    		
    		listTab = tabbedPane.getTabs().size() - 1;
    	}
    	catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    }
    
    /**
     * Opens a dialogue box which asks the user what they want to name their chat.
     * @param e button click caused by the create room menu item
     */
    @FXML
    public void createChat(ActionEvent e) {
    	
    	TextInputDialog dialog = new TextInputDialog("");
    	dialog.setTitle("New chat");
    	dialog.setHeaderText("");
    	dialog.setContentText("Enter the name of your new chat:");

    	// Traditional way to get the response value.
    	Optional<String> result = dialog.showAndWait();
    	if (result.isPresent()){
    	    System.out.println("Chat name: " + result.get());
    	}
    	
    	
    	/*System.err.println("inne i createChat");
    	
    	Stage primaryStage = new Stage();
    	start(primaryStage);
    	System.err.println("cool?");
		
    	
    	/*try {							
			System.out.println("1. Client: createChat på client: ");
			output.write("CHAT,CREATE,"+ clientId +",99 NEW CHAT" );
			output.newLine();
			output.flush();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
    	
    }
    
    /**
     * creates a new tab including a Vbox with a list of all available chats, a textfield to input
     * one of these, and a button to confirm this choice.
     * @param e button click caused by the join room menu item
     */
    @FXML
    public void joinChat(ActionEvent e) {
    	Stage dialog = new Stage();
        dialog.initOwner(root);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("JoinOrChallenge.fxml"));
    	loader.setResources(I18N.getRsb());
        
    	try {
    		VBox vbox = loader.load();
    		
    		String d = (I18N.tr("ludo.joinChat"));
    		JoinOrChallengeController contr = loader.getController();
    		contr.setDesc(d);
    		
    		Scene dialogScene = new Scene(vbox, 410, 215);
    		
    		dialog.setScene(dialogScene);
    		dialog.show();
    	}
    	catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    }
    
    /**
     * addNewTabToChatMapping
     * @param chatId
     */
    public void addNewTabToChatMapping( int chatId) {					// TODO chatboard.fxml
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatBoard.fxml"));
    	loader.setResources(I18N.getRsb());

    	try {
    		AnchorPane chatBoard = loader.load();
        	Tab tab = new Tab("Chat" + chatId);
    		tab.setContent(chatBoard);
        	tabbedPane.getTabs().add(tab);
    	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	ObservableList<Tab> tabs = tabbedPane.getTabs();	// list of all open tabs
    	
    	chatToTab.put(chatId, tabs.size()-1);						// adds chatId to mapping

    }
    
    /**
     * Displays a nice popup with an important question
     * @param e button click caused by the about menu item
     */
    @FXML
    public void about(ActionEvent e) {
    	JOptionPane.showConfirmDialog(null, "Got milk?");
    }
    
    /**
     * Writes a clients message to the masterchat-room in Home
     * @param e button click caused by the say button in ludo
     */
    @FXML		// OBS brukes bare for masterchat i Ludo Home
    public void saySomething(ActionEvent e) {
    	
    	String txt = toSay.getText();
    	if(!txt.equals("") && txt !=null) {
    		try {								
    			
    			
    			System.out.println("1. Client: SaySomething fra/ på client: "+txt);
    			output.write("CHAT,1,"+ clientId +"," +txt);
				output.newLine();
				output.flush();

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    }
    
    /**
     * Writes a clients message to the masterchat-room in Home
     * @param e enter click caused by the say button in ludo
     */
    @FXML
    public void saySomethingKey(KeyEvent e) {
    	if(e.getCode() == KeyCode.ENTER)
    		saySomething(new ActionEvent());
    }
    
    /**
     * Handles the action of one player that tries to enter a
     * new random game with said button
     * @param e the buttonclick action
     */
    @FXML
    public void joinRandomGame(ActionEvent e) {  
    	Parent root;
    	// TODO, hvilken tab id kommer dette fra 
		try {								// Client sier jeg vil spille 
			System.out.println("1. Client Trykket på knapp rand game, skal sende nå");
			output.write("GAME,0,"+ clientId +",Random Game trykket på");
			output.newLine();
			output.flush();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
     }
    
    /**
     * makeNewGameTab
     * Use controller to set up communication for this game.
	 * Note, a new game tab would be created due to some communication from the server
	 * This is how a layout is loaded and added to a tab pane.
     * @param gameId
     */
    public void makeNewGameTab(int gameId) {

		// Får inn i controlleren input.readLine()
    	FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
		gameLoader.setResources(I18N.getRsb());
    	GameBoardController gameController = gameLoader.getController();
    	// Unødvendig gameLoader.setController(gameController);
		// Use controller to set up communication for this game.
		// Note, a new game tab would be created due to some communication from the server
		// This is how a layout is loaded and added to a tab pane.

		try {
			AnchorPane gameBoard = gameLoader.load();
        	Tab tab = new Tab("Game" + gameId);
    		tab.setContent(gameBoard);
        	tabbedPane.getTabs().add(tab);
        	gameBoards.add(gameController);
    	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	ObservableList<Tab> tabs = tabbedPane.getTabs();	// list of all open tabs
    	gameToTab.put(gameId, tabs.size()-1);				// adds gameId to maping
		
    	
    	// TODO lage egen funk mtp "user generatet new game også
	
    }
    
    /**
     * Sends a message to Server that this client
     * wants to create a private chat.
     * @param chatName name of the new chat
     */
    public void newPrivateChat(String chatName) {
		try {								
			output.write("CHAT,0,"+ clientId +"," +chatName);
			output.newLine();
			output.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
    
    /**
    * makeNewChatTab
    * Use controller to set up communication for this chat.
	* Note, a new chat tab would be created due to some communication from the server
	* This is how a layout is loaded and added to a tab pane.
    * @param chat id
    */
   public void makeNewChatTab(int chatId) {

		// Får inn i controlleren input.readLine()
    	FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("PrivateChat.fxml"));
		chatLoader.setResources(I18N.getRsb());
		ChatController chatController = chatLoader.getController();

		try {
			AnchorPane chatWindow = chatLoader.load();
	       	Tab tab = new Tab("Chat" + chatId);
	   		tab.setContent(chatWindow);
	       	tabbedPane.getTabs().add(tab);
	       	chatWindows.add(chatController);
	       	
	   	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   	
		ObservableList<Tab> tabs = tabbedPane.getTabs();	// list of all open tabs
   		chatToTab.put(chatId, tabs.size()-1);				// adds gameId to maping
	
   }
	
    public void setRoot(Stage stage) {
    	this.root = stage;
    }
    
    
    
    
    
    
}
