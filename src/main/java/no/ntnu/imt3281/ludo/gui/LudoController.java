package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import no.ntnu.imt3281.i18n.I18N;

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
    @FXML private TextArea chatArea;
    @FXML private TabPane tabbedPane;
    @FXML private TitledPane joinOrChallenge;

    /** Maps chatId to tab */
    HashMap<Integer, Integer> map = new HashMap<>();
    private Stage root;
    private int gameId = 0;
    private int clientId;
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    
    private DefaultListModel<String> participantsModel;
    private ExecutorService executorService;
    private boolean shutdown = false;
    private int userId;
    
    // public LudoController() {} // tom constructor for load fxml
    
    /**
     * TODO
     * Sets up connection with socket,
     * thread to listen to incoming messages.
     * @param socket 
     * @param id
     * @param stageroot
     */
    public void setUpController(Socket socket, int id, Stage stageroot) {
		setRoot(stageroot);
    	setConnection(socket);
    	setUserId(id);
    	map.put(1, 0);
    	//addNewTabToChatMapping(1); // Legger til MasterChat (id 1)
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

    @FXML
    public void connect(ActionEvent e) {
    	// TODO:
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
							
			                if (type.equals("CHAT")) { 				// Message er av typen CHAT
			                	if (message.startsWith("0")) {
			                		addNewTabToChatMapping(actionId); 
			                	}
			                	routeChatMessage(message, actionId);
			                	
			                // } else if (type.equals("LOGOUT")) { // User is logging out removeUser(tmp.substring(7));
			                } else { // All other messages
			                    
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
    	Integer tabId = map.get(chatId);
   	
    	if(tabId != null) {	// korrekt check?
    		
    				// Henter ut riktig Anchor Pane for riktig chatterom
	    	AnchorPane tabRoot = (AnchorPane) tabbedPane.getTabs().get(tabId).getContent();
	    				// Finner alle elementene i dette chattevinduet 
	    	TextArea textA = (TextArea)tabRoot.lookup("#chatArea");
	    	
	    	System.out.println("Say in route Chat M: " +message);
	    	textA.appendText(message+ "\n");		// Legg til meldingen 
	    	/*
	    	Iterator<Node> it = tabRoot.getChildren().iterator();
	    				// Itererer gjennom elementene 
	    	while(it.hasNext()) {
	    		Node n = it.next();
	    		String nodeID = n.getId();
	    											// Dersom elementet er chatArea 
	    		if(nodeID.equals("chatArea")) {
	    			TextArea text = (TextArea) n;	// Hent ut dette tekstområdet
	    			text.appendText(message);		// Legg til meldingen 
	    		}
	    	}
	    	*/
		   // mulig løsning til overSwingUtilities.invokeLater(() -> text.append(message));
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
    
    
    
    @FXML
    public void challengePlayer(ActionEvent e) {
    	// TODO: vis liste av alle active spillere
    	// TODO: kunne velge disse
    	// TODO: sende request
    	// TODO: motta svar?
    	
    	Stage dialog = new Stage();
        dialog.initOwner(root);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("JoinOrChallenge.fxml"));
    	loader.setResources(I18N.getRsb());
        
    	try {
    		AnchorPane pane = loader.load();
    		
    		String d = (I18N.tr("ludo.challengePlayer"));
    		JoinOrChallengeController contr = loader.getController();
    		contr.setDesc(d);
    		
    		Scene dialogScene = new Scene(pane, 410, 420);
    		
    		dialog.setScene(dialogScene);
    		dialog.show();
    	}
    	catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    }
    
    
    @FXML
    public void joinChat(ActionEvent e) {
    	Stage dialog = new Stage();
        dialog.initOwner(root);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("JoinOrChallenge.fxml"));
    	loader.setResources(I18N.getRsb());
        
    	try {
    		AnchorPane pane = loader.load();
    		
    		String d = (I18N.tr("ludo.joinChat"));
    		JoinOrChallengeController contr = loader.getController();
    		contr.setDesc(d);
    		
    		Scene dialogScene = new Scene(pane, 410, 215);
    		
    		dialog.setScene(dialogScene);
    		dialog.show();
    	}
    	catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    }
    
    
    public void addNewTabToChatMapping(int chatId) {	
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
    	
    	map.put(chatId, tabs.size());
    }
    
    @FXML
    public void listRooms(ActionEvent e) {
    	//JOptionPane.showConfirmDialog(null, "Got milk?");
    }
    
    @FXML
    public void about(ActionEvent e) {
    	JOptionPane.showConfirmDialog(null, "Got milk?");
    }
    /**
     * Writes a clients message to a chat-room
     * @param e
     */
    @FXML
    public void saySomething(ActionEvent e) {
    	String txt = toSay.getText();
    	if(!txt.equals("") && txt !=null) {
    		try {								// midlertidlig løsning
    			output.write("CHAT,1,"+ clientId +"," +txt);
				output.newLine();
				output.flush();

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    }
    
    @FXML
    public void saySomethingKey(KeyEvent e) {
    	if(e.getCode() == KeyCode.ENTER)
    		saySomething(new ActionEvent());
    }
    
   /* Mulig vi ikke trenger disse
    * @FXML
    public void cancel(ActionEvent e) {
    	// TODO: Lukke current vindu man står i (gå tilbake)
    }
    
    @FXML
    public void sendToServer(ActionEvent e) {
    	// TODO: Må ta en string som skal sjekkes opp mot server.
    	// 		 Gir bruker svar/sender videre basert på resultat
    } */
    
    
    /**
     * Handles the action of one player that tries to enter a
     * new random game with said button
     * @param e the buttonclick action
     */
    @FXML
    public void joinRandomGame(ActionEvent e) {  	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
    	loader.setResources(I18N.getRsb());

		GameBoardController controller = loader.getController();
		// Use controller to set up communication for this game.
		// Note, a new game tab would be created due to some communication from the server
		// This is here purely to illustrate how a layout is loaded and added to a tab pane.
		
    	try {
    		AnchorPane gameBoard = loader.load();
    		gameId++;
        	Tab tab = new Tab("Game" + gameId);
    		tab.setContent(gameBoard);
        	tabbedPane.getTabs().add(tab);
    	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
     }

	
    
    
    public void setRoot(Stage stage) {
    	this.root = stage;
    }
    
}
