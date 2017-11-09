package no.ntnu.imt3281.ludo.logic;

import java.util.Objects;

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
	 * @param obj object that calls the event
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		
		if(obj instanceof PieceEvent && obj != null) {
			PieceEvent temp = (PieceEvent) obj;
			return (this.player == temp.getPlayer() && this.piece == temp.getPiece()
					&& this.from == temp.getFrom() && this.to == temp.getTo());
		}
		else return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
    public int hashCode() {
		// makes a hash put of the given paramenters
        return Objects.hash(player, piece, from, to);
    }
	
	/**
	 * gets player in the current turn
	 * @return integer player index
	 */
	public int getPlayer() {
		return player;
	}
	
	/**
	 * sets the player in the current turn
	 * @param player index of the player that is given the turn
	 */
	public void setPlayer(int player) {
		this.player = player;
	}
	
	/**
	 * gets chosen piece for the current players turn
	 * @return integer, players chosen piece
	 */
	public int getPiece() {
		return piece;
	}
	
	/**
	 * sets chosen piece
	 * @param piece index of piece chosen
	 */
	public void setPiece(int piece) {
		this.piece = piece;
	}
	
	/**
	 * gets the position from which a piece moves
	 * @return integer, index of current position of a piece
	 */
	public int getFrom() {
		return from;
	}
	
	/**
	 * sets the position where a piece is located before moving
	 * @param from integer of field index with a piece
	 */
	public void setFrom(int from) {
		this.from = from;
	}
	
	/**
	 * gets the position where a piece is supposed to move
	 * @return integer, field index for a piece's intended destination
	 */
	public int getTo() {
		return to;
	}
	
	/**
	 * sets the position to where the piece should move
	 * @param to field index for intended destination of a piece
	 */
	public void setTo(int to) {
		this.to = to;
	}
	
	@Override
	public String toString() {
		StringBuilder piecestring = new StringBuilder();
		piecestring.append("Player: " + getPlayer() + " Piece: " + getPiece()
		+ " From: " + getFrom() + " To: " + getTo());
		return piecestring.toString();
	}
	
}
