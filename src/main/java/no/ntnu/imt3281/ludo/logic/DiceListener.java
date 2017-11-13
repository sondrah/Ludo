package no.ntnu.imt3281.ludo.logic;

/**
 * Interface which listen to Dice
 */
public interface DiceListener extends EventListener{
	
	/**
	 * Used so that a class implementing this can receive
	 * DiceEvents from the Ludo game server.
	 * @param diceEvent an event that a dice has been thrown
	 */
	public void diceThrown(DiceEvent diceEvent);
}
