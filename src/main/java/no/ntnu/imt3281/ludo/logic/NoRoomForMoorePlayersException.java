package no.ntnu.imt3281.ludo.logic;

public class NoRoomForMoorePlayersException extends java.lang.RuntimeException {

	public NoRoomForMoorePlayersException(String txt) {
		System.err.printf(txt);
	}
}
