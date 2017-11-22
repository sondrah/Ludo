package no.ntnu.imt3281.ludo;

import javafx.application.Application;
import no.ntnu.imt3281.ludo.client.Client;
import no.ntnu.imt3281.ludo.server.ServerController;

public class App {
	
	public static void main(String[] args) {
		ServerController servercontroller = new ServerController();
		Client c = new Client(args);
	}

}
