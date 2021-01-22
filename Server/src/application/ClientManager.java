package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

import javafx.application.Platform;

public class ClientManager implements Runnable
{
	private ServerBackend serverBackend;
	public Socket clientSocket;
	private ServerAppController controller;
	private BufferedReader dataIn;
    private BufferedWriter dataOut;
    private String login;
	
	public ClientManager(ServerBackend serverBackend, Socket clientSocket, ServerAppController controller)
	{
		this.serverBackend = serverBackend;
		this.clientSocket = clientSocket;
		this.controller = controller;
	}
	
	private void initBuffers() throws IOException
	{
		dataOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        dataIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}
	
	public void closeConnection() throws IOException
	{
		if(dataIn != null) dataIn.close();
		if(dataOut != null) dataOut.close();
		clientSocket.close();
		Thread.currentThread().interrupt();
	}
	
	private boolean checkLogin() throws IOException
	{
		String name = dataIn.readLine();
		if(serverBackend.checkIfLoginExists(name) == true)
		{
			sendMessage("loginError");
			return false;
		}
		login = name;
		sendMessage("correctLogin");
		serverBackend.addClient(login, this);
		return true;
	}
	
	public void notifyUser(String hour, boolean status) //Method notifies user about status change of given hour
	{
		try
		{
			String message = status ? "book" : "cancel";
			sendMessage(message);
			sendMessage(hour);
		}
		catch(IOException e)
		{
			System.out.println("Failed to send message");
		}
		
	}
	
	private void sendSchedule() throws IOException //Method sends schedule to client
	{
		List<Visit> schedule = serverBackend.getSchedule();
		sendMessage("sendSchedule");
		for(Visit visit : schedule)
		{
			sendMessage(visit.getHour());
			sendMessage(visit.getBookedStatus());
		}
		sendMessage("end");
	}
	
	public void sendMessage(String text) throws IOException
	{
		dataOut.write(text + "\r\n");
        dataOut.flush();
	}
	
	private void handleCommands() throws IOException //Method handle command incomming from client
	{
		String text = "s";
        while((text = dataIn.readLine()) != null)
        {
            if(text.equals("book"))
            {
            	bookVisit();
            }
            if(text.equals("cancel"))
            {
            	cancelVisit();
            }
            if(text.equals("logout"))
            {
            	clientLogout();
            }
        }
        Platform.runLater(() -> controller.logAdd("Client disconnected"));
	}
	
	private void bookVisit() throws IOException
	{
		String hour = dataIn.readLine();
    	if(serverBackend.setBookedStatus(hour, true, login))
    	{
    		sendMessage("bookSuccess");
    		serverBackend.notifyUsers(hour, true);
    		Platform.runLater(() -> controller.logAdd("Client booked visit"));
    	}
    	else
    		sendMessage("bookFailed");
	}
	
	private void cancelVisit() throws IOException
	{
		String hour = dataIn.readLine();
    	if(serverBackend.setBookedStatus(hour, false, login))   
    	{
    		sendMessage("bookSuccess");
    		serverBackend.notifyUsers(hour, false);
    		Platform.runLater(() -> controller.logAdd("Client canceled visit"));
    	}
    	else
    		sendMessage("bookFailed");
	}
	
	private void clientLogout() throws IOException
	{
		String text = dataIn.readLine();
		serverBackend.deleteClient(text);
    	closeConnection();
	}
	
	@Override
	public void run()
	{
		try
		{
			initBuffers();
			if(checkLogin())
			{
				Platform.runLater(() -> controller.logAdd("New client connected"));
				sendSchedule();
				handleCommands();
			}
		}
		catch(IOException e)
		{
			System.out.println("Client disconnected");
		}
		finally
		{
			try
			{
				closeConnection();
			}
			catch(IOException e)
			{
				System.out.println("Error while closing connection");
				e.printStackTrace();
			}
		}
	}
}
