package no.ntnu.imt3281.ludo.server;

/**
 * This class holds data about about a game
 * so we can send this easily to the server
 * and treat it there
 */
public class GameEvent {
	
	private int gameID;
	private Object event;
	
	
	/**
	 * Constructs a GameEvent with the given parameters
	 * @param gameID - The ID of the game
	 * @param event - The event that needs to be sent
	 */
	public GameEvent(int gameID, Object event) {
		setGameID(gameID);
		setEvent(event);
	}
	
	
	/**
	 * Sets the game ID
	 * @param gameID - The ID of a game
	 */
	public void setGameID(int gameID) {
		this.gameID = gameID;
	}
	
	
	/**
	 * Gets the game ID 
	 * @return The ID of the game
	 */
	public int getGameID() {
		return gameID;
	}
	
	
	/**
	 * Sets the event that is sent
	 * @param event - An event that is any of the
	 * events in the 'logic'-package
	 */
	public void setEvent(Object event) {
		this.event = event;
	}
	
	
	/**
	 * Gets the event that is sent
	 * @return An event in the 'logic'-package as Object
	 */
	public Object getEvent() {
		return event;
	}
	
}
