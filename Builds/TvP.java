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
import bwapi.TechType;
import bwapi.UnitType;


public class TvP extends BuildOrder
{
	boolean cannonBuildDetected = false;
	boolean scoutRequested = false;
	boolean antiTechStarted;
	MassTank massTank = new MassTank();
	boolean cannonRush = false;

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		DebugMessages.addMessage("Going bio.");
		
		if(game.getFrameCount() >= 1600 && !scoutRequested)
		{
			scoutRequested = true;
			bot.scout.requestWorkerScout(bot);
		}
		
		if(!cannonRush && bot.invader != null &&
				(bot.invader.getType() == UnitType.Protoss_Photon_Cannon || bot.invader.getType() == UnitType.Protoss_Pylon))
		{
			cannonRush = true;
			bot.army.initializeTankForce();
		}
		
		
		if((bot.scout.opponentStrategy == ScoutGroup.cannons || cannonBuildDetected) 
				&& bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode) < 5)
		{
			cannonBuildDetected = true;
			massTank.onFrame(game, self, bot);
			return;
		}
		
		if((bot.scout.opponentStrategy == ScoutGroup.tech || bot.scout.opponentStrategy == ScoutGroup.zealotPush
				|| (game.getFrameCount() >= 6000 && bot.scout.opponentStrategy == ScoutGroup.unknown)) 
				&& !antiTechStarted)
		{
			antiTechStarted = true;
			bot.defensiveTanks = 2;
			bot.army.mobileTankCount = 2;
		}
		
		if(antiTechStarted)
		{
			bot.drawCircle(new Position(self.getStartLocation().getX()*32 + 64, self.getStartLocation().getY()*32 + 32), Color.Green, 64);
			if (bot.getAvailableGas() >= 200)
				bot.workersPerGas = 1;
			else if (bot.getAvailableGas() <= 100)
				bot.workersPerGas = 2;
		}
		else
		{
			bot.drawCircle(new Position(self.getStartLocation().getX()*32 + 64, self.getStartLocation().getY()*32 + 32), Color.White, 64);
		}
		
		//if we're running out of supply and have enough minerals ...
		if (self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks) *3 + bot.ccCount * 3
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
				bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if(bot.getAvailableMinerals()>= 150 && (bot.count(UnitType.Terran_Barracks) < Math.min(bot.ccCount * 2, 10))) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if(bot.geyserCount > 0 && bot.getAvailableMinerals()>= 100 && bot.count(UnitType.Terran_Refinery) == 0 && bot.count(UnitType.Terran_Bunker) >= 1) 
		{
			bot.build(UnitType.Terran_Refinery);
		}
		
		if(bot.getAvailableMinerals()>= 125 && bot.count(UnitType.Terran_Engineering_Bay) < 1 && bot.count(UnitType.Terran_Bunker) >= 1) 
		{
			bot.build(UnitType.Terran_Engineering_Bay);
		}
		
		if(antiTechStarted && bot.count(UnitType.Terran_Engineering_Bay) >= 1 && bot.getAvailableMinerals() >= 75)
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
		
		if (bot.getAvailableMinerals() >= 150 
				&& bot.count(UnitType.Terran_Barracks) != 0 && bot.count(UnitType.Terran_Refinery) != 0
				&& bot.count(UnitType.Terran_Academy) == 0
				&& bot.count(UnitType.Terran_Engineering_Bay) > 0
				&& bot.army.units.size() >= 10
				&& bot.count(UnitType.Terran_Command_Center) >= 2)
		{
			bot.build(UnitType.Terran_Academy);
		}
		
		if (bot.getAvailableMinerals() >= 400 && (bot.wallOff == null || !bot.wallOff.placementFound || bot.wallOff.wall.size() <= 1))
		{
			bot.build(UnitType.Terran_Command_Center);
		}
		
		if (bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) >= 1)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				int count = structures.getUnitCount(UnitType.Terran_Bunker);
				
				if(count < (this.antiTechStarted && !structures.tooFar?2:1))
				{
		  			bot.buildDefensive(UnitType.Terran_Bunker, structures);
		  			break;
				}
			}
		}
		
		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Engineering_Bay) >= 1
				&& bot.count(UnitType.Terran_Barracks) >= 1 && bot.count(UnitType.Terran_Factory) < 1
				&& antiTechStarted)
		{
			bot.build(UnitType.Terran_Factory);
		}
		
		if (antiTechStarted 
				&& bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Academy) > 0
				&& bot.count(UnitType.Terran_Engineering_Bay) > 0
				&& bot.count(UnitType.Terran_Factory) > 0
				&& bot.count(UnitType.Terran_Command_Center) >= 2
				&& bot.count(UnitType.Terran_Starport) < 1)
		{
			bot.build(UnitType.Terran_Starport);
		}
	}
	
	@Override 
	public boolean overrideStructureOrder(Game game, Player self, Tyr bot, Agent agent)
	{
		if(!cannonBuildDetected && !cannonRush && agent.unit.getType() == UnitType.Terran_Factory)
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
			else if (!agent.unit.isTraining() && bot.getAvailableMinerals() >= 100)
				agent.unit.train(UnitType.Terran_Vulture);
			return true;
		}
		return false;
	}

}
