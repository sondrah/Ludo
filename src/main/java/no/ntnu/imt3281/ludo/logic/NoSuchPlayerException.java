package no.ntnu.imt3281.ludo.logic;

public class NoSuchPlayerException extends java.lang.RuntimeException {

	public NoSuchPlayerException(String txt) {
		System.err.printf(txt);
	}
}
