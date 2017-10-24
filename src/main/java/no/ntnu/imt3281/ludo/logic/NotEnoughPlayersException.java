package no.ntnu.imt3281.ludo.logic;



public class NotEnoughPlayersException extends Exception {
	
	
	public NotEnoughPlayersException(String txt) {
											// What is going on here.... BRACKFAST?
		System.err.printf(txt, NotEnoughPlayersException);
		
		System.out.printf("The number of players must be more than 2");
	}
	
	
	
	
	public String getMessage() {
		return "No exception should be thrown";
	}
	
	

}
