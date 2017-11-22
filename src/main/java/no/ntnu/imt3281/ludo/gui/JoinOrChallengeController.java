package no.ntnu.imt3281.ludo.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class JoinOrChallengeController {
	
	@FXML private Label desc;
	@FXML private Label error;
	@FXML private Button cancel;
	@FXML private Button confirm;
	
	@FXML
	public void cancelWindow() {
		stage.close();
	}
	
	@FXML
	public void confirmText() {
		
	}
	
	
	
	
}
