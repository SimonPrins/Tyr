import java.util.List;

import bwapi.Game;
import bwapi.Player;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;


public class ProductionStructures extends UnitGroup
{
	public ProductionStructures(OutOfJob rejects) 
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		for(Agent myUnit : units)
		{
			if(bot.build.overrideStructureOrder(game, self, bot, myUnit))
				continue;
			
			if(myUnit.unit.getType() == UnitType.Terran_Engineering_Bay && !myUnit.unit.isUpgrading())
			{
				if(bot.getAvailableMinerals() >= UpgradeType.Terran_Infantry_Weapons.mineralPrice()
						&& bot.getAvailableGas() >= UpgradeType.Terran_Infantry_Weapons.gasPrice())
					myUnit.unit.upgrade(UpgradeType.Terran_Infantry_Weapons);
				
				if(bot.getAvailableMinerals() >= UpgradeType.Terran_Infantry_Armor.mineralPrice()
						&& bot.getAvailableGas() >= UpgradeType.Terran_Infantry_Armor.gasPrice())
					myUnit.unit.upgrade(UpgradeType.Terran_Infantry_Armor);
			}
			
			if(myUnit.unit.getType() == UnitType.Terran_Machine_Shop && !myUnit.unit.isResearching())
			{
				if(bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 150)
					myUnit.unit.research(TechType.Tank_Siege_Mode);
			}
			
			if(myUnit.unit.getType() == UnitType.Terran_Factory && !myUnit.unit.isTraining())
			{
				if(myUnit.unit.getAddon() == null 
						&& bot.getAvailableMinerals() >= 50 && bot.getAvailableGas() >= 50)
					myUnit.unit.buildAddon(UnitType.Terran_Machine_Shop);
				else if(bot.getAvailableMinerals() >= 150 && bot.getAvailableGas() >= 100
						&& (bot.count(UnitType.Terran_Siege_Tank_Tank_Mode) == 0 || 
						self.hasResearched(TechType.Tank_Siege_Mode))
						|| bot.getAvailableGas() >= 250)
					myUnit.unit.train(UnitType.Terran_Siege_Tank_Tank_Mode);
			}
			
			//if this is Command Center, make it train additional worker
			if(myUnit.unit.getType() == UnitType.Terran_Command_Center && !myUnit.unit.isTraining())
			{
				if(bot.count(UnitType.Terran_Academy) == 1 && bot.getAvailableMinerals() >= 50 && bot.getAvailableGas() >= 50)
				{
					myUnit.unit.buildAddon(UnitType.Terran_Comsat_Station);
				}
	          	if (bot.getAvailableMinerals() >= 50
	          			&& bot.workForce.units.size() + bot.builders.units.size() < bot.maximumWorkers
	          			&& (self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() > bot.count(UnitType.Terran_Barracks)*3 + bot.ccCount * 3
	          					|| bot.getAvailableMinerals() >= 150)) {
	          		myUnit.unit.train(UnitType.Terran_SCV);
	          	}
			}

            //if this is a Barracks, make it train a marine
          	if (myUnit.unit.getType() == UnitType.Terran_Barracks 
          			&& (bot.scout.opponentStrategy != ScoutGroup.cannons || bot.getAvailableMinerals() >= 300) 
          			&& !myUnit.unit.isTraining()
          			&& bot.getAvailableMinerals() >= 50
          			&& (self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() > bot.count(UnitType.Terran_Barracks)*3 + bot.ccCount * 3 
          					|| bot.getAvailableMinerals() >= 150)
          					) {
          		myUnit.unit.train(UnitType.Terran_Marine);
          	}
          	
          	if(myUnit.unit.getType() == UnitType.Terran_Starport
          			&& !myUnit.unit.isTraining()
          			&& bot.getAvailableMinerals() >= 150
          			&& bot.getAvailableGas() >= 100)
          		myUnit.unit.train(UnitType.Terran_Wraith);
          	

        	if (myUnit.unit.getType() == UnitType.Zerg_Hatchery || myUnit.unit.getType() == UnitType.Zerg_Hive || myUnit.unit.getType() == UnitType.Zerg_Lair  )
        	{
        		List<Unit>larvas = myUnit.unit.getLarva();
        		for(Unit larva : larvas)
        		{
        			if ( (self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.ccCount * 3)
                  		  && (bot.getAvailableMinerals() >= 100)
                  		  && self.supplyTotal() + bot.supplyConstructing < 400)
        			{
        				larva.train(UnitType.Zerg_Overlord);
        	          	System.out.println("Overlord in training.");
        				continue;
                	}
    	          	
        			//if this is a larva, make it train additional worker
        			if (bot.getAvailableMinerals() >= 50 && bot.workForce.units.size() >= 12 
        					&& bot.count(UnitType.Zerg_Spawning_Pool) > 0)
        			{
        				larva.train(UnitType.Zerg_Zergling);
        	          	System.out.println("Zerglings in training.");
        				continue;
        			}

        	         //if this is a larva, make it train additional worker
        	         if (bot.getAvailableMinerals() >= 50 && bot.workForce.units.size() < bot.maximumWorkers) {
        	          	larva.train(UnitType.Zerg_Drone);
        	          	System.out.println("Drone in training.");
        				continue;
        	         }
        	         else
        	         {
          	          	System.out.println("Minerals: " + bot.getAvailableMinerals());
         	          	System.out.println("Supply: " + self.supplyUsed());
        	         }
        		}
        	}
            
          //if this is a Nexus, make it train an additional worker
        	if (myUnit.unit.getType() == UnitType.Protoss_Nexus && !myUnit.unit.isTraining() && bot.getAvailableMinerals() >= 50 
        			&& bot.workForce.units.size() < bot.maximumWorkers) {
        		myUnit.unit.train(UnitType.Protoss_Probe);
        	}
            
          //if this is a Gateway, make it train zealots!
        	if (myUnit.unit.getType() == UnitType.Protoss_Gateway && !myUnit.unit.isTraining())
        	{
        		if (bot.count(UnitType.Protoss_Cybernetics_Core) >= 1
        				&& bot.getAvailableMinerals() >= 125 && bot.getAvailableGas() >= 50
        				&& bot.count(UnitType.Protoss_Dragoon) <= bot.count(UnitType.Protoss_Zealot))
        			myUnit.unit.train(UnitType.Protoss_Dragoon);
        		else if(bot.getAvailableMinerals() >= 100
        				&& (bot.count(UnitType.Protoss_Dragoon) >= bot.count(UnitType.Protoss_Zealot) || bot.getAvailableMinerals() >= 150))
        			myUnit.unit.train(UnitType.Protoss_Zealot);
        	}
		}
		
	}
	
}
