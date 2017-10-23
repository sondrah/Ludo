package no.ntnu.imt3281.ludo.logic;

/**
 * @author Snorre
 * This Class is meant to inform players that a piece has been moved.
 * Informs about which player has moved which of their pieces,
 * and from current position, to intended position.
 */
public class PieceEvent extends java.util.EventObject {

	private int player;
	private int piece;		// index of a piece in a players piece-array (0,1,2,3)
	private int from;
	private int to;
	
	/**
	 * Constructs a PieceEvent with the given object
	 * @param obj the object that calls the event
	 */
	public PieceEvent(Object obj) {
		super(obj);
	}
	
	/**
	 * Constructs a PieceEvent with the given object and
	 * integers player, piece, from, to.
	 * @param obj object the calls the event
	 * @param player index of the player that calls the event
	 * @param piece index of the piece in the current players
	 * 		  piece-array, 0,1,2,3
	 * @param from index of the field on the board the given piece
	 * 		  is located
	 * @param to index of the field where the given piece is
	 *  	  supposed to move
	 */
	public PieceEvent(Object obj, int player, int piece, int from, int to) {
		super(obj);
		setPlayer(player);
		setPiece(piece);
		setFrom(from);
		setTo(to);
	}
	
	/**
	 * @return
	 */
	public int getPlayer() {
		return player;
	}
	
	/**
	 * @param player
	 */
	public void setPlayer(int player) {
		this.player = player;
	}
	
	/**
	 * @return
	 */
	public int getPiece() {
		return player;
	}
	
	/**
	 * @param piece
	 */
	public void setPiece(int piece) {
		this.player = piece;
	}
	
	/**
	 * @return
	 */
	public int getFrom() {
		return player;
	}
	
	/**
	 * @param from
	 */
	public void setFrom(int from) {
		this.player = from;
	}
	
	/**
	 * @return
	 */
	public int getTo() {
		return player;
	}
	
	/**
	 * @param to
	 */
	public void setTo(int to) {
		this.player = to;
	}
	
}
