/**
 * 
 */
package no.ntnu.imt3281.ludo.logic;

/** 
 * Interface which listen to the Pieces
 */
public interface PieceListener extends EventListener {
	
	/**
	 * Used so that a class implementing this can receive
	 * DiceEvents from the Ludo game server
	 * @param pieceEvent event that a piece has moved
	 */
	public void pieceMoved(PieceEvent pieceEvent);

}
