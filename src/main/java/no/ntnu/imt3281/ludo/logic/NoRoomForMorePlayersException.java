package no.ntnu.imt3281.ludo.logic;



/**
 * Exception which is thrown if a game is full
 */
public class NoRoomForMorePlayersException extends java.lang.RuntimeException {

	
	/**
	 * Constructor that creates a new Exception with the given message
	 * @param txt displayed for user informing that game is full
	 */
	public NoRoomForMorePlayersException(String txt) {
		super(txt);
		System.err.printf(txt);
	}
}
