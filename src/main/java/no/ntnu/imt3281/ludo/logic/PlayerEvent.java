package no.ntnu.imt3281.ludo.logic;


/**
 * @author Snorre
 *
 */
public class PlayerEvent extends java.util.EventObject {

	
	public static final int PLAYING = 0;
	public static final int WAITING = 1;
	public static final int LEFTGAME = 2;
	public static final int WON = 3;
	private int activePlayer;
	private int state;
	
	
	/**
	 * @param obj
	 */
	public PlayerEvent(Object obj) 
	{
		super(obj);
	}
	
	/**
	 * @param obj
	 * @param active
	 * @param state
	 */
	public PlayerEvent(Object obj, int active, int state) {
		super(obj);
		setActivePlayer(active);
		setState(state);
	}
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean Equals(Object obj) {
		return true;						//FIXME
	}

	/**
	 * @return
	 */
	public int getActivePlayer() {
		return activePlayer;
	}
	
	/**
	 * @param active
	 */
	public void setActivePlayer(int active) {
		this.activePlayer = active;
	}
	
	/**
	 * @return
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state
	 */
	public void setState(int state) {
		this.state = state;
	}
	
	
	
	
	
}
