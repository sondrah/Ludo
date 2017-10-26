package no.ntnu.imt3281.ludo.logic;

public class IllegalPlayerNameException extends java.lang.RuntimeException {
	
	public IllegalPlayerNameException(String txt){
		System.err.printf(txt);
	}
	

}
