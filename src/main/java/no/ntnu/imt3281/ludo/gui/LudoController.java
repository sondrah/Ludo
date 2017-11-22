package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

    private int gameId = 0;
    
    private int userId;
    
    
    public void setUserName(String usr) {
    	userName.setText(usr);
    }

    private int clientId;
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private DefaultListModel<String> participantsModel;

    
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

    
    @FXML
    public void connect(ActionEvent e) {
    	// TODO:
    }
    
    public void setUserId(int id) {
    	this.clientId = id;
    }
    
    /**
     * ca 4000 studenter, i gjøvik 30 000 tusen
     * bra studentmiljø 
     * This method handles the communication from the server. Note that this
     * method never returns, messages from the server is read in a loop that
     * never ends. All other user interaction is handled in the GUI thread.
     * 
     * Login and logout messages is used to add/remove users to/from the list of
     * participants while all other messages are displayed.
     */

    public void processConnection() {
        while (true) {  // Sjekker hele tiden etter innkommende meldinger 
            try {
                
                String retMessage = input.readLine();	
				String[] returnMessage = retMessage.split(",");
				String type = returnMessage[0];
				String actionId = returnMessage[1];	
				String receivedClientId = returnMessage[2];	
				String message = returnMessage[3];
				
				// masterChat.setText(message);
                if (type.equals("CHAT")) { // User is logging in
                	if (message.startsWith("0")) {
                		addNewTabtoChatMapping(actionId);
                	}
                	routeChatMessage(message, actionId);
                	
                // } else if (type.equals("LOGOUT")) { // User is logging out removeUser(tmp.substring(7));
                } else { // All other messages
                    
                }
            } catch (IOException ioe) {
            	System.err.println("Error receiving data: ");
                       
            }
        }		// While true end
    }
    
    /**
     * Used to add messages to the message area in a thread safe manner
     * 
     * @param text
     *            the text to be added
     */
    private void routeChatMessage(String message, String retChatId) {
    	int returnedChatId = Integer.parseInt(retChatId);
    	int curChatId = 1; // TODO hardcode
    	int curTabId = 1; 
    	// her Sondre Itere gjennom mapping 	
    	if (returnedChatId == curChatId)
    		curTabId = curTabId;
    		
    		// Henter ut riktig Anchor Pane for riktig klient?? 
    	AnchorPane tabRoot = (AnchorPane) tabbedPane.getTabs().get(curTabId).getContent();
    	Iterator<Node> it = tabRoot.getChildren().iterator();
    			
    	while(it.hasNext()) {
    		Node n = it.next();
    		String nodeID = n.getId();
    		
    		if(nodeID.equals("chatArea")) {
    			TextArea text = (TextArea) n;
    			text.appendText(message);
    		}
    	}

	    	
	    		SwingUtilities.invokeLater(() -> text.append(message));
 
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
    public void challenge(ActionEvent e) {
    	// TODO: vis liste av alle active spillere
    	// TODO: kunne velge disse
    	// TODO: sende request
    	// TODO: motta svar?
    }
    
    
    @FXML
    public void joinChat(ActionEvent e) {
    	// TODO: Ta inn chatnavn, la person joine om finnes
    }
    
    
    @FXML
    public void listRooms(ActionEvent e) {
    	// TODO: make a list of all available chats
    	
    	// ScrollPane(chat.show())
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
    	if(!txt.equals("")) {
    		try {								// midlertidlig løsning
				output.write("CHAT,1,"+ clientId +"," +txt);
				output.newLine();
				output.flush();
				
				// skal ikke message listener gjøre dette?
				
				String res = input.readLine();	// vente på melding?
				String[] msg = res.split(",");
				String type = msg[0];
				String chatId = msg[1];	
				String receivedClientId = msg[2];	
				String message = msg[3];
				
				masterChat.setText(message);
				
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
    
}
