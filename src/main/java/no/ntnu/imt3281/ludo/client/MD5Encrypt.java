package no.ntnu.imt3281.ludo.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import no.ntnu.imt3281.ludo.Logging;

/**
 * Borrowed from this:
 * https://stackoverflow.com/questions/6592010/encrypt-and-decrypt-a-password-in-java
 * 
 */
public class MD5Encrypt {
   private static MessageDigest md;

   /**
    * Used for encryption
    * @param pass password to be encrypted 
    * @return cryptated string of password
    */
   public static String cryptWithMD5(String pass){
    try {
        md = MessageDigest.getInstance("MD5");
        byte[] passBytes = pass.getBytes();
        md.reset();
        byte[] digested = md.digest(passBytes);
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < digested.length; i++){
            sb.append(Integer.toHexString(0xff & digested[i]));
        }
        return sb.toString();
    } catch (NoSuchAlgorithmException ex) {
    	Logging.log(ex.getStackTrace());
    }
        return null;


   }
}
