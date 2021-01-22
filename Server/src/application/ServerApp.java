package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class ServerApp extends Application 
{
	@Override
	public void start(Stage stage) throws IOException
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("ServerView.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);
		
		ServerAppController controller = loader.getController();
		ServerBackend serverBackend = new ServerBackend(158, controller);
		serverBackend.startServer();
		
		stage.setOnCloseRequest((WindowEvent event) -> serverBackend.shutDown());
		stage.setScene(scene);
		stage.setTitle("Server");
		stage.show();
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
}
