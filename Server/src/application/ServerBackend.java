package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.application.Platform;

public class ServerBackend 
{
	private int port;
	private ServerSocket serverSocket;
	private Thread acceptClientsThread;
	private ServerAppController controller;
	private List<Visit> schedule = new ArrayList<Visit>();
	private Map<String, ClientManager> clients = new HashMap<>();
	
	public ServerBackend(int port, ServerAppController controller)
	{
		this.port = port;
		this.controller = controller;
	}
	
	public void startServer()
	{
		try
		{
			createSocket();
			setSchedule();
			enableConnection();
			Platform.runLater(() -> controller.setData(this));
			Platform.runLater(() -> controller.refreshDisplayedSchedule());
			Platform.runLater(() -> controller.logAdd("Server started"));
		}
		catch(IOException e)
		{
			Platform.runLater(() -> controller.logAdd("Failed to start server"));
		}
	}
	
	public void createSocket() throws IOException
	{
		this.serverSocket = new ServerSocket(port);
	}
	
	public void enableConnection()
	{
		acceptClientsThread = new Thread(new AcceptConnectionThread(this, serverSocket, controller));
		acceptClientsThread.start();
	}
	
	public void shutDown() //Method informs all clients that server is closing and closes server
	{
		try
		{
			for(Map.Entry<String, ClientManager> entry : clients.entrySet())
			{
				entry.getValue().sendMessage("close");
				entry.getValue().closeConnection();
			}
			serverSocket.close();
			acceptClientsThread.interrupt();
		}
		catch(IOException e)
		{
			System.out.println("Error occured while closing socket");
		}
	}
	
	public boolean checkIfLoginExists(String login) //Method checks if given login already exists 
	{
		for(Map.Entry<String, ClientManager> entry : clients.entrySet())
		{
			if(entry.getKey().equals(login))
				return true;
		}
		return false;
	}
	
	public void addClient(String login, ClientManager client)
	{
		clients.put(login, client);
	}
	
	public void deleteClient(String login)
	{
		clients.remove(login);
	}
	
	public void notifyUsers(String hour, boolean status) //Method notifies clients about visit book status changed at the given hour
	{
		for(Map.Entry<String, ClientManager> entry : clients.entrySet())
		{
			ClientManager client = entry.getValue();
			client.notifyUser(hour, status);
		}
	}
	
	public List<Visit> getSchedule()
	{
		return schedule;
	}
	
	public int findVisitIndex(String hour, String booked, String name)
	{
		for(int i = 0; i < schedule.size(); i++)
		{
			if(hour.equals(schedule.get(i).getHour()))
			{
				if(schedule.get(i).getBookedStatus().equals(booked))
					return -1;
				if(schedule.get(i).getBookedStatus().equals("No"))
					return i;
				if(schedule.get(i).getName().equals(name))
					return i;
			}
		}
		return -1;
	}
	
	public boolean setBookedStatus(String hour, boolean status, String name) //Method changes book status of visit at the given hour
	{
		String booked = status ? "Yes" : "No";
		String newName = status ? name : null;
		int index = findVisitIndex(hour, booked, name);
		if(index != -1)
		{
			schedule.set(index, new Visit(hour, booked, newName));
			Platform.runLater(() -> controller.refreshDisplayedSchedule());
			return true;
		}
		return false;
	}
	
	private void setSchedule()
	{
		schedule.add(new Visit("10:00", "No", null));
		schedule.add(new Visit("11:00", "No", null));
		schedule.add(new Visit("12:00", "No", null));
		schedule.add(new Visit("13:00", "No", null));
		schedule.add(new Visit("14:00", "No", null));
		schedule.add(new Visit("15:00", "No", null));
		schedule.add(new Visit("16:00", "No", null));
		schedule.add(new Visit("17:00", "No", null));
		schedule.add(new Visit("18:00", "No", null));
	}
}