/**
 * 
 */
package no.ntnu.imt3281.ludo.logic;

/**
 * Interface which listen to Player
 * @author Guro
 *
 */
public interface PlayerListener extends EventListener {

	public void playerStateChanged(PlayerEvent playerEvent);
	
}
