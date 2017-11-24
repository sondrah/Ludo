package no.ntnu.imt3281.ludo;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
import no.ntnu.imt3281.ludo.client.Client;
import no.ntnu.imt3281.ludo.server.ServerController;

/**
 * Starts program
 *
 */
public class App {
	
	/**
	 * Starts server and client
	 */
	public static void main(String[] args) {
		
		try {
			/** Logs all exceptions */
			Logger exceptionlogger = Logger.getLogger("ExceptionLogg");  
		    /** Sets up file for 'exceptionlogger' to write to */
			FileHandler fh = new FileHandler("./src/main/java/no/ntnu/imt3281/ludo/exceptionlogg.log");	// path to log file
	        exceptionlogger.addHandler(fh);
	        
	        SimpleFormatter formatter = new SimpleFormatter();
	        fh.setFormatter(formatter);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		
		ServerController servercontroller = new ServerController();
		Client c = new Client(args);
		// Client cc = new Client(args);
	}
}
