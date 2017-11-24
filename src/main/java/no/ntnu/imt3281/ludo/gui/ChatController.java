package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


/**
 * This class controls private chats
 *
 */
public class ChatController {
	@FXML private TextField toSay;
	
    private BufferedWriter outputChat;
	private Socket chatSocket;
	private int chatId;
	private int clientId;
	
	  /**
     * Gives game-chat an Id
     * @param chatId Id of game-chat
     */
    public void setChatId(int chatId, int clientId) {
    	this.chatId = chatId;
    	this.clientId = clientId;
    }
    
    
    /**
     * creates a socket connection to the server for communication
     * @param socket the assigned socket
     */
    public void setConnection(Socket socket) {
    	try {
			this.chatSocket = socket;
			outputChat = new BufferedWriter(new OutputStreamWriter(
			        socket.getOutputStream()));
			
    	} catch(IOException ioe) {
    		System.err.println("fikk ikke connection, i chatController");
    		ioe.printStackTrace();
    		
    	}
    }
	
	/**
	 * takes text from the textField and transfers it through to the server
	 * @param e button click caused by the say button
	 */
	@FXML
	public void sendText(ActionEvent e) {
		// TODO: Ratt akkurat som ælle andre chats tæinkje e? Socket connection?
    	String txt = toSay.getText();
    	if(!txt.equals("") && txt !=null) { 
    		try {			
    			outputChat.write("CHAT,SAY,"+chatId+","+clientId +"," +txt);
    			outputChat.newLine();
    			outputChat.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    	}
	}


}
