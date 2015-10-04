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

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.UnitType;


public class DefensiveBC extends BuildOrder
{
	GreedyBC greedy = new GreedyBC();

	boolean scoutRequested = false;
	int desiredBunkers = 1;
	boolean zealots = false;
	
	public DefensiveBC()
	{
		greedy.scoutRequested = true;
	}
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(game.getFrameCount() >= 1600 && !scoutRequested)
		{
			scoutRequested = true;
			bot.scout.requestWorkerScout(bot);
		}
		
		if(bot.count(UnitType.Terran_Bunker) >= desiredBunkers
				&& bot.count(UnitType.Terran_Factory) >= 1)
			greedy.onFrame(game, self, bot);
		
		bot.workersPerGas = 3;
		
		if(!zealots && bot.scout.opponentStrategy == ScoutGroup.zealotPush)
		{
			zealots = true;
			desiredBunkers = 2;
		}

		if(zealots)
			bot.drawCircle(Tyr.tileToPostion(self.getStartLocation()), Color.Red);
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks) *3 + bot.count(UnitType.Terran_Starport) *3 + bot.ccCount * 3)
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if(bot.getAvailableMinerals()>= 150
				&& (bot.count(UnitType.Terran_Barracks) < desiredBunkers)) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if(bot.geyserCount > 0 && bot.getAvailableMinerals()>= 100 
				&& bot.count(UnitType.Terran_Refinery) == 0 
				&& bot.count(UnitType.Terran_Barracks) >= 1
				&& bot.count(UnitType.Terran_Bunker) >= desiredBunkers) 
		{
			bot.build(UnitType.Terran_Refinery);
		}
		
		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Barracks) >= 1 && bot.count(UnitType.Terran_Factory) < 1)
		{
			bot.build(UnitType.Terran_Factory);
		}
		
		if (bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) >= 1)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				int count = structures.getUnitCount(UnitType.Terran_Bunker);
				
				if(count < (!structures.tooFar?(desiredBunkers):0))
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
		return greedy.overrideStructureOrder(game, self, bot, agent);
	}
}
