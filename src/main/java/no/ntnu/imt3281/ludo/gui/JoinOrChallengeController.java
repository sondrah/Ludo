package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * This class handles the joinOrChallenge tab
 * changes the view based on whether the client
 * pressed join room or challenge player
 */
public class JoinOrChallengeController {
	
	@FXML private Label desc;
	@FXML private Label error;
	@FXML private Button cancel;
	@FXML private Button confirm;
	@FXML private TextField message;
	
	
	/**
	 * sets the label to appropriate text
	 * @param d text that is to be set
	 */
	public void setDesc(String d) {
		desc.setText(d);
	}
	
	/**
	 * sets the error message to appropriate text
	 * @param e errortext that is to be sets
	 */
	public void setError(String e) {
		error.setText(e);
	}
	
	/**
	 * Supposed to close a tab
	 * @param e button click caused by cancel button
	 */
	@FXML
	public void cancelWindow(ActionEvent e) {
		
	}
	
	/**
	 * Supposed to send a text to server for checking
	 * @param e button click caused by confirm
	 */
	public void confirmText(ActionEvent e) {
		
	}
	
	
	
	
}
