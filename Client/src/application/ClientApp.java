package application;
	
import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;


public class ClientApp extends Application 
{
	@Override
	public void start(Stage stage) throws IOException
	{
		String login = getLogin();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("ClientView.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);
		
		ClientAppController controller = loader.getController();
		ClientBackend clientBackend = new ClientBackend("localhost", 158, controller, login);
		if(!clientBackend.startClient())
			alertBox();
		else
		{
			stage.setOnCloseRequest((WindowEvent event) -> clientBackend.closeConnection());
			stage.setScene(scene);
			stage.setTitle("Client - " + login);
			stage.show();
		}
	}
	
	private String getLogin()
	{
		TextInputDialog LoginWindow = new TextInputDialog("Name");

        LoginWindow.setHeaderText("Enter your name: ");
        LoginWindow.setContentText("Name");

        Optional<String> text = LoginWindow.showAndWait();
        if(text.isPresent())
            return text.get();
        else
            return null;
	}
	
	private void alertBox()
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.getDialogPane().setMinWidth(150);
		alert.getDialogPane().setContent(new Text("Name already exists"));
		alert.showAndWait();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
