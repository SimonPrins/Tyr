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
import bwapi.UnitType;


public class MassTank extends BuildOrder
{
	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		bot.drawCircle(new Position(self.getStartLocation().getX()*32 + 64, self.getStartLocation().getY()*32 + 32), Color.Yellow, 64);
		
		if(bot.army.tankForce == null)
		{
			bot.workersPerGas = 2;
			bot.maximumWorkers = 25;
			bot.army.requiredSize = 5;
			bot.army.initializeTankForce();
			bot.bunkers.disabled = true;
		}
		

		
		//if we're running out of supply and have enough minerals ...
		if (self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks) *3 + bot.ccCount * 3
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			if (bot.count(UnitType.Terran_Supply_Depot) == 0
					&& bot.defensiveStructures.size() >= 1)
			{
				Position desiredPos = bot.defensiveStructures.get(0).getDefensePos();
				if (desiredPos != null)
					bot.build(UnitType.Terran_Supply_Depot, desiredPos);
				else
					bot.build(UnitType.Terran_Supply_Depot);
			}
			else
				bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if(bot.getAvailableMinerals()>= 150 
				&& ((bot.count(UnitType.Terran_Barracks) < 1) || (bot.getAvailableMinerals()>= 300))
				&& (bot.count(UnitType.Terran_Barracks) < Math.min(bot.ccCount * 2, 10)))
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if(bot.getAvailableMinerals() >= 200 && bot.getAvailableGas() >= 100
				&& bot.count(UnitType.Terran_Barracks) >= 1 && bot.count(UnitType.Terran_Factory) < 1)
		{
			bot.build(UnitType.Terran_Factory);
		}
		
		if(bot.geyserCount > 0 && bot.getAvailableMinerals()>= 100 && bot.count(UnitType.Terran_Refinery) == 0 && bot.count(UnitType.Terran_Barracks) >= 1) 
		{
			bot.build(UnitType.Terran_Refinery);
		}
	}

}
