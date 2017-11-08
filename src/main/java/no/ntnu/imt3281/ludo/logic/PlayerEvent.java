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
	 * Constructs a PlayerEvent with given object
	 * @param obj object that calls this event
	 */
	public PlayerEvent(Object obj) 
	{
		super(obj);
	}
	
	/**
	 * Constructs a PlayerEvent with given object and
	 * integers activePlayer and state
	 * @param obj object that calls the event
	 * @param active
	 * @param state
	 */
	public PlayerEvent(Object obj, int active, int state) {
		super(obj);
		setActivePlayer(active);
		setState(state);
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		PlayerEvent temp = (PlayerEvent) obj;
		return (this.activePlayer == temp.getActivePlayer() && this.state == temp.getState());
	}

	/**
	 * gets current active player
	 * @return integer value of current active player. (ref. Ludo class)
	 */
	public int getActivePlayer() {
		return activePlayer;
	}
	
	/**
	 * sets current active player
	 * @param active index(color) of the player that is to be set as active
	 */
	public void setActivePlayer(int active) {
		this.activePlayer = active;
	}
	
	/**
	 * gets the current state of an active player (Playing, waiting..)
	 * @return integer, current state
	 */
	public int getState() {
		return state;
	}

	/**
	 * sets current state of an active player.
	 * @param state index of current state
	 */
	public void setState(int state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		StringBuilder playerstring = new StringBuilder();
		playerstring.append("Active Player: " + getActivePlayer() + " State: " + getState());
		return playerstring.toString();
	}
	
}
