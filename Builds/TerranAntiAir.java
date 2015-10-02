import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.UnitType;
import bwapi.UpgradeType;


public class TerranAntiAir extends BuildOrder
{
	boolean scoutRequested = false;
	
	boolean initialized = false;

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if (!initialized)
		{
			initialized = true;
			bot.army.requiredSize = 40;
			bot.army.maximumSize = 60;
		}
		
		if(game.getFrameCount() >= 1600 && !scoutRequested)
		{
			scoutRequested = true;
			bot.scout.requestWorkerScout(bot);
		}
		
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
				&& (bot.count(UnitType.Terran_Barracks) < 1)) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if(bot.geyserCount > 0 && bot.getAvailableMinerals()>= 100 
				&& (bot.count(UnitType.Terran_Refinery) == 0
				|| ( bot.count(UnitType.Terran_Refinery) < bot.count(UnitType.Terran_Command_Center) && bot.count(UnitType.Terran_Starport) > 0)) 
				&& bot.count(UnitType.Terran_Barracks) >= 1
				&& bot.count(UnitType.Terran_Bunker) >= 1) 
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
				
				if(count < 1)
				{
		  			bot.buildDefensive(UnitType.Terran_Bunker, structures);
		  			break;
				}
			}
		}
		
		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Barracks) >= 1 && bot.count(UnitType.Terran_Factory) < 3)
		{
			bot.build(UnitType.Terran_Factory);
		}
		
		if (bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Factory) >= 2
				&& bot.count(UnitType.Terran_Command_Center) >= 2
				&& bot.count(UnitType.Terran_Starport) < 2)
		{
			bot.build(UnitType.Terran_Starport);
		}

		if(bot.count(UnitType.Terran_Engineering_Bay) >= 1 && bot.getAvailableMinerals() >= 75)
		{
			for(DefensiveStructures structures : bot.defensiveStructures)
			{
				int count = structures.getUnitCount(UnitType.Terran_Missile_Turret);
				
				if(count < 2 && structures.defenses.size() > 0)
				{
		  			bot.buildDefensive(UnitType.Terran_Missile_Turret, structures);
		  			break;
				}
			}
		}
		
		if (bot.count(UnitType.Terran_Starport) >= 2
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
			if (!agent.unit.isTraining() && bot.getAvailableMinerals() >= 100)
				agent.unit.train(UnitType.Terran_Vulture);
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Starport)
		{
			if (agent.unit.getAddon() == null 
					&& bot.getAvailableMinerals() >= 50 && bot.getAvailableGas() >= 50)
				agent.unit.buildAddon(UnitType.Terran_Control_Tower);
			else if(!agent.unit.isTraining() && bot.getAvailableMinerals() >= 400 && bot.getAvailableGas() >= 300 && bot.count(UnitType.Terran_Armory) >= 1)
				agent.unit.train(UnitType.Terran_Valkyrie);
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Science_Facility)
		{
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Engineering_Bay)
		{
			return true;
		}
		else if (agent.unit.getType() == UnitType.Terran_Physics_Lab)
		{
			return true;
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
		return false;
	}

}
