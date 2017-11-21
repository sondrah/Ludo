package no.ntnu.imt3281.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Static Wrapper class for I18N
 * This class will provide projectwide I18N
 */
public final class I18N{
	
	private I18N(){
		// this is just to make this class
		// uninitiable. This should not
		// be doing anything either
	}
    
	/*
	 * @field rsb holds static copy of current language-resources
	 */
    private static ResourceBundle rsb;
   
    /**
     * sets rsb to get resources from a .properties file
     * @param file the path to the .properties-files
     * @param locale the local to be retreived
     */
    public static void getResource(String file, Locale locale){
        // TODO: check for valid string FILE and LOCALE
    	rsb = ResourceBundle.getBundle(file, locale);
    }
    
    /**
     * Sets rsb to get resources from the given file with the
     * standard locale
     * @param file Path to the i18n-file
     */
    public static void getResource(String file) {
    	rsb = ResourceBundle.getBundle(file, new Locale("en", "US"));
    }
    
    /**
     * Gets a translation of the given string
     * 
     * @param s the string to "look up"/trandslate
     * @return the translation(tr) of provided string from
     * current resources
     */
    public static String tr(String s){
    	String res = "";
    	if(rsb != null){
    		res = rsb.getString(s);
    	}
    	//TODO: throw an exception if no rsb? Should never happen!
    	
    	return res;
    }
    
    
    /**
     * Returns the current resourcebundle
     * @return The current Resources read
     */
    public static ResourceBundle getRsb() {
    	return rsb;
    }
}