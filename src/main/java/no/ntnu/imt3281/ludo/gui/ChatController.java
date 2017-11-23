package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class ChatController {
	@FXML private TextField toSay;
	
   //  private BufferedReader inputChat;
    private BufferedWriter outputChat;
	private Socket chatSocket;
	private int chatId;
	private int clientId;
	
	public ChatController() {
		
		
	}
	
	  /**
     * Gives game-chat an Id
     * @param chatId Id of game-chat
     */
    public void setChatId(int chatId, int clientId) {
    	this.chatId = chatId;
    	this.clientId = clientId;
    }
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
	
	@FXML
	public void sendText(ActionEvent e) {
		// TODO: Ratt akkurat som ælle andre chats tæinkje e? Socket connection?
    	String txt = toSay.getText();
    	if(!txt.equals("") && txt !=null) { 
    		try {			
    			outputChat.write("CHAT,"+chatId+","+clientId +"," +txt);
    			outputChat.newLine();
    			outputChat.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    	}
	}


}
