package no.ntnu.imt3281.ludo.logic;


/**
 * Exception thrown if not enough players to start game
 */
public class NotEnoughPlayersException extends java.lang.RuntimeException {
	
	/**
	 * Constructor that creates a new Exception with the given message
	 * @param txt displayed for user informing there is not enough players in game
	 */
	public NotEnoughPlayersException(String txt) {
		super(txt);
		System.err.printf(txt);
	}							
}
		