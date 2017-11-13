package no.ntnu.imt3281.ludo.server;

public class Chat {

	private static int chatID;
	private String chatname;
	
	public Chat() {
		super();
	}
	
	public Chat(String cname) {
		super();
		setName(cname);
		// TODO databasekall som spør etter en id (egen funksjon i databaseklassen?)
	}

	private void setName(String cname) {
		this.chatname = cname;
	}
	
	public String getName() {
		return chatname;
	}
	
	public int getID() {
		return chatID;
	}
	
}
