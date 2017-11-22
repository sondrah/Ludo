package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JoinOrChallengeController {
	
	@FXML private Label desc;
	@FXML private Label error;
	@FXML private Button cancel;
	@FXML private Button confirm;
	@FXML private TextField message;
	
	
	public void setDesc(String d) {
		desc.setText(d);
	}
	
	public void setError(String e) {
		error.setText(e);
	}
	
	@FXML
	public void cancelWindow(ActionEvent e) {
		((Node)(e.getSource())).getScene().getWindow().hide();
	}
	
	public void confirmText(ActionEvent e) {
		
	}
	
	
	
	
}
