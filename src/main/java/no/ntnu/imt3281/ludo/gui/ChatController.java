package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ChatController {
	
   //  private BufferedReader inputChat;
    private BufferedWriter outputChat;
	private Socket chatSocket;
	public int chatId;
	public ChatController() {
		
		
		
	}
	
	  /**
     * Gives game-chat an Id
     * @param chatId Id of game-chat
     */
    public void setChatId(int chatId) {
    	this.chatId = chatId;
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
	
}
