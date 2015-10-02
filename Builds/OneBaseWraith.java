import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;


public class OneBaseWraith extends BuildOrder
{
	boolean scoutRequested = false;
	
	boolean initialized = false;
	int dropshipCount = 0;

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		DebugMessages.addMessage("Going Wraiths.");
		
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
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks) *3 + bot.count(UnitType.Terran_Starport) *3 + bot.ccCount * 3)
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
		
		if(bot.getAvailableMinerals()>= 100 
				&& bot.count(UnitType.Terran_Refinery) == 0
				&& bot.count(UnitType.Terran_Barracks) >= 1) 
		{
			bot.build(UnitType.Terran_Refinery);
		}
		
		if (bot.getAvailableMinerals() >= 150 
				&& bot.count(UnitType.Terran_Barracks) != 0 && bot.count(UnitType.Terran_Refinery) != 0
				&& bot.count(UnitType.Terran_Academy) == 0
				&& bot.count(UnitType.Terran_Engineering_Bay) > 0
				&& bot.count(UnitType.Terran_Wraith) >= 5)
		{
			bot.build(UnitType.Terran_Academy);
		}
		
		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Barracks) >= 1 && bot.count(UnitType.Terran_Factory) < 1)
		{
			bot.build(UnitType.Terran_Factory);
		}
		
		if (bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Starport) < 2)
		{
			bot.build(UnitType.Terran_Starport);
		}
	}
	
	@Override 
	public boolean overrideStructureOrder(Game game, Player self, Tyr bot, Agent agent)
	{
		if(agent.unit.getType() == UnitType.Terran_Factory)
		{
			return true;
		}
		if(agent.unit.getType() == UnitType.Terran_Barracks)
		{
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
