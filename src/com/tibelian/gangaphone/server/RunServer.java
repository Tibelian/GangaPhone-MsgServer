package com.tibelian.gangaphone.server;

import java.io.IOException;
import java.net.ServerSocket;

import com.tibelian.gangaphone.client.ClientManager;
import com.tibelian.gangaphone.client.Container;

public class RunServer {
	
	public static boolean stayOnline = true;

	public static void main(String[] args) 
	{
		try {
			// start server
	        ServerSocket server = new ServerSocket(ServerConfig.PORT);
	        System.out.println("RunServer@main --> server started");
			
	        
	        // wait for clients and open connection with them
			while(stayOnline)
				if (Container.clients.size() < ServerConfig.MAX_CLIENTS)
					new ClientManager(server.accept()).start();
					
			
			// if the loop end's then shutdown the server
			server.close();
	        System.out.println("RunServer@main --> server stopped");
		}
		catch(IOException e) {
			System.err.println("RunServer@main() error --> " + e);
		}
	}

}
