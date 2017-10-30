package no.ntnu.imt3281.ludo.logic;



public class NotEnoughPlayersException extends java.lang.RuntimeException {
	
	public NotEnoughPlayersException(String txt) {
		super(txt);
		System.err.printf(txt);
	}							// System.out eller System.err ??
}
		