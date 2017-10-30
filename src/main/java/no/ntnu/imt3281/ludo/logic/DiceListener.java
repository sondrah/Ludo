package no.ntnu.imt3281.ludo.logic;
/**
 * Interface which listen to Dice
 * @author Guro
 *
 */
public interface DiceListener extends EventListener{

	public void diceThrown(DiceEvent diceEvent);
}
