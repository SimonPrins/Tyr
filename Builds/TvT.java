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
import bwapi.TechType;
import bwapi.UnitType;


public class TvT extends BuildOrder
{
	boolean scoutRequested = false;
	boolean attacked;

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(game.getFrameCount() >= 1600 && !scoutRequested)
		{
			scoutRequested = true;
			bot.scout.requestWorkerScout(bot);
		}
		
		if(!attacked && bot.army.units.size() >= bot.army.requiredSize)
			attacked = true;
		
		bot.workersPerGas = 2;
		bot.army.mobileTankCount = 2;
		
		if(bot.scout.opponentStrategy == ScoutGroup.defensive)
			bot.army.maximumSize = 40;
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks)*3 + bot.ccCount * 3)
        		  && (bot.getAvailableMinerals() >= 100)
        		  && self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if (bot.getAvailableMinerals()>= 150 && (bot.count(UnitType.Terran_Barracks) < Math.min(bot.ccCount * 2, 10))) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if(bot.geyserCount > 0 && bot.getAvailableMinerals()>= 150 && bot.count(UnitType.Terran_Refinery) == 0 && bot.count(UnitType.Terran_Barracks) >= 4) 
		{
			bot.build(UnitType.Terran_Refinery);
		}
		
		if(bot.getAvailableMinerals()>= 125 && bot.count(UnitType.Terran_Refinery) > 0 && bot.count(UnitType.Terran_Engineering_Bay) < 1) 
		{
			bot.build(UnitType.Terran_Engineering_Bay);
		}
		
		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Barracks) >= 1 && bot.count(UnitType.Terran_Factory) < 1)
		{
			bot.build(UnitType.Terran_Factory);
		}
		
		if (bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Engineering_Bay) > 0
				&& bot.count(UnitType.Terran_Factory) > 0
				&& bot.count(UnitType.Terran_Starport) < 1
				&& bot.count(UnitType.Terran_Command_Center) >= 2)
		{
			bot.build(UnitType.Terran_Starport);
		}
		
		if (bot.getAvailableMinerals() >= 150 
				&& bot.count(UnitType.Terran_Barracks) != 0 && bot.count(UnitType.Terran_Refinery) != 0
				&& bot.count(UnitType.Terran_Academy) == 0)
		{
			bot.build(UnitType.Terran_Academy);
		}
		
		if (bot.getAvailableMinerals() >= 400)
		{
			bot.build(UnitType.Terran_Command_Center);
		}
		
		if (bot.getAvailableMinerals() >= 100 && attacked)
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
	
	@Override 
	public boolean overrideStructureOrder(Game game, Player self, Tyr bot, Agent agent)
	{
		if(agent.unit.getType() == UnitType.Terran_Factory)
		{
			int neededTanks = bot.army.mobileTankCount;
			for(DefensiveStructures defPos : bot.defensiveStructures)
				if (!defPos.tooFar)
					neededTanks += bot.defensiveTanks;
			
			if(agent.unit.getAddon() == null 
					&& bot.getAvailableMinerals() >= 50 && bot.getAvailableGas() >= 50)
				agent.unit.buildAddon(UnitType.Terran_Machine_Shop);
			else if(!agent.unit.isTraining() && bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
					&& bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode)
						< neededTanks
					&& (bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) == 0 || self.hasResearched(TechType.Tank_Siege_Mode)))
				agent.unit.train(UnitType.Terran_Siege_Tank_Tank_Mode);
			return true;
		}
		return false;
	}

}
