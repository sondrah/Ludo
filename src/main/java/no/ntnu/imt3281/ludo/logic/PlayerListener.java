/**
 * 
 */
package no.ntnu.imt3281.ludo.logic;

/**
 * Interface which listen to Player
 */
public interface PlayerListener extends EventListener {

	/**
	 * Used so that a class implementing this can receive
	 * PlayerEvents from the Ludo game server
	 * @param playerEvent an event that a players state has changed
	 */
	public void playerStateChanged(PlayerEvent playerEvent);
	
}
