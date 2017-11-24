package no.ntnu.imt3281.ludo;

import no.ntnu.imt3281.ludo.client.Client;
import no.ntnu.imt3281.ludo.server.ServerController;

/**
 * Starts program
 *
 */
public class App {
	
	/**
	 * Starts server and client simultaneously
	 */
	public static void main(String[] args) {	
		@SuppressWarnings("unused")
		ServerController servercontroller = new ServerController();
		@SuppressWarnings("unused")
		Client c = new Client(args);
		
	}
}
