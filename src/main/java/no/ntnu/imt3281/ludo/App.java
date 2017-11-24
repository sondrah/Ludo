package no.ntnu.imt3281.ludo;

import javafx.application.Application;
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
		ServerController servercontroller = new ServerController();
		Client c = new Client(args);
	}
}
