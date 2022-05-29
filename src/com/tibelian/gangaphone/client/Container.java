package com.tibelian.gangaphone.client;

import java.util.ArrayList;

public class Container {
	
	public static ArrayList<ClientManager> clients = new ArrayList<>();
	
	public static boolean isOnline(int userId) {
		for(ClientManager c:clients)
			if (c.getUserId() == userId) {
				System.out.println("Container@isOnline() userId '" + userId + "' is connected!");
				return true;
			}
		return false;
	}
	
}
