package no.ntnu.imt3281.ludo.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import no.ntnu.imt3281.i18n.I18N;
import no.ntnu.imt3281.ludo.client.MD5Encrypt;


/**
 * This class acts as a controller for the "home" menus.
 * Performs different actions based on button clicks, and sets
 * labels, buttons, textfield to true/false based on what the
 * current menu is.
 */
public class WelcomeController {
	@FXML
	private Pane paneHome;
	@FXML
	private Button btnHomeLogin;
	@FXML
	private Button btnHomeRegister;
	@FXML
	private Button btnLogin;
	@FXML
	private Button btnRegister;
	@FXML
	private Button btnHome;
	@FXML
	private Label lblHeader;
	@FXML
	private Label lblInfo;
	@FXML
	private Label lblPassword;
	@FXML
	private Label lblPassword2;
	@FXML
	private Label lblUsername;
	@FXML
	private Label lblError;
	@FXML
	private TextField txtFieldUsername ;
	@FXML
	private PasswordField txtFieldPassword;
	@FXML
	private PasswordField txtFieldPassword2;
	
	
	/**
	 * determines the visual elements for the login screen
	 * @param event button click caused by the login button of the home screen
	 */
	@FXML
	public void goToLogin(ActionEvent event) {
		btnHomeLogin.setVisible(false);
		btnHomeRegister.setVisible(false);
		lblInfo.setVisible(false);
		
		lblHeader.setText(I18N.tr("welcomescreen.login"));
		
		lblUsername.setVisible(true);
		lblPassword.setVisible(true);
		txtFieldUsername.setVisible(true);
		txtFieldPassword.setVisible(true);
		btnLogin.setVisible(true);
		btnHome.setVisible(true);
	}
	
	/**
	 * determines the visual elements for the register screen
	 * @param event button click caused by the register button of the home screen
	 */
	@FXML
	public void goToRegister(ActionEvent event) {
		
		btnHomeLogin.setVisible(false);
		btnHomeRegister.setVisible(false);
		lblInfo.setVisible(false);
		
		lblHeader.setText(I18N.tr("welcomescreen.register"));
		
		lblUsername.setVisible(true);
		lblPassword.setVisible(true);
		lblPassword2.setVisible(true);
		txtFieldUsername.setVisible(true);
		txtFieldPassword.setVisible(true);
		txtFieldPassword2.setVisible(true);
		btnRegister.setVisible(true);
		btnHome.setVisible(true);
	}
	
	/**
	 * determines the visual elements for the home screen
	 * @param event button click caused by the back button of the login/register screen
	 */
	@FXML
	public void back(ActionEvent event) {
				
		btnHomeLogin.setVisible(true);
		btnHomeRegister.setVisible(true);
		lblInfo.setVisible(true);
		lblHeader.setText(I18N.tr("welcomescreen.welcome"));
		lblInfo.setText(I18N.tr("welcomescreen.infotext"));
		
		btnHome.setVisible(false);
		lblUsername.setVisible(false);
		lblPassword.setVisible(false);
		lblPassword2.setVisible(false);
		txtFieldUsername.setVisible(false);
		txtFieldPassword.setVisible(false);
		txtFieldPassword2.setVisible(false);
		btnRegister.setVisible(false);
		btnLogin.setVisible(false);
	}
	
	/**
	 * Checks the login credentials with the server.
	 * Displays an error message if login not successful,
	 * else opens Ludo
	 * @param event button click caused login button of the login screen
	 */
	@FXML
	public void login(ActionEvent event) {
		Parent root;
		
		String usr = txtFieldUsername.getText();
		String pwd = txtFieldPassword.getText();
		
		if(usr.length() < 0 || usr.length() > 20 || pwd.length() < 0) {
			try {
				Socket socket = new Socket("localhost", 12345);
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream()));
				
				String hashedPwd = MD5Encrypt.cryptWithMD5(pwd);
				
				// sends login-request: LOGIN,1,usr,pwd
				bw.write("LOGIN,1," + usr + "," + hashedPwd);
				
				bw.close();
				
				BufferedReader br = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				
				
				// gets the true/false of: LOGIN,1,true/false
				String res = br.readLine();
				res = res.split(",")[2];
				
				if(res.toUpperCase() == "FALSE") {
					try {
			            root = FXMLLoader.load(getClass().getResource("Ludo.fxml"));
			            Stage stage = new Stage();
			            stage.setTitle("Ludo");
			            stage.setScene(new Scene(root, 1050, 800));
			            stage.show();
			            
			            // hiding the login window (effecevly: closing it)
			            ((Node)(event.getSource())).getScene().getWindow().hide();
			        }
			        catch (IOException e) {
			            e.printStackTrace();
			        }
					
				} else {
					lblError.setVisible(true);
					lblError.setText(I18N.tr("errors.failedToLogIn"));
				}
			}
			catch(IOException ioe) {
				
			}
		}
	}
	
	/**
	 * Registers a new users if inputed values are valid, and updates
	 * this information with server and database.
	 * Displays error message if not valid. 
	 * @param event button click caused register button of the register screen
	 */
	@FXML	
	public void register(ActionEvent event) {
		String usr, pwd, pwd2;
		usr = txtFieldUsername.getText();
		pwd = txtFieldPassword.getText();
		pwd2 = txtFieldPassword2.getText();
	
		if(usr.length() <= 0 || usr.length() > 20 || pwd.length() <= 0) {
			lblError.setVisible(true);
			// TODO i18n
			lblError.setText(I18N.tr("errors.notValidUserOrPassword"));
		}
		else if(pwd != pwd2) {
			lblError.setVisible(true);
			lblError.setText(I18N.tr("errors.equalPassword"));
		}
		else {
			lblError.setVisible(false);	// TODO sjekk mot server 
			lblInfo.setVisible(true);
			lblInfo.setText(I18N.tr("register.success"));
			txtFieldUsername.setText("");
		}
		txtFieldPassword.setText("");
		txtFieldPassword2.setText("");
	}

}
