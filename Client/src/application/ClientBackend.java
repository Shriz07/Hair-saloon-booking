package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

public class ClientBackend 
{
	private String host;
	private String login;
	private int port;
	private Socket clientSocket;
	private ClientAppController controller;
	private BufferedReader dataIn;
    private BufferedWriter dataOut;
    private HandleCommands handleCommands;
    private List<Visit> schedule = new ArrayList<Visit>();
    
    public ClientBackend(String host, int port, ClientAppController controller, String login)
    {
    	this.host = host;
    	this.port = port;
    	this.controller = controller;
    	this.login = login;
    }
    
    public boolean startClient()
    {
    	try
    	{
    		createSocket();
    		initBuffers();
    		if(tryLogin())
    		{
	    		Platform.runLater(() -> controller.setData(this));
	    		Platform.runLater(() -> controller.logAdd("Connected to server"));
	    		Platform.runLater(() -> controller.refreshDisplayedVisits());
	    		handleCommands = new HandleCommands(dataIn, dataOut);
	    		handleCommands.start();
    		}
    		else
    		{
    			closeConnection();
    			return false;
    		}
    	}
    	catch(IOException e)
    	{
    		Platform.runLater(() -> controller.logAdd("Failed to connect to server"));
    		return false;
    	}
    	return true;
    }
    
    private boolean tryLogin() throws IOException
    {
    	sendMessage(login);
    	String check = dataIn.readLine();
    	if(check.equals("loginError"))
    		return false;
    	return true;
    }
    
    private void initBuffers() throws IOException
    {
        dataOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        dataIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
	
    public void createSocket() throws IOException
    {
    	clientSocket = new Socket(host, port);
    }
    
    public void sendMessage(String text) throws IOException
    {
    	dataOut.write(text + "\r\n");
    	dataOut.flush();
    }
    
    public void closeConnection()
    {
    	try
    	{
    		sendMessage("logout");
    		sendMessage(login);
    		if(handleCommands != null) handleCommands.interrupt();
    		if(dataIn != null) dataIn.close();
	    	if(dataOut != null) dataOut.close();
	    	if(clientSocket != null) clientSocket.close();
    	}
    	catch(IOException e)
    	{
    		System.out.println("Error occured while closing connection");
    	}
    }
    
    public List<Visit> getSchedule()
    {
    	return schedule;
    }
    
    public void bookVisit(Visit visit) throws IOException
    {
    	sendMessage("book");
    	sendMessage(visit.getHour());
    }
    
    public void cancelVisit(Visit visit) throws IOException
    {
    	sendMessage("cancel");
    	sendMessage(visit.getHour());
    }
    
    private class HandleCommands extends Thread
    {
    	private BufferedReader dataIn;
        private BufferedWriter dataOut;
        
        public HandleCommands(BufferedReader dataIn, BufferedWriter dataOut)
        {
        	this.dataIn = dataIn;
        	this.dataOut = dataOut;
        }
        
        public void run()
        {
        	try
        	{
	        	getSchedule();
	        	listen();
        	}
        	catch(IOException e)
        	{
        		closeConnection();
        	}
        }
        
        public void listen() throws IOException
        {
        	String message;
        	while((message = dataIn.readLine()) != null)
        	{
        		if(message.equals("book") || message.equals("cancel"))
        			bookOrCancelVisit(message);
        		else if(message.equals("bookSuccess"))
        			Platform.runLater(() -> controller.logAdd("Hour was booked"));
        		else if(message.equals("bookFailed"))
        			Platform.runLater(() -> controller.logAdd("Failed to book visit"));
        		else if(message.equals("close"))
        		{
        			Platform.runLater(() -> controller.logAdd("Server shutdown"));
        			closeConnection();
        			break;
        		}
        		Platform.runLater(() -> controller.refreshDisplayedVisits());
        	}
        }
        
        public void bookOrCancelVisit(String message) throws IOException
        {
        	String hour = dataIn.readLine();
			String status = message.equals("book") ? "Yes" : "No";
			for(int i = 0; i < schedule.size(); i++)
			{
				if(hour.equals(schedule.get(i).getHour()))
					schedule.set(i, new Visit(hour, status));
			}
        }
        
        public void getSchedule() throws IOException
        {
        	if(dataIn.readLine().equals("sendSchedule"))
        	{
        		while(true)
        		{
	        		String hour = dataIn.readLine();
	        		if(hour.equals("end")) break;
	        		String booked = dataIn.readLine();
	        		schedule.add(new Visit(hour, booked));
        		}
        	}
        }
    }
}
