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
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;


public class GreedyBC extends BuildOrder
{
	boolean scoutRequested = false;
	
	boolean initialized = false;
	int dropshipCount = 0;

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		DebugMessages.addMessage("Going BattleCruiser.");
		
		if (!initialized)
		{
			initialized = true;
			bot.army.requiredSize = 40;
			bot.army.maximumSize = 60;
		}
		
		if (bot.scout.opponentStrategy == ScoutGroup.besiege)
		{
			bot.scout.scoutCount = 2;
		}
		
		if(game.getFrameCount() >= 1600 && !scoutRequested)
		{
			scoutRequested = true;
			bot.scout.requestWorkerScout(bot);
		}
		
		bot.defensiveTanks = 4;
		
		if (bot.getAvailableGas() >= 400)
			bot.workersPerGas = bot.count(UnitType.Terran_Factory) >= 1?2:1;
		else if (bot.getAvailableGas() <= 300)
			bot.workersPerGas = bot.count(UnitType.Terran_Factory) >= 1?3:2;
			
		else
		{
			bot.drawCircle(new Position(self.getStartLocation().getX()*32 + 64, self.getStartLocation().getY()*32 + 32), Color.White, 64);
		}
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks) *3 + bot.count(UnitType.Terran_Starport) *3 + bot.ccCount * 3)
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if(bot.getAvailableMinerals()>= 150
				&& bot.count(UnitType.Terran_Command_Center) >= 2
				&& (bot.count(UnitType.Terran_Barracks) < (game.enemy().getRace() == Race.Protoss?2:1))) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if(bot.geyserCount > 0 && bot.getAvailableMinerals()>= 100 
				&& (bot.count(UnitType.Terran_Refinery) == 0
				|| ( bot.count(UnitType.Terran_Refinery) < bot.count(UnitType.Terran_Command_Center) && bot.count(UnitType.Terran_Starport) > 0)) 
				&& bot.count(UnitType.Terran_Barracks) >= 1
				&& bot.count(UnitType.Terran_Bunker) >= (game.enemy().getRace() == Race.Protoss?2:1)) 
		{
			bot.build(UnitType.Terran_Refinery);
		}
		
		if(bot.getAvailableMinerals()>= 125 && bot.count(UnitType.Terran_Engineering_Bay) < 1 && bot.count(UnitType.Terran_Starport) >= 1) 
		{
			bot.build(UnitType.Terran_Engineering_Bay);
		}
		
		if (bot.getAvailableMinerals() >= 150 
				&& bot.count(UnitType.Terran_Barracks) != 0 && bot.count(UnitType.Terran_Refinery) != 0
				&& bot.count(UnitType.Terran_Academy) == 0
				&& bot.count(UnitType.Terran_Engineering_Bay) > 0
				&& bot.army.units.size() >= 10
				&& bot.count(UnitType.Terran_Command_Center) >= 2)
		{
			bot.build(UnitType.Terran_Academy);
		}
		
		int patchCount = 0;
		for(MineralWorkers base : bot.workForce.mineralWorkers)
			patchCount += base.minerals.size();
		
		boolean beingConstructed = false;
		for(BuildCommand command : bot.buildCommands)
			if (command.building == UnitType.Terran_Command_Center)
			{
				beingConstructed = true;
				break;
			}
		
		if (bot.getAvailableMinerals() >= 400 &&
				!beingConstructed &&
				(bot.count(UnitType.Terran_Command_Center) < 3 || patchCount * 2 + 4 < bot.workForce.units.size() + bot.builders.units.size()))
		{
			bot.build(UnitType.Terran_Command_Center);
		}
		
		if (bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) >= 1)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				if (structures.defendedPosition.getDistance(Tyr.tileToPostion(self.getStartLocation())) < 128
						&& bot.count(UnitType.Terran_Factory) == 0)
					continue;
					
				
				int count = structures.getUnitCount(UnitType.Terran_Bunker);
				
				if(count < (game.enemy().getRace() == Race.Protoss?2:1))
				{
		  			bot.buildDefensive(UnitType.Terran_Bunker, structures);
		  			break;
				}
			}
		}
		
		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Barracks) >= 1 && bot.count(UnitType.Terran_Factory) < 2)
		{
			bot.build(UnitType.Terran_Factory);
		}
		
		if (bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) +  bot.count(UnitType.Terran_Siege_Tank_Tank_Mode) >= 2
				&& bot.count(UnitType.Terran_Command_Center) >= 2
				&& bot.count(UnitType.Terran_Starport) < 3
				&& (bot.count(UnitType.Terran_Starport) < 1 || (bot.count(UnitType.Terran_Battlecruiser) >= 1 && bot.getAvailableGas() >= 400)))
		{
			bot.build(UnitType.Terran_Starport);
		}
		
		if (bot.count(UnitType.Terran_Starport) >= 1 && bot.count(UnitType.Terran_Science_Facility) == 0
				&& bot.getAvailableGas() >= 150 && bot.getAvailableMinerals() >= 100)
		{
			bot.build(UnitType.Terran_Science_Facility);
		}

		if(bot.count(UnitType.Terran_Engineering_Bay) >= 1 && bot.getAvailableMinerals() >= 75)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				int count = structures.getUnitCount(UnitType.Terran_Missile_Turret);
				
				if(count == 0 && structures.defenses.size() > 0)
				{
		  			bot.buildDefensive(UnitType.Terran_Missile_Turret, structures);
		  			break;
				}
			}
		}
		
		if (bot.count(UnitType.Terran_Battlecruiser) >= 3
				&& bot.count(UnitType.Terran_Armory) < 2
				&& bot.getAvailableGas() >= 50
				&& bot.getAvailableMinerals() >= 100)
		{
			bot.build(UnitType.Terran_Armory);
		}
	}
	
	@Override 
	public boolean overrideStructureOrder(Game game, Player self, Tyr bot, Agent agent)
	{
		if(agent.unit.getType() == UnitType.Terran_Factory)
		{
			int neededTanks = bot.army.mobileTankCount + 2*dropshipCount;
			neededTanks += bot.defensiveTanks * bot.defensiveStructures.size();
			
			if(agent.unit.getAddon() == null 
					&& bot.getAvailableMinerals() >= 50 && bot.getAvailableGas() >= 50)
				agent.unit.buildAddon(UnitType.Terran_Machine_Shop);
			else if(!agent.unit.isTraining() && bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
					&& bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode)
						< neededTanks
					&& (bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) == 0 || self.hasResearched(TechType.Tank_Siege_Mode)))
				agent.unit.train(UnitType.Terran_Siege_Tank_Tank_Mode);
			else if (!agent.unit.isTraining() && bot.getAvailableMinerals() >= 500 && bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode)
					>= neededTanks)
				agent.unit.train(UnitType.Terran_Vulture);
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Starport)
		{
			if (agent.unit.getAddon() == null 
					&& bot.getAvailableMinerals() >= 50 && bot.getAvailableGas() >= 50)
				agent.unit.buildAddon(UnitType.Terran_Control_Tower);
			else if (!agent.unit.isTraining() && bot.count(UnitType.Terran_Dropship) < dropshipCount
					&& bot.getAvailableMinerals() >= 100&& bot.getAvailableGas() >= 100
					&& agent.unit.getAddon() != null)
				agent.unit.train(UnitType.Terran_Dropship);
			else if(!agent.unit.isTraining() && bot.getAvailableMinerals() >= 400 && bot.getAvailableGas() >= 300 && bot.count(UnitType.Terran_Physics_Lab) >= 1)
				agent.unit.train(UnitType.Terran_Battlecruiser);
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Science_Facility)
		{
			if (!agent.unit.isResearching() && agent.unit.getAddon() == null && bot.getAvailableGas() >= 50 && bot.getAvailableMinerals() >= 50)
				agent.unit.buildAddon(UnitType.Terran_Physics_Lab);
		}
		else if (agent.unit.getType() == UnitType.Terran_Engineering_Bay)
		{
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Physics_Lab)
		{
			if(!agent.unit.isResearching() 
					&& ! agent.unit.isUpgrading()
					&& bot.getAvailableGas() >= 150 && bot.getAvailableMinerals() >= 150)
				agent.unit.upgrade(UpgradeType.Colossus_Reactor);
			else if(!agent.unit.isResearching() 
					&& ! agent.unit.isUpgrading()
					&& bot.getAvailableGas() >= 100 && bot.getAvailableMinerals() >= 100)
				agent.unit.research(TechType.Yamato_Gun);
			
			return true;
		}
		else if(agent.unit.getType() == UnitType.Terran_Armory)
		{
			if(bot.getAvailableMinerals() >= UpgradeType.Terran_Ship_Weapons.mineralPrice()
					&& bot.getAvailableGas() >= UpgradeType.Terran_Ship_Weapons.gasPrice())
				agent.unit.upgrade(UpgradeType.Terran_Ship_Weapons);
			
			if(bot.getAvailableMinerals() >= UpgradeType.Terran_Ship_Plating.mineralPrice()
					&& bot.getAvailableGas() >= UpgradeType.Terran_Ship_Plating.gasPrice())
				agent.unit.upgrade(UpgradeType.Terran_Ship_Plating);
			
			return true;
		}
		return false;
	}

}
