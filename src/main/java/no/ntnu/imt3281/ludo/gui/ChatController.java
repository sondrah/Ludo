package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ChatController {
	@FXML private TextField toSay;
	
    private BufferedReader input;
    private BufferedWriter output;
    private Socket socket;
    private int clientId;
	public int chatId;
	
	@FXML
	public void sendText(ActionEvent e) {
		// TODO: Ratt akkurat som ælle andre chats tæinkje e? Socket connection?
    	String txt = toSay.getText();
    	if(!txt.equals("") && txt !=null) { 
    		try {			
    			output.write("CHAT,"+chatId+","+clientId +"," +txt);
				output.newLine();
				output.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    	}
	}

	
}
