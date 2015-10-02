import bwapi.Game;
import bwapi.Player;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;


public class TwoBaseBC extends BuildOrder
{
	boolean scoutRequested = false;
	
	boolean initialized = false;
	boolean zealotsDetected = false;
	int dropshipCount = 0;
	boolean coreExists = false;
	boolean BCs = false;
	
	boolean isExpanding = false;

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(bot.scout.opponentStrategy == ScoutGroup.zealotPush)
			zealotsDetected = true;
		
		if(!coreExists)
		{
			for(EnemyPosition building : bot.enemyBuildingMemory)
			{
				if (building.type == UnitType.Protoss_Cybernetics_Core 
						|| building.type == UnitType.Protoss_Templar_Archives
						|| building.type == UnitType.Protoss_Citadel_of_Adun
						|| building.type == UnitType.Protoss_Assimilator)
				{
					coreExists = true;
					break;
				}
			}
		}
		
		if (coreExists)
		{
			DebugMessages.addMessage("DTs incoming.");
			
			
			if(bot.getAvailableMinerals()>= 125
				&& bot.count(UnitType.Terran_Engineering_Bay) < 1 
				&& (bot.count(UnitType.Terran_Barracks) >= 1)) 
			{
				bot.build(UnitType.Terran_Engineering_Bay);
			}
		}
		
		DebugMessages.addMessage("Going Mech.");
		
		if (!initialized)
		{
			initialized = true;
			bot.army.requiredSize = 30;
			bot.army.maximumSize = 60;
			bot.bunkers.maximumRepair = 1;
		}
		
		if(game.getFrameCount() >= 1800 && !scoutRequested)
		{
			scoutRequested = true;
			bot.scout.requestWorkerScout(bot);
		}
		
		bot.defensiveTanks = 2;
		
		if (bot.getAvailableGas() >= 600)
			bot.workersPerGas = bot.count(UnitType.Terran_Physics_Lab) >= 1?1:0;
		else if (bot.getAvailableGas() >= 400)
				bot.workersPerGas = bot.count(UnitType.Terran_Physics_Lab) >= 1?2:1;
		else if (bot.getAvailableGas() <= 300)
			bot.workersPerGas = bot.count(UnitType.Terran_Physics_Lab) >= 1?3:2;
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks) *3 
				+ bot.count(UnitType.Terran_Starport) *3 
				+ bot.count(UnitType.Terran_Factory) * 4
				+ bot.ccCount * 3)
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if(bot.getAvailableMinerals()>= 150
				&& (bot.count(UnitType.Terran_Barracks) < 1)) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if(bot.geyserCount > 0 && bot.getAvailableMinerals()>= 100 
				&& (bot.count(UnitType.Terran_Refinery) == 0
				|| ( bot.count(UnitType.Terran_Refinery) < bot.count(UnitType.Terran_Command_Center) && bot.count(UnitType.Terran_Starport) > 0)) 
				&& bot.count(UnitType.Terran_Barracks) >= 1) 
		{
			bot.build(UnitType.Terran_Refinery);
		}
		
		if(bot.getAvailableMinerals()>= 125 && bot.count(UnitType.Terran_Engineering_Bay) < 1 && bot.count(UnitType.Terran_Factory) >= 2) 
		{
			bot.build(UnitType.Terran_Engineering_Bay);
		}
		
		if (bot.getAvailableMinerals() >= 150 
				&& bot.count(UnitType.Terran_Barracks) != 0 && bot.count(UnitType.Terran_Refinery) != 0
				&& bot.count(UnitType.Terran_Academy) == 0
				&& bot.count(UnitType.Terran_Factory) > 0)
		{
			bot.build(UnitType.Terran_Academy);
		}
		
		boolean beingConstructed = false;
		for(BuildCommand command : bot.buildCommands)
			if (command.building == UnitType.Terran_Command_Center)
			{
				beingConstructed = true;
				break;
			}
		
		int patchCount = 0;
		for(MineralWorkers base : bot.workForce.mineralWorkers)
			patchCount += base.minerals.size();
		
		if (bot.getAvailableMinerals() >= 400 &&
				!beingConstructed &&
				(bot.count(UnitType.Terran_Command_Center) < 2  || patchCount * 2 + 4 < bot.workForce.units.size() + bot.builders.units.size())
				&& (bot.army.units.size() >= bot.army.requiredSize - 5 || isExpanding))
		{
			isExpanding = true;
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
				
				if(count < 1)
				{
		  			bot.buildDefensive(UnitType.Terran_Bunker, structures);
		  			break;
				}
			}
		}
		
		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Barracks) >= 1)
		{
			if (bot.count(UnitType.Terran_Factory) < 1)
			{
				bot.build(UnitType.Terran_Factory);
			}
			else if( bot.count(UnitType.Terran_Factory) < 3
					&& bot.getAvailableMinerals() >= 300
					//&& (bot.count(UnitType.Terran_Factory) < 2 || bot.ccCount >= 2)
					)
			{
				bot.build(UnitType.Terran_Factory);
			}
		}
		
		if(BCs)
		{
			if (bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
					&& bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) +  bot.count(UnitType.Terran_Siege_Tank_Tank_Mode) >= 2
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
		if(agent.unit.getType() == UnitType.Terran_Barracks)
		{
			if (!agent.unit.isTraining() && bot.getAvailableMinerals() >= 50
					&& (bot.count(UnitType.Terran_Marine) < 8 || coreExists || bot.getAvailableMinerals() >= 500))
				agent.unit.train(UnitType.Terran_Marine);
			return true;
		}
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
			else if (!agent.unit.isTraining() 
					&& bot.getAvailableMinerals() >= 100
					//&& (bot.getAvailableMinerals() >= 500 || (bot.scout.opponentStrategy == ScoutGroup.zealotPush && bot.count(UnitType.Terran_Command_Center) == 1)) 
					&& (bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Tank_Mode)
					>= neededTanks || bot.getAvailableMinerals() >= 200))
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
