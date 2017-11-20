package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import no.ntnu.imt3281.ludo.server.ChatServer.Client;
import no.ntnu.imt3281.ludo.logic.Ludo;

public class GameServer {
	
	private ServerSocket sokk;
	private ExecutorService gameThread;
	
	

	public GameServer() {
		
		try {
			sokk = new ServerSocket(12345);
			gameThread = Executors.newCachedThreadPool();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		
		
		
		
		
	}
	
	 
	
	
	   
	 

	
	
}
