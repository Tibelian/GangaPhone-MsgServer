package com.tibelian.gangaphone.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import com.tibelian.gangaphone.server.RunServer;

public class ClientManager extends Thread {

	private boolean quit = false;
	private int userId;
	private Socket client;
    
    public ClientManager(Socket client) {
    	this.client = client;
    }
    

    @Override
    public void run() 
    {
    	// first ask for the user's id
    	write("identity");
		execute(read());

    	// append client to the user's list
 	   	Container.clients.add(this);
    	System.out.println("ClientManager@run() --> adding client '"+userId+"' to the Container");
 	   
    	// now wait for client to answer or request anything
    	while(client != null && quit == false)
    		execute(read());
		   
    	// close connection
    	System.out.println("ClientManager@run() --> closing connection with the client " + userId);
    	Container.clients.remove(this);
    	try { client.close(); }
    	catch(IOException e) {}
    }
    
    public String read() {
        try {
        	System.out.println("ClientManager@read() --> waiting for a message from the client");
            DataInputStream msgFromClient = new DataInputStream(client.getInputStream());
            String msg = msgFromClient.readUTF();
        	System.out.println("ClientManager@read() --> received message: \n--\n" + msg + "\n--");
            return msg;
        }
        catch (EOFException eo) {
        	System.err.println("ClientManager@read() EOFException --> " + eo);
        	quit = true; // close connection with the client
        }
        catch (IOException e) {
        	System.err.println("ClientManager@read() IOException --> " + e);
        }
        return null;
    }

    public void write(String msg) {
        try {
        	System.out.println("ClientManager@write() --> writing a message to the client '"+userId+"': \n--\n" + msg + "\n--");
            DataOutputStream msgToServer = new DataOutputStream(client.getOutputStream());
            msgToServer.writeUTF(msg);
        }
        catch (IOException e) {
        	System.err.println("ClientManager@write() error --> " + e);
        }
    }

    private void execute(String message) {
    	if (message == null) return;
    	System.out.println("ClientManager@execute() --> executing received command");
    	
    	try {
    		// parse message
	    	// result[0] == operation
	    	// result[1] == args
	    	String[] result = message.split("\n");
	    	
	    	// check the command
	    	switch(result[0]) {
	    	
	    		// user is saying his ID
		    	case "identity":
		    		userId = Integer.parseInt(result[1]);
		    		break;
		    	
	    		// the user wants to know if these ids are connected
		    	case "is_online":
		    		ArrayList<Integer> otherIds = new ArrayList<>();
		    		String[] idsStr = result[1].trim().split(",");
		    		for(String id:idsStr) otherIds.add(Integer.parseInt(id));
		    		checkWhoIsConnected(otherIds);
		    		break;
		    		
	    		// user's notifying a new message
		    	case "new_message":
		    		notifyClientNewMessage(Integer.parseInt(result[1]));
		    		break;
		    		
	    		// user's closing the connection
		    	case "quit":
		    		quit = true;
		    		break;
		    		
	    		// power off
		    	case "shutdown":
		    		RunServer.stayOnline = false;
		    		break;
		    	
		    	default:
		        	System.out.println("ClientManager@execute() --> unknonwn command: \n--\n" + message + "\n--");
		    		break;
	    	}
    	}
    	catch(NumberFormatException ne) {
        	System.err.println("ClientManager@execute() --> NumberFormatException: " + ne);
    	}
    }
    
    public int getUserId() {
    	return userId;
    }
    
    
    
    
    
   private void checkWhoIsConnected(ArrayList<Integer> ids) {
	   System.out.println("ClientManager@checkWhoIsConnected() num ids received --> " + ids.size());
	   String connList = ""; int i = 0;
	   for(int id:ids)
		   if (Container.isOnline(id)) {
			   if (i > 0) connList += ",";  
			   connList += id+"";
			   i++;
		   }
	   write("is_online\n"+connList+"");
   }
 
   
   private void notifyClientNewMessage(int targetId) {
	   for(ClientManager c:Container.clients) {
		   if (c.getUserId() == targetId) {
			   c.write("new_message");
			   return;
		   }
	   }
   }
    
}
