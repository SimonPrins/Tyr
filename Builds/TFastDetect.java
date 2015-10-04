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


public class TFastDetect extends BuildOrder
{
	BuildOrder contained;
	public TFastDetect(BuildOrder contained)
	{
		this.contained = contained;
	}
	
	boolean scoutRequested = false;

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		bot.defensiveTanks = 4;
		bot.army.dropHarass.add(new DropHarass(bot.hobos));
		
		if(!scoutRequested && game.getFrameCount() >= 800)
		{
			bot.scout.requestWorkerScout(bot);
			scoutRequested = true;
		}
		
		boolean coreExists = false;
		for(EnemyPosition building : bot.enemyBuildingMemory)
		{
			if (building.type == UnitType.Protoss_Cybernetics_Core)
			{
				coreExists = true;
				break;
			}
		}
		
		if(coreExists && bot.getAvailableMinerals()>= 125 && bot.count(UnitType.Terran_Engineering_Bay) < 1 && bot.count(UnitType.Terran_Bunker) >= 1) 
		{
			bot.build(UnitType.Terran_Engineering_Bay);
		}
		
		if (bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Command_Center) >= 2 && bot.count(UnitType.Terran_Factory) >= 1 && bot.count(UnitType.Terran_Starport) == 0)
		{
			bot.build(UnitType.Terran_Starport);
		}
		
		if (coreExists && bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) >= 1)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				if (!(structures.defendedPosition.getDistance(Tyr.tileToPostion(self.getStartLocation())) < 128
						&& bot.count(UnitType.Terran_Factory) == 0))
					continue;
					
				
				int count = structures.getUnitCount(UnitType.Terran_Bunker);
				
				if(count < (game.enemy().getRace() == Race.Protoss?2:1))
				{
		  			bot.buildDefensive(UnitType.Terran_Bunker, structures);
		  			break;
				}
			}
		}

		if(bot.count(UnitType.Terran_Engineering_Bay) >= 1 && bot.getAvailableMinerals() >= 75)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				int count = structures.getUnitCount(UnitType.Terran_Missile_Turret);
				
				if(count < 1 && structures.defenses.size() > 0)
				{
		  			bot.buildDefensive(UnitType.Terran_Missile_Turret, structures);
		  			break;
				}
			}
		}
		
		if (bot.getAvailableMinerals() >= 150 
				&& bot.count(UnitType.Terran_Barracks) != 0 && bot.count(UnitType.Terran_Refinery) != 0
				&& bot.count(UnitType.Terran_Academy) == 0)
		{
			bot.build(UnitType.Terran_Academy);
		}
		
		contained.onFrame(game, self, bot);
	}

	@Override
	public boolean overrideStructureOrder(Game game, Player self, Tyr bot, Agent agent)
	{
		if (agent.unit.getType() == UnitType.Terran_Starport)
		{
			if (agent.unit.getAddon() == null 
					&& bot.getAvailableMinerals() >= 50 && bot.getAvailableGas() >= 50)
				agent.unit.buildAddon(UnitType.Terran_Control_Tower);
			else
			if (bot.getAvailableMinerals() >= 100 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Dropship) < bot.army.dropHarass.size())
			{
				agent.unit.train(UnitType.Terran_Dropship);
			}
			return true;
		}
			if (agent.unit.getType() == UnitType.Terran_Factory)
		{
			if (bot.getAvailableMinerals() >= 100 && !agent.unit.isTraining() && 
					bot.count(UnitType.Terran_Vulture) <= 2*(bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode)))
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
		
		return contained.overrideStructureOrder(game, self, bot, agent);
	}
}
