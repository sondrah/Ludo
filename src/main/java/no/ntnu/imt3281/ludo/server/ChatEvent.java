package no.ntnu.imt3281.ludo.server;


/**
 * This class holds data about about a chat
 * so we can send this easily to the server
 * and treat it there
 */
public class ChatEvent {

	private int chatID;
	private int userID;
	private String message;
	
	/**
	 * Constructs a ChatEvent with the given parameters
	 * @param chatID - The ID of the chat spoken in
	 * @param userID - The ID of the user who spoke
	 * @param message - The message the user sent
	 */
	public ChatEvent(int chatID, int userID, String message) {
		setChatID(chatID);
		setUserID(userID);
		setMessage(message);
	}


	/**
	 * Gets the chat ID
	 * @return ID of the chat spoken in
	 */
	public int getChatID() {
		return chatID;
	}


	/**
	 * Sets the ID of the chat
	 * @param chatID - ID of the chat spoken in
	 */
	public void setChatID(int chatID) {
		this.chatID = chatID;
	}


	/**
	 * Gets the user that spoke
	 * @return ID of the user who spoke
	 */
	public int getUserID() {
		return userID;
	}


	/**
	 * Sets the ID of the user
	 * @param userID - ID of the user that spoke
	 */
	public void setUserID(int userID) {
		this.userID = userID;
	}


	/**
	 * Gets the message that is sent
	 * @return The message that we send
	 */
	public String getMessage() {
		return message;
	}


	/**
	 * Sets the message we want to send
	 * @param message - The message the user sent
	 */
	public void setMessage(String message) {
		this.message = message;
	}
		
}
