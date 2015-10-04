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
import bwapi.Race;
import bwapi.UnitType;
import bwapi.UpgradeType;


public class Mech extends BuildOrder
{
	boolean initialized = false;

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		DebugMessages.addMessage("Going mech.");
		
		if(!initialized)
		{
			bot.army.requiredSize = 20;
			bot.army.maximumSize = 40;
			bot.workersPerGas = 1;
			initialized = true;
		}
		
		if(bot.count(UnitType.Terran_Factory) >= 2)
			bot.workersPerGas = 2;
		else
			bot.workersPerGas = 1;
			
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= 
				(bot.count(UnitType.Terran_Barracks) + bot.ccCount + bot.count(UnitType.Terran_Factory)) * 3)
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if(bot.getAvailableMinerals()>= 150 && (bot.count(UnitType.Terran_Barracks) == 0)) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if(bot.count(UnitType.Terran_Refinery) < bot.count(UnitType.Terran_Command_Center) && bot.getAvailableMinerals()>= 100 && bot.geyserCount > 0 && bot.count(UnitType.Terran_Barracks) >= 1) 
		{
			bot.build(UnitType.Terran_Refinery);
		}

		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Barracks) >= 1 && bot.count(UnitType.Terran_Factory) < Math.min(5, 2*bot.count(UnitType.Terran_Command_Center)-1))
		{
			bot.build(UnitType.Terran_Factory);
		}
		
		if (bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) >= 1)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				int count = structures.getUnitCount(UnitType.Terran_Bunker);
				
				if(count < (game.enemy().getRace() == Race.Protoss?2:1))
				{
		  			bot.buildDefensive(UnitType.Terran_Bunker, structures);
		  			break;
				}
			}
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
		
		if (bot.getAvailableMinerals() >= 150 
				&& bot.count(UnitType.Terran_Barracks) != 0 && bot.count(UnitType.Terran_Refinery) != 0
				&& bot.count(UnitType.Terran_Academy) == 0
				&& bot.army.units.size() >= 10)
		{
			bot.build(UnitType.Terran_Academy);
		}
		
		if (bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode) >= 6
				&& bot.count(UnitType.Terran_Armory) < 2
				&& bot.getAvailableGas() >= 50
				&& bot.getAvailableMinerals() >= 100)
		{
			bot.build(UnitType.Terran_Armory);
		}
		
		if(bot.getAvailableMinerals()>= 125 && bot.count(UnitType.Terran_Engineering_Bay) < 1 && bot.count(UnitType.Terran_Vulture) >= 2) 
		{
			bot.build(UnitType.Terran_Engineering_Bay);
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
	}

	@Override
	public boolean overrideStructureOrder(Game game, Player self, Tyr bot, Agent agent)
	{
		if (agent.unit.getType() == UnitType.Terran_Factory)
		{
			if (bot.getAvailableMinerals() >= 100 && !agent.unit.isTraining() && 
					bot.count(UnitType.Terran_Vulture) <= 4 + 2*(bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode)))
			{
				agent.unit.train(UnitType.Terran_Vulture);
				return true;
			}
			else if(bot.count(UnitType.Terran_Armory) >= 1 && !agent.unit.isTraining()
				&& bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode) 
					>= 8 + bot.count(UnitType.Terran_Goliath) 
				&& bot.getAvailableMinerals() >= 100 && bot.getAvailableGas() >= 50)
				{
					agent.unit.train(UnitType.Terran_Goliath);
					return true;
				}
		}
		else if(agent.unit.getType() == UnitType.Terran_Armory)
		{
			if(bot.getAvailableMinerals() >= UpgradeType.Terran_Vehicle_Weapons.mineralPrice()
					&& bot.getAvailableGas() >= UpgradeType.Terran_Vehicle_Weapons.gasPrice())
				agent.unit.upgrade(UpgradeType.Terran_Vehicle_Weapons);
			
			if(bot.getAvailableMinerals() >= UpgradeType.Terran_Vehicle_Plating.mineralPrice()
					&& bot.getAvailableGas() >= UpgradeType.Terran_Vehicle_Plating.gasPrice())
				agent.unit.upgrade(UpgradeType.Terran_Vehicle_Plating);
			
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Engineering_Bay)
		{
			return true;
		}
		return false;
	}
}
