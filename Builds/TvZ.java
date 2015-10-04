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

import bwapi.Game;
import bwapi.Player;
import bwapi.UnitType;


public class TvZ extends BuildOrder
{
	boolean scoutRequested = false;
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(game.getFrameCount() >= 1600 && !scoutRequested)
		{
			scoutRequested = true;
			bot.scout.requestWorkerScout(bot);
		}
		
		if(bot.bunkers.minimumRepair == 1)
			bot.bunkers.minimumRepair = 2;
		
		if (bot.bunkers.bunkers.size() == 1 && bot.army.units.size() >= 11 && bot.army.units.size() < 15 && !bot.bunkers.disabled)
			bot.bunkers.disabled = true;
		
		if ((bot.army.units.size() < 11 || bot.army.units.size() >= 19) && bot.bunkers.disabled)
			bot.bunkers.disabled = false;
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks)*3 + bot.ccCount * 3)
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400
				&& bot.count(UnitType.Terran_Barracks) >= 1
				)
        	  bot.build(UnitType.Terran_Supply_Depot);
		
        //if we've the resources to build a barracks ...
        if (((bot.getAvailableMinerals()>= 150 
        		&& (bot.army.units.size() > 0 || bot.count(UnitType.Terran_Barracks) == 0  || bot.getAvailableMinerals()>= 200)))
        		&& (bot.count(UnitType.Terran_Barracks) < Math.min(bot.ccCount * 2 + 1, 10))) 
        	bot.build(UnitType.Terran_Barracks);
        
        if(bot.geyserCount > 0 && bot.getAvailableMinerals()>= 100 && bot.count(UnitType.Terran_Refinery) == 0
        		&& bot.count(UnitType.Terran_Barracks) == 4) 
        {
      	  bot.build(UnitType.Terran_Refinery);
        }
        
        if(bot.getAvailableMinerals()>= 125 && bot.count(UnitType.Terran_Refinery) > 0
        		&& bot.count(UnitType.Terran_Engineering_Bay) < 1) 
        {
      	  bot.build(UnitType.Terran_Engineering_Bay);
        }
          
        if (bot.getAvailableMinerals() >= 400)
        	bot.build(UnitType.Terran_Command_Center);

		
		if (bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) >= 1)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				int count = structures.getUnitCount(UnitType.Terran_Bunker);
				
				if(count < 1)
				{
		  			bot.buildDefensive(UnitType.Terran_Bunker, structures);
		  			break;
				}
			}
		}
	}
}
