package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import no.ntnu.imt3281.i18n.I18N;
import no.ntnu.imt3281.ludo.gui.WelcomeController;
import javafx.event.ActionEvent;

public class Client extends Application{
	
	
	public Client() {
		//Stage primaryStage = new Stage();
		//start(primaryStage);
		
		I18N.getResource("no.ntnu.imt3281.i18n.i18n");
	}
	
	public static void main(String[] args) {
		Client c = new Client();
		launch(args);
	}
	
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Ludo");
		
		AnchorPane root = new AnchorPane();
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/no/ntnu/imt3281/ludo/gui/WelcomeScreen.fxml"));
    	loader.setResources(I18N.getRsb());
    	
    	WelcomeController controller = loader.getController();
		
    	try {
    		Pane pane = loader.load();
    		root.getChildren().add(pane);
    		
    		//root.getChildren().remove(pane);
    		
    		primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();
    	}
    	catch (IOException ioe){
    		ioe.printStackTrace();
    	}
		
	}
	
}
