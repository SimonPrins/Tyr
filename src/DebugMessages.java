/*
* Tyr is an AI for StarCraft: Broodwar, 
* 
* Please visit https://github.com/SimonPrins/Tyr for further information.
* 
* Copyright 2015 Simon Prins
*
* This file is part of Tyr.
* Tyr is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
* Tyr is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* You should have received a copy of the GNU General Public License
* along with Tyr.  If not, see http://www.gnu.org/licenses/.
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import bwapi.Game;


public class DebugMessages
{
	private static ArrayList<String> messages = new ArrayList<String>();
	private static ArrayList<String> permanentMessages = new ArrayList<String>();
	private static File file;
	
	public static void addMessage(String message)
	{
		messages.add(message);
	}
	
	public static void toScreen(Game game)
	{
		for(int i=0; i<permanentMessages.size(); i++)
	       	game.drawTextScreen(10, 85 + i*15, permanentMessages.get(i));
		
		for(int i=0; i<messages.size(); i++)
	       	game.drawTextScreen(10, 85 + (i + permanentMessages.size())*15, messages.get(i));
		
		messages = new ArrayList<String>();
	}
	
	private static void createFile()
	{
		try
		{
			file = new File("bwapi-data\\write\\" + Tyr.game.enemy().getRace() + " - " + Tyr.game.enemy().getName() + ".txt");
			try {
				if (!file.exists())
					file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch(Exception e){}
	}
	
	public static ArrayList<String> readFile()
	{
		try
		{
			File file = new File("bwapi-data\\read\\" + Tyr.game.enemy().getRace() + " - " + Tyr.game.enemy().getName() + ".txt");
			
			if (!file.exists())
				return new ArrayList<String>();
			
			try 
			{
				FileReader reader = new FileReader(file);
				BufferedReader br = new BufferedReader(reader);
				ArrayList<String> result = new ArrayList<String>();
				
				for (String nextLine = br.readLine(); nextLine != null; nextLine = br.readLine())
					result.add(nextLine);
				br.close();
				return result;
				
			} catch (FileNotFoundException e)
			{
				return new ArrayList<String>();
			} catch (IOException e) {
				return new ArrayList<String>();
			}
		}
		catch(Exception e){return new ArrayList<String>();}
	}
	
	public static void saveMessage(String message)
	{
		try
		{
			if (file == null)
				createFile();
			
			try
			{
				FileWriter writer = new FileWriter(file, true);
				writer.append(message + "\r\n");
				writer.close();
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		} catch(Exception e){}
	}

	public static void addMessagePermanent(String message) 
	{
		permanentMessages.add(message);
		
	}
}
