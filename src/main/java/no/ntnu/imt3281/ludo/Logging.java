package no.ntnu.imt3281.ludo;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {

	
	/** Logs all exceptions */
	Logger exceptionlogger = Logger.getLogger("ExceptionLogg");  
    /** Sets up file for 'exceptionlogger' to write to */
	FileHandler fh;
	
	
	public void Logging() {
		
		try {
			fh  = new FileHandler("./src/main/java/no/ntnu/imt3281/ludo/exceptionlogg.log");	// path to log file
			exceptionlogger.addHandler(fh);
			    
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
