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

import java.util.ArrayList;

import bwapi.Player;
import bwta.BWTA;
import bwta.BaseLocation;


public class BWTAInitializer implements Runnable
{
	public static boolean initialized = false;
	Tyr bot;
	Player self;
	
	public BWTAInitializer(Tyr bot, Player self)
	{
		this.bot = bot;
		this.self = self;
	}

	@Override
	public void run()
	{
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
		try
		{
			bot.expands = new ArrayList<BaseLocation>();
			bot.suspectedEnemy = new ArrayList<BaseLocation>();
			
	        System.out.println("Analyzing map...");
	        BWTA.readMap();
	        BWTA.analyze();
	        System.out.println("Map data ready");
	        
	        for(BaseLocation bloc : BWTA.getBaseLocations())
	        {
	        	if(!bloc.isStartLocation() || self.getStartLocation().getDistance(bloc.getTilePosition()) < 4)
	        		bot.expands.add(bloc);
	        	else
	        		bot.suspectedEnemy.add(bloc);
	        }
	        
	        initialized = true;
		}
		catch(Exception e)
		{
			System.out.println("Error intializing BWTA: " + e.getMessage());
			Tyr.game.printf("Error intializing BWTA: " + e.getMessage());
		}
	}

}
