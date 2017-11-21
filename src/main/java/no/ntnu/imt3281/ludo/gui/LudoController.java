package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.swing.JOptionPane;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.layout.AnchorPane;
import no.ntnu.imt3281.i18n.I18N;

/**
 * Controlls all actions on the homepage
 * Has the masterchat, friendlists and other
 * functionalities like starting new games
 */
public class LudoController {
    
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu file;
    @FXML
    private MenuItem connect;
    @FXML
    private MenuItem close;
    @FXML
    private Menu game;
    @FXML
    private MenuItem challenge;
    @FXML
    private MenuItem random;
    @FXML
    private Menu chat;
    @FXML
    private MenuItem join;
    @FXML
    private MenuItem listRooms;
    @FXML
    private Menu help;
    @FXML
    private MenuItem about;
    @FXML
    private TextArea masterChat;
    @FXML
    private Button say;
    @FXML
    private Button cancel;
    @FXML
    private Button send; 
    @FXML
    private Label errorMessage;
    @FXML
    private Label userName;
    @FXML
    private TextField toSay;
    @FXML
    private TabPane tabbedPane;
    @FXML
    private TitledPane joinOrChallenge;

    private int gameId = 0;
    
    private int userId;
    
    
    public void setUserName(String usr) {
    	if(userName == null) System.err.println("YENSE");
    	userName.setText(usr);
    }

    private int clientId;
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;

    
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
 /*   public void processConnection() {
        while (true) {
            try {
                String tmp = input.readLine();
                if (tmp.startsWith("LOGIN:")) { // User is logging in
                    addUser(tmp.substring(6));
                } else if (tmp.startsWith("LOGOUT:")) { // User is logging out
                    removeUser(tmp.substring(7));
                } else { // All other messages
                    displayMessage(tmp + "\n");
                }
            } catch (IOException ioe) {
            	JOptionPane.showMessageDialog(this, "Error receiving data: "
                        + ioe);
            }
        }
    } */
    
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
    	joinOrChallenge.setVisible(true);
    	errorMessage.setVisible(false);
    	joinOrChallenge.setText(I18N.tr("ludo.challengePlayer"));
    }
    
    
    @FXML
    public void joinChat(ActionEvent e) {
    	// TODO: Ta inn chatnavn, la person joine om finnes
    	joinOrChallenge.setVisible(true);
    	errorMessage.setVisible(false);
    	joinOrChallenge.setText(I18N.tr("ludo.joinChat"));
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
    
    @FXML
    public void saySomething(ActionEvent e) {
    	String txt = toSay.getText();
    	if(!txt.equals("")) {
    		try {								// midlertidlig løsning
				output.write("CHAT,1,"+ clientId +"," +txt);
				output.newLine();
				output.flush();
				
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
    public void cancel(ActionEvent e) {
    	// TODO: Lukke current vindu man står i (gå tilbake)
    	joinOrChallenge.setVisible(false);
    }
    
    @FXML
    public void sendToServer(ActionEvent e) {
    	// TODO: Må ta en string som skal sjekkes opp mot server.
    	// 		 Gir bruker svar/sender videre basert på resultat
    }
    
    
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
