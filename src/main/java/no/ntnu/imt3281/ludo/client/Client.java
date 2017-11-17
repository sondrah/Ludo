package no.ntnu.imt3281.ludo.client;

import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.gui.GameBoardController;

public class Client extends Application{

	
	public Client() {
		Stage primaryStage = new Stage();
		start(primaryStage);
	}
	
	public static void main(String[] args) {
		Client c = new Client();
	}
	
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Ludo");
		
		AnchorPane root = new AnchorPane();
		
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("WelcomeScreen.fxml"));
    	loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));
    	
    	WelcomeController controller = loader.getController();
		
		Pane pane = loader.load();
		
		root.getChildren().add(pane);
		
	}
}
