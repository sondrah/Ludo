package no.ntnu.imt3281.ludo.logic;



public class NotEnoughPlayersException extends Exception {
	
	
	public NotEnoughPlayersException(String txt) {  // ikke sikker om det skal være sånn
		System.out.printf(txt);
	}
	
	
	public String getMessage() {
		return "No exception should be thrown";
	}
	
	

}
		