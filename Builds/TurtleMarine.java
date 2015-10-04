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


public class TurtleMarine extends BuildOrder
{
	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		bot.drawCircle(new Position(self.getStartLocation().getX()*32 + 64, self.getStartLocation().getY()*32 + 64), Color.Red, 32);
		
		bot.invasionDist = 768;
		//bot.drawCircle(TestBot1.tileToPostion(self.getStartLocation()), Color.White, 768);
		
		if(bot.army.rallyPoint == null)
			bot.army.rallyPoint = Tyr.tileToPostion(bot.getDefensePos(bot.defensiveStructures.get(0))); 
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks) *3 + bot.ccCount * 3)
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if(bot.getAvailableMinerals()>= 150 && bot.count(UnitType.Terran_Barracks) < 4) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if (bot.getAvailableMinerals() >= 100 && bot.count(UnitType.Terran_Barracks) >= 1 
				&& bot.count(UnitType.Terran_Bunker) < 2)
			bot.build(UnitType.Terran_Bunker);

		
		if(bot.getAvailableMinerals()>= 100 && bot.count(UnitType.Terran_Refinery) == 0 && bot.army.units.size() >= 4) 
		{
			bot.build(UnitType.Terran_Refinery);
		}

		
		if(bot.getAvailableMinerals()>= 125 && bot.count(UnitType.Terran_Engineering_Bay) < 1 && bot.count(UnitType.Terran_Refinery) >= 1) 
		{
			bot.build(UnitType.Terran_Engineering_Bay);
		}
		
		if(bot.count(UnitType.Terran_Missile_Turret) < 1 && bot.count(UnitType.Terran_Engineering_Bay) >= 1 && bot.getAvailableMinerals() >= 75)
		{
			bot.build(UnitType.Terran_Missile_Turret);
		}
		
		if (bot.getAvailableMinerals() >= 150 
				&& bot.count(UnitType.Terran_Barracks) != 0 && bot.count(UnitType.Terran_Refinery) != 0
				&& bot.count(UnitType.Terran_Academy) == 0)
		{
			bot.build(UnitType.Terran_Academy);
		}
	}

}
