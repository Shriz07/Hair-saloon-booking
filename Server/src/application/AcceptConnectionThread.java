package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AcceptConnectionThread implements Runnable 
{
	private ServerBackend serverBackend;
	private ServerSocket serverSocket;
	private ServerAppController controller;
	private ExecutorService clientExecutor = Executors.newCachedThreadPool();
	
	public AcceptConnectionThread(ServerBackend serverBackend, ServerSocket serverSocket, ServerAppController controller)
	{
		this.serverBackend = serverBackend;
		this.serverSocket = serverSocket;
		this.controller = controller;
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				Socket clientSocket = serverSocket.accept();
				clientExecutor.execute(new ClientManager(serverBackend, clientSocket, controller));
			}
			catch(IOException e)
			{
				System.out.println("Server shutdown");
				try
				{
					clientExecutor.shutdownNow();
					clientExecutor.awaitTermination(1, TimeUnit.SECONDS);
				}
				catch(InterruptedException e2)
				{
					Thread.currentThread().interrupt();
				}
				break;
			}
		}
	}
}
