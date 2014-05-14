package de.mazenet.client;

import generated.*;

public class MazeClient 
{
	// Member
	//MazeNet
	private static MoveMessageType lastMove;
	
	//Netzwerk
	private static MazeNetworkConnector connection;
	
	/**
	 * @param args
	 * Konstruktor
	 */
	public static void main(String[] args) 
	{
		connection = new MazeNetworkConnector();
		
		// Ki anlegen
		KiPlayer player = new BasicKi();
		if(connection.open())
		{
			
		}

	}

}
