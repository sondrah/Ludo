package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.logic.Ludo;


/**
 * This class has the necessary info the user needs
 * to initiate his instance of ludo
 */
public class GameInfo {
	private int gameID;
	private String[] playerNames;
	
	/**
	 * Constructs a GameInfo Object with the given parameters
	 * @param gameID - The ID of the game the player is in
	 * @param playerNames - The names of all players in the game
	 */
	public GameInfo(int gameID, String[] playerNames) {
		playerNames = new String[Ludo.MAX_PLAYERS];
		
		setGameID(gameID);
		setPlayerNames(playerNames);
	}
	
	
	/**
	 * Sets the gameID
	 * @param gameID - ID of the game
	 */
	public void setGameID(int gameID) {
		this.gameID = gameID;
	}
	
	/**
	 * Sets the playerNames array
	 * @param playerNames An array with the playernames
	 */
	public void setPlayerNames(String[] playerNames) {
		this.playerNames = playerNames;
	}
	
	
	/**
	 * Gets the gameID
	 * @return The ID of a game
	 */
	public int getGameID() {
		return gameID;
	}
	
	
	/**
	 * Gets the playerNames
	 * @return An array of playernames
	 */
	public String[] getPlayerNames() {
		return playerNames;
	}
	
}
