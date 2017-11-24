package no.ntnu.imt3281.ludo;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class Logging {
	
	/** Logs all exceptions */
	private static Logger exceptionlogger = Logger.getLogger("ExceptionLogg");  
    /** Sets up file for 'exceptionlogger' to write to */
	private static FileHandler fh;
	
	
	private void Logging() {
		// static class has no constructor
	}


	/**
	 * Sets up the file handler
	 * NEEDS to be called first
	 */
	public static void setup() {
		try {
			fh  = new FileHandler("./src/main/java/no/ntnu/imt3281/ludo/exceptionlogg.log");	// path to log file
			exceptionlogger.addHandler(fh);
			    
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Logs the given msg to this log
	 * @param msg The message to be logged
	 */
	public static void log(String msg) {
		exceptionlogger.info(msg);
	}
	
}
