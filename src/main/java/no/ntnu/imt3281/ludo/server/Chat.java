package no.ntnu.imt3281.ludo.server;


/*
 * Holds one chat-text with information about what chatSever
 * it belongs to
 * @chatID unique ID for every chat
 * @message string with the text the client writes
 */
public class Chat {

	private static int chatID;
	//private String chatname;	 ER VEL IKKE NØDVENDIG?
	private String message;
	
	public Chat() {
		super();
	}
	
	/*
	 * Sets up a chatobject with
	 * @cname name of the chat
	 */
	public Chat(String cname) {
		super();
		//setName(cname);
		// TODO databasekall som spør etter en id (egen funksjon i databaseklassen?)
	}
	/*
	private void setName(String cname) {
		this.chatname = cname;
	}
	
	public String getName() {
		return chatname;
	}
	*/
	public int getID() {
		return chatID;
	}
	
	public void setMessage(String txt) {
		this.message = txt;
	}
	
	public String getMessage() {
		return message;
	}
	
}
