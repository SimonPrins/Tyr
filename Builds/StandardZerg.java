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
import bwapi.UnitType;


public class StandardZerg extends BuildOrder
{

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		bot.maximumWorkers = 12;
		
		if (bot.getAvailableMinerals() >= 200 && bot.count(UnitType.Zerg_Spawning_Pool) == 0)
		{
			bot.build(UnitType.Zerg_Spawning_Pool);
		}
		
		//if (bot.getAvailableMinerals() >= 300)
		//{
		//	bot.build(UnitType.Zerg_Hatchery);
		//}
	}

}
