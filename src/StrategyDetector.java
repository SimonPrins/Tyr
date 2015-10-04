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
import bwapi.Unit;
import bwapi.UnitType;


public class StrategyDetector 
{
	public int opponentStrategy = ScoutGroup.unknown;
	
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if (opponentStrategy != ScoutGroup.unknown)
			return;
		
		if(game.enemy().getRace() == Race.Protoss)
		{
			int nexusCount = 0;
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
			{
				if (enemy.type == UnitType.Protoss_Nexus)
					nexusCount++;
			}
			if(nexusCount >= 2)
			{
				opponentStrategy = ScoutGroup.tech;
				return;
			}
			for(Unit enemy : game.enemy().getUnits())
			{
				if(enemy.getType().gasPrice() > 0 
						|| enemy.getType() == UnitType.Protoss_Assimilator 
						|| enemy.getType() == UnitType.Protoss_Cybernetics_Core)
				{
					opponentStrategy = ScoutGroup.tech;
					return;
				}
			}
				int gatewayCount = 0;
				for(EnemyPosition enemy : bot.enemyBuildingMemory)
				{
					if (enemy.type == UnitType.Protoss_Photon_Cannon || enemy.type == UnitType.Protoss_Forge)
					{
						opponentStrategy = ScoutGroup.cannons;
						return;
					}
					if (enemy.type == UnitType.Protoss_Gateway)
						gatewayCount++;
				}
				if(gatewayCount >= 2)
				{
					opponentStrategy = ScoutGroup.zealotPush;
					return;
				}
		}
		if(game.enemy().getRace() == Race.Terran)
		{
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
				if (enemy.type == UnitType.Terran_Bunker)
				{
					opponentStrategy = ScoutGroup.defensive;
					return;
				}
			
			int tankCount = 0;
			for(Unit enemy : game.enemy().getUnits())
			{
				if(enemy.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
						|| enemy.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
					tankCount++;
			}
			if(tankCount >= 3)
			{
				opponentStrategy = ScoutGroup.defensive;
				return;
			}
		}
	}
}
