package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import no.ntnu.imt3281.i18n.I18N;

/**
 * Controlls all actions on the homepage
 * Has the masterchat, friendlists and other
 * functionalities like starting new games
 */
public class LudoController {

    @FXML
    private MenuItem random;

    @FXML
    private TabPane tabbedPane;

    /**
     * Handles the action of one player that tries to enter a
     * new random game with said button
     * @param e the buttonclick action
     */
    @FXML
    public void joinRandomGame(ActionEvent e) {  	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
    	loader.setResources(I18N.getRsb());

		GameBoardController controller = loader.getController();
		// Use controller to set up communication for this game.
		// Note, a new game tab would be created due to some communication from the server
		// This is here purely to illustrate how a layout is loaded and added to a tab pane.
		
    	try {
    		AnchorPane gameBoard = loader.load();
        	Tab tab = new Tab("Game");
    		tab.setContent(gameBoard);
        	tabbedPane.getTabs().add(tab);
    	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
}
