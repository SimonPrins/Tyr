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
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;


public class BBS extends BuildOrder
{
	boolean scoutRequested = false;
	boolean cannonBuildDetected = false;
	boolean zealotRushDetected = false;
	private MassTank massTank = new MassTank();
	private TvP tvp = new TvP();

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		bot.army.rallyPoint = new Position(game.mapWidth()*16, game.mapHeight()*16);
		bot.builders.clearEmptyMineralPatches = false;
		
		if(bot.count(UnitType.Terran_Marine) > 0 && !scoutRequested)
		{
			scoutRequested = true;
			tvp.scoutRequested = true;
			for (int i=0; i<(bot.suspectedEnemy.size() >= 3?2:1); i++)
				bot.scout.requestWorkerScout(bot);
		}
		
		if (!zealotRushDetected)
		{
			if (bot.enemyDefensiveStructures.size() > 0)
				cannonBuildDetected = true;
			
			if (cannonBuildDetected)
			{
				if (bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode) < 5)
					massTank.onFrame(game, self, bot);
				else
					tvp.onFrame(game, self, bot);
					
				return;
			}
		}
		
		if (!cannonBuildDetected)
		{
			int gatewayCount = 0;
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
				if (enemy.type == UnitType.Protoss_Gateway)
					gatewayCount++;
			if(gatewayCount >= 2)
			{
				if (bot.scout.opponentStrategy == ScoutGroup.zealotPush)
					bot.army.requiredSize = 40;
				bot.army.maximumSize = 60;
				
				zealotRushDetected = true;
			}
			
			if (!zealotRushDetected)
			{
				int zealots = 0;
				for(Unit enemy : game.enemy().getUnits())
					if (enemy.getType() == UnitType.Protoss_Zealot)
						zealots++;
				
				if (zealots >= 5)
				{
					bot.army.requiredSize = 40;
					bot.army.maximumSize = 60;
					
					zealotRushDetected = true;
				}
			}
			
			if (zealotRushDetected)
			{
				if (bot.count(UnitType.Terran_Bunker) == 0 && bot.count(UnitType.Terran_Barracks) > 0)
				{
					if (bot.getAvailableMinerals() >= 100 && bot.defensiveStructures.size() >= 0)
			  			bot.buildDefensive(UnitType.Terran_Bunker, bot.defensiveStructures.get(0));
					
					return;
				}
				
				tvp.onFrame(game, self, bot);
				return;
			}
		}
		
		bot.army.requiredSize = 5;
		bot.army.maximumSize = 5;
		

		if (bot.suspectedEnemy.size() == 1 && bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Marine) >= 8 && bot.count(UnitType.Terran_Bunker) < 1)
		{
			Position enemyPos = bot.suspectedEnemy.get(0).getPosition();
			Position expo = null;
			int distance = Integer.MAX_VALUE;
			for(BaseLocation loc : bot.expands)
			{
				int newDist = (int)enemyPos.getDistance(loc.getPosition());
				if(newDist < distance)
				{
					distance = newDist;
					expo = loc.getPosition();
				}
			
			}
			if(expo != null)
				bot.build(UnitType.Terran_Bunker, expo);
		}
		
		//Build two barracks as soon as we have 300 minerals.
		if(bot.getAvailableMinerals()>= 150 && bot.count(UnitType.Terran_Barracks) < 2) 
		{
			for (int i = bot.count(UnitType.Terran_Barracks); i < 2; i++)
				bot.build(UnitType.Terran_Barracks, new Position(game.mapWidth()*16, game.mapHeight()*16));
		}
		
		if (bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) == 2 && bot.count(UnitType.Terran_Supply_Depot) == 0)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
		if (bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) == 2 && bot.count(UnitType.Terran_Supply_Depot) > 0
				&& (self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= 4)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
	}

	@Override 
	public boolean overrideStructureOrder(Game game, Player self, Tyr bot, Agent agent)
	{
		if (cannonBuildDetected)
		{
			if (agent.unit.getType() == UnitType.Terran_Barracks)
			{
				if (bot.count(UnitType.Terran_Machine_Shop) == 0 && bot.getAvailableMinerals() <= 250)
					return true;
			}
			return massTank.overrideStructureOrder(game, self, bot, agent);
		}
		
		if (zealotRushDetected)
		{
			if (bot.count(UnitType.Terran_Bunker) == 0)
				return true;
			
			return tvp.overrideStructureOrder(game, self, bot, agent);
		}
		
		if(agent.unit.getType() == UnitType.Terran_Command_Center)
		{
			if ((bot.count(UnitType.Terran_Marine) == 0 || bot.getAvailableMinerals() >= 250) && !agent.unit.isTraining() && bot.getAvailableMinerals() >= 50)
			{
				if (bot.count(UnitType.Terran_SCV) < 8 )
				{
					agent.unit.train(UnitType.Terran_SCV);
				}
				else if (bot.count(UnitType.Terran_Barracks) >= 2 && bot.count(UnitType.Terran_SCV) < 9)
				{
					agent.unit.train(UnitType.Terran_SCV);
				}
				else if (bot.count(UnitType.Terran_Supply_Depot) >= 1)
				{
					agent.unit.train(UnitType.Terran_SCV);
				}
			}
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Barracks)
		{
			if (!agent.unit.isTraining() && bot.getAvailableMinerals() >= 50)
				agent.unit.train(UnitType.Terran_Marine);
			return true;
		}
		return false;
	}
}
