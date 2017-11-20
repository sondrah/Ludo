package no.ntnu.imt3281.ludo.server;


import java.sql.SQLException;
import java.util.Vector;

/**
 * Master-server who controls DB, chats & games  
 * 
 */
public class ServerController {

	/** The 'url' to our database (local) */
	private String url = "jdbc:derby:BadgerDB;";
	/** Unique ID for each chat */
	private int chatID = 0;
	/** A vector all chats */
	public Vector<TheChatServer> chats;
	
	/** Starts up an object of ServerController */
	public static void main(String[] args) {
		ServerController servercontroller = new ServerController();
	}
	
	
	public ServerController() {		
		
		try {
			Database db = new Database(url);		// tries to connect to DB	
		} catch(SQLException sqle) {			
			url += "create=true";				
			sqle.printStackTrace();
			try {
				Database db = new Database(url);	// if DB not found, adds "create=true" to make DB
			} catch(SQLException sqle2) {
				System.err.println("No DB after 2nd try"); 
				sqle2.printStackTrace();
			}
		}
													// Makes masterchat for all logged in players
		TheChatServer masterchat = new TheChatServer("Master chat", ++chatID);
		// TODO
		// alle nye chats som skal legges til sjekkes at det ikke er duplikater
		//  av chatname og ID før chatserver blir laget
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
}