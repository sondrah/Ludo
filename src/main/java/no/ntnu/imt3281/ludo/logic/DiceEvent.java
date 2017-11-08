	package no.ntnu.imt3281.ludo.logic;

/**
 * @author Snorre
 *
 * This Class is supposed to inform players that a dice has been thrown.
 * A DiceEvent will be passed to all registered DiceListener objects.
 * Included in this Class is a reference to a ludo-game, which player
 * is currently active, and the value of the dice.
 */
public class DiceEvent extends java.util.EventObject {
	
	private int player;
	private int dice;
	
	/**
	 * Constructs a DiceEvent with the given object
	 * @param obj is the object that calls the event
	 */
	public DiceEvent(Object obj) {
		super(obj);
	}
	
	/**
	 * Constructs a DiceEvent with the given object and
	 * integers player and dice
	 * @param obj the object that calls the event
	 * @param player index of the player that calls the event
	 * @param dice the value of the dice thrown
	 */
	public DiceEvent(Object obj, int player, int dice) {
		super(obj);
		setPlayer(player);
		setDice(dice);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		DiceEvent temp = (DiceEvent) obj;
		return (this.player == temp.getPlayer() && this.dice == temp.getDice());
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
	 * gets the value of the dice thrown
	 * @return index of the dice thrown 1,2,3,4,5,6
	 */
	public int getDice() {
		return dice;
	}
	
	/**
	 * sets the value of the dice thrown
	 * @param dice value of the current dice thrown
	 */
	public void setDice(int dice) {
		this.dice = dice;
	}
	
	@Override
	public String toString() {
		StringBuilder dicestring = new StringBuilder();
		dicestring.append("Player: " + getPlayer() + " Dice: " + getDice());
		return dicestring.toString();
	}
}
