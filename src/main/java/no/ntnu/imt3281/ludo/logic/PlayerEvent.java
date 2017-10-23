package no.ntnu.imt3281.ludo.logic;



/**
 * 
 * @author sondrah
 *
 */
public class PlayerEvent extends java.util.EventObject {

	
	private static final int PLAYING;
	private static final int WAITING;
	private static final int LEFTGAME;
	private static final int WON;
	private int activePlayer;
	private int state;
	
	
	public PlayerEvent(Object source) 
	{
		super(source);
		
	}
	
	
	
	
	
}
