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
 *
 *
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
	
	
	@FXML
	public void goToLogin(ActionEvent event) {
		btnHomeLogin.setVisible(false);
		btnHomeRegister.setVisible(false);
		lblInfo.setVisible(false);
		lblHeader.setText("Logg inn her");
		lblUsername.setVisible(true);
		lblPassword.setVisible(true);
		txtFieldUsername.setVisible(true);
		txtFieldPassword.setVisible(true);
		btnLogin.setVisible(true);
		btnHome.setVisible(true);
	}
	
	@FXML
	public void goToRegister(ActionEvent event) {
		
		btnHomeLogin.setVisible(false);
		btnHomeRegister.setVisible(false);
		lblInfo.setVisible(false);
		lblHeader.setText("Registrer her");
		lblUsername.setVisible(true);
		lblPassword.setVisible(true);
		lblPassword2.setVisible(true);
		txtFieldUsername.setVisible(true);
		txtFieldPassword.setVisible(true);
		txtFieldPassword2.setVisible(true);
		btnRegister.setVisible(true);
		btnHome.setVisible(true);
	}
	
	@FXML
	public void back(ActionEvent event) {
				
		btnHomeLogin.setVisible(true);
		btnHomeRegister.setVisible(true);
		lblInfo.setVisible(true);
		lblHeader.setText("Velkommen til Ludo!");
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
				
				bw.write("LOGIN,1," + usr + "," + hashedPwd);
				
				bw.close();
				
				BufferedReader br = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				
				
				String res = br.readLine();
				
				if(res == "true") {
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
	
	@FXML	
	public void register(ActionEvent event) {
		String usr, pwd, pwd2;
		usr = txtFieldUsername.getText();
		pwd = txtFieldPassword.getText();
		pwd2 = txtFieldPassword2.getText();
	
		if(usr.length() <= 0 || usr.length() > 20 || pwd.length() <= 0) {
			lblError.setVisible(true);
			// TODO i18n
			lblError.setText("Ugyldig passord eller brukernavn. Brukernavn og passord må inneholde en tekst");
		}
		else if(pwd != pwd2) {
			lblError.setVisible(true);
			lblError.setText("Begge passordfelt må være like");
		}
		else {
			lblError.setVisible(false);	// TODO sjekk mot server 
			lblInfo.setVisible(true);
			lblInfo.setText("Bruker er registrert! Trykk tilbake for å logge inn.");
		}
		txtFieldPassword.setText("");
		txtFieldPassword2.setText("");
	}

}
