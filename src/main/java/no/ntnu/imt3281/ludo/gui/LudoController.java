package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Optional;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.stage.Stage;
import no.ntnu.imt3281.i18n.I18N;
import no.ntnu.imt3281.ludo.Logging;

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
		                if (input.ready()) {				
		                	String response = input.readLine();
		                	
		                	System.out.println("4. client prosess response fra server: " + response);
		                	
		                	String[] arr = response.split(",");
		                	int chatid = 0;
		                	
		                	switch(arr[0]) {
		                	case "CHAT":
		                		// CHAT,SAY,chatid,clientid,message
		                		// CHAT,CREATE,chatid,message
		                		chatid = Integer.parseInt(arr[2]);
		                		
		                		if(arr[1].equals("CREATE")) {
		                			makeNewChatTab(arr[3], chatid);
		                		} else {
		                			routeChatMessage(arr[4], chatid);
		                		}
		                		break;
		                		
		                	case "GAME":
		                		// GAME,CREATE,TRUE,uid,cid,players[]
		                		int gameid = 0; 
		                		GameBoardController gameBoard = null;
		                		
		                		if(arr[1].equals("CREATE")) {
		                			gameid = Integer.parseInt(arr[3]);
		                			
		                			chatid = Integer.parseInt(arr[4]) ;
		                			
		                			if(arr[2].equals("TRUE")) {
			                			// arr[5].split(",") should return an array with
			                			// the players in this game
			                			makeNewGameTab(gameid, chatid, arr[5].split(":"));
			                		}
			                		else {
			                			// kjøre på FX-thread
			                			Platform.runLater(() ->{
			                				Alert alert = new Alert(AlertType.INFORMATION);
				                			alert.setTitle(I18N.tr("ludo.fyiHeader"));
				                			alert.setHeaderText(null);
				                			alert.setContentText(I18N.tr("ludo.fyiContent"));
				                			alert.showAndWait();
			                			});
			                		}
		                		}
		                		else if(arr[1].equals("THROW")) {
			                		// GAME,THROW,gid,cid
		                			gameid = Integer.parseInt(arr[2]);
		                			chatid = Integer.parseInt(arr[3]);
		                			
		                			
		                			gameBoard = getGameBoard(gameid);
		                			gameBoard.throwDice(Integer.parseInt(arr[3]));
		                		}
		                		else if(arr[1].equals("MOVE")) {
		                			gameBoard = getGameBoard(gameid);
		                			
		                			if(arr[3].equals("TRUE")) {
		                				// GAME,MOVE,TRUE,gameid,player,from,to
		                				int player = Integer.parseInt(arr[4]);
		                				int from = Integer.parseInt(arr[5]);
		                				int to = Integer.parseInt(arr[6]);
		                				
		                				gameBoard.movePiece(player, from, to);
		                			}
		                		}
		                		break;
		                		
		                	case "LISTPLAYERS":
		                		// melding:
		                		// LISTPLAYERS,clientid,players[]
		                		
		                		// finne tab
		                		// inne i den tabben,
		                		// lookup(#listView)
		                		// listView.setItems(array)
		                		break;
		                		
		                	case "LISTCHATS":
		                		// melding:
		                		// LISTCHATS,clientid,players[]
		                		
		                		// finne tab
		                		// inne i den tabben,
		                		// lookup(#listView)
		                		// listView.setItems(array)
		                		break;

		                	default : // logger	
		                		break;
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
    		//if( chatId == 1) {
    		AnchorPane tabRoot = (AnchorPane) tabbedPane.getTabs().get(tabId).getContent();
    		
	    				// Finner alle elementene i dette chattevinduet 
	    	if (tabRoot != null) {
		    	TextArea textA = (TextArea)tabRoot.lookup("#chatArea");
		    	if (textA != null) {
		    		System.out.println("5. CHAT routeChatMes : tabID: "+tabId+ " melding " +message);
		    		textA.appendText(message+ "\n");		// Legg til meldingen 
		    	}
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
    	
    	// gameBoards. vinne riktig gameBoaard
    	// Så finne riktig tab
    	Integer tabId = gameToTab.get(gameId);
    	System.out.println("6.routeGameM: " +message+ " Tab id til Game: " + tabId );
    	if(tabId != null) {	 
    										// Henter ut riktig Anchor Pane for riktig chatterom
	    	AnchorPane tabRoot = (AnchorPane) tabbedPane.getTabs().get(tabId).getContent();
	    	if (tabRoot != null) {
	    									// Finner alle elementene i dette chattevinduet 
	    		TextArea textA = (TextArea)tabRoot.lookup("#ChatArea");  // "#gameChatArea" eller likt??
	    		if(textA != null) {    		// Her må vi antagelig ha flere mtp chat og board
	    			textA.appendText(message+ "\n");		// Legg til meldingen 
	    		}
	    		else System.out.println("5. CHAT routeGameMes ERR FANT IKKE textArea!!");
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
    	dialog.setTitle(I18N.tr("ludo.newChat"));
    	dialog.setHeaderText("");
    	dialog.setContentText(I18N.tr("ludo.newChatContent"));

    	// Traditional way to get the response value.
    	Optional<String> result = dialog.showAndWait();
    	if (result.isPresent()){
    	    System.out.println("0,ChatChat  name: " + result.get());
    	    try {
	    	    String txt = "CHAT,CREATE," + result.get() + ","+clientId;
	    		output.write(txt); 
	    		output.newLine();
	    		output.flush();
    	    } catch(IOException ioe) {
    	    	ioe.printStackTrace();
    	    }
    	}
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
     * Displays a nice popup window with a depressing message
     * @param e button click caused by the about menu item
     */
    @FXML
    public void about(ActionEvent e) {
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle(I18N.tr("ludo.help.Title"));
    	alert.setHeaderText(I18N.tr("ludo.help.Header"));
    	alert.setContentText(I18N.tr("ludo.help.Content"));

    	alert.showAndWait();
    }
    
    /**
     * Writes a clients message to the masterchat-room in Home
     * @param e button click caused by the say button in ludo
     */
    @FXML		// OBS brukes bare for masterchat i Ludo Home
    public void sendText(ActionEvent e) {
    	
    	String txt = toSay.getText();
    	if(!txt.equals("")) {
    		try {								
    			
    		
    			output.write("CHAT,SAY,1," + clientId +"," +txt);
				output.newLine();
				output.flush();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
    	}
    }
    
    /**
     * Writes a clients message to the masterchat-room in Home
     * @param e enter click caused by the say button in ludo
     */
    @FXML
    public void sendTextKey(KeyEvent e) {
    	if(e.getCode() == KeyCode.ENTER)
    		sendText(new ActionEvent());
    }
    
    /**
     * Handles the action of one player that tries to enter a
     * new random game with said button
     * @param e the buttonclick action
     */
    @FXML
    public void joinRandomGame(ActionEvent e) {  
		try {								// Client sier jeg vil spille 
			
			output.write("GAME,CREATE,"+ clientId);
			output.newLine();
			output.flush();

		} catch (IOException e1) {
			// skulle funka..  exceptionlogger info(e1) 
		}
		
     }
    
    /**
     * makeNewGameTab
     * Use controller to set up communication for this game.
	 * Note, a new game tab would be created due to some communication from the server
	 * This is how a layout is loaded and added to a tab pane.
     * @param gameId
     */
    public void makeNewGameTab(int gameId, int chatId, String[] players ) {
    	
    	FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
		gameLoader.setResources(I18N.getRsb());
    	
		try {
			AnchorPane gameBoard = gameLoader.load();
			GameBoardController gameController = gameLoader.getController();
	    	if (gameController != null) {
		    	gameController.StartGameBoard(gameId, chatId, clientId, players, socket);
		    	gameController.setUserName(userName.getText());
		    } else System.out.println("7. Make Game tab, fant ikke game controller! gameloader: "+ gameLoader);

        	Tab tab = new Tab("Game" + gameId);
    		tab.setContent(gameBoard);
    		
    		Platform.runLater(() ->{
    			tabbedPane.getTabs().add(tab);
    			tabbedPane.getSelectionModel().select(tab);
    		});
    		
        	gameBoards.add(gameController);
    	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	ObservableList<Tab> tabs = tabbedPane.getTabs();	// list of all open tabs
    	gameToTab.put(gameId, tabs.size());				// adds gameId to maping
    	chatToTab.put(chatId, tabs.size());				// adds gameId to maping
   		System.out.println("makeNewChatTab ChatID: "+chatId+ " Får tab: " + (tabs.size()));

    }
    
    
    /**
    * makeNewChatTab
    * Use controller to set up communication for this chat.
	* Note, a new chat tab would be created due to some communication from the server
	* This is how a layout is loaded and added to a tab pane.
    * @param chat id
    */
   public void makeNewChatTab(String chatName, int chatId) {

    	FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("PrivateChat.fxml"));
		chatLoader.setResources(I18N.getRsb());
		
		try {
			AnchorPane chatWindow = chatLoader.load();
			ChatController chatController = chatLoader.getController();
			chatController.setChatId(chatId, clientId);
			chatController.setConnection(socket);  // TODO sjekk Bjønn ok?? 
	       	Tab tab = new Tab("Chat: " + chatName);
	   		tab.setContent(chatWindow);
	   		
	   		Platform.runLater(() -> {
	   			tabbedPane.getTabs().add(tab);
    			tabbedPane.getSelectionModel().select(tab);
	   		});
	   		
	       	chatWindows.add(chatController);
	       	
	   	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   	
		ObservableList<Tab> tabs = tabbedPane.getTabs();	// list of all open tabs
   		chatToTab.put(chatId, tabs.size());				// adds gameId to maping
   		System.out.println("makeNewChatTab ChatID: "+chatId+ " Får tab: " + (tabs.size()));
	
   }
	
    public void setRoot(Stage stage) {
    	this.root = stage;
    }
    
    
    private GameBoardController getGameBoard(int gameid) {
    	GameBoardController board = null;
    	
    	for(GameBoardController gameBoard : gameBoards) {
    		if(gameBoard.getGameId() == gameid) {
    			board = gameBoard;
    		}
    	}
    	
    	return board;
    }
    
    
    
}
