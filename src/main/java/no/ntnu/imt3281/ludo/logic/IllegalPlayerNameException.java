package no.ntnu.imt3281.ludo.logic;

/**
 * Defines an IllegalPlayerNameException used to
 * detect illegal playernames
 */
public class IllegalPlayerNameException extends java.lang.RuntimeException {
	
	/**
	 * Constructor that creates a new Exception with the
	 * given message
	 * @param txt Exceptionmessage
	 */
	public IllegalPlayerNameException(String txt) {
		super(txt);
		System.err.printf(txt);
	}
	

}
