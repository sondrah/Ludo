package no.ntnu.imt3281.ludo.logic;


/**
 * Exception thrown if player does not exist
 */
public class NoSuchPlayerException extends java.lang.RuntimeException {

	/**
	 * Constructor that creates a new Exception with the given message
	 * @param txt displayed for user informing 
	 */
	public NoSuchPlayerException(String txt) {
		super(txt);
		System.err.printf(txt);
	}
}
