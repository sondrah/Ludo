package no.ntnu.imt3281.ludo.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import no.ntnu.imt3281.i18n.I18N;
import no.ntnu.imt3281.ludo.gui.WelcomeController;
import no.ntnu.imt3281.ludo.Logging;

/**
 * Launches the application and creates a welcome screen window.
 * Displays this to the user.
 */
public class Client extends Application{
	
	/**
	 * Constructs a new Client
	 */
	public Client() {
		I18N.getResource("no.ntnu.imt3281.i18n.i18n");
		Logging.setup();
	}
	
	/**
	 * 
	 * Constructs a new client with the parameters args
	 * @param args included arguments
	 */
	public Client(String[] args) {
		launch(args);
	}
	
	/**
	 * launches the application
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Client c = new Client(args);
	}
	
	// Launches the initial window of the application (Welcome screen) into a scene
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Ludo-Alea-iacta-est");
		
		AnchorPane root = new AnchorPane();
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/no/ntnu/imt3281/ludo/gui/WelcomeScreen.fxml"));
    	loader.setResources(I18N.getRsb());

    	@SuppressWarnings("unused")
		WelcomeController controller = loader.getController();
		
    	try {
    		Pane pane = loader.load();
    		root.getChildren().add(pane);
    		
    		primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();
    	}
    	catch (IOException ioe){
    		Logging.log(ioe.getStackTrace()); 
    	}
		
	}
	
}
