/**
 * 
 */
package no.ntnu.imt3281.ludo.logic;

/**Interface which listen to the Pices 
 * @author Guro
 *
 */
public interface PieceListener extends EventListener {
	
	public void piceMoved(PieceEvent pieceEvent);

}
