package no.ntnu.imt3281.ludo.logic;

public class NoRoomForMorePlayersException extends java.lang.RuntimeException {

	public NoRoomForMorePlayersException(String txt) {
		System.err.printf(txt);
	}
}
