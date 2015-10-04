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

import java.util.List;

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BaseLocation;


public class WraithAgent extends Agent
{
	int current = -1;
	BaseLocation target = null;
	
	int state = AttackState;

	public static final int AttackState = 0;
	public static final int FleeState = 1;
	
	
	public WraithAgent(Unit wraith)
	{
		super(wraith);
	}
	
	public void onFrame(Game game, Player self, Tyr bot)
	{
		List<Unit> inRange = game.getUnitsInRadius(unit.getPosition(), UnitType.Terran_Wraith.sightRange());
		
		Unit closestEnemy = null;
		double closestDistance = UnitType.Terran_Wraith.sightRange() + 100;
		for(Unit enemy: inRange)
		{
			if(enemy.getPlayer() == self)
				continue;
			
			if(enemy.getType().airWeapon() == WeaponType.None)
				continue;
			
			double dist = unit.getDistance(enemy);
			if (dist < closestDistance)
			{
				closestDistance = dist;
				closestEnemy = enemy;
			}
		}
		if (closestEnemy == null)
		{
			if (state != AttackState)
			{
				state = AttackState;
				current ++;

				if (current >= bot.suspectedEnemy.size() + bot.expands.size())
					current = 0;
			}
			if(unit.isIdle())
			{
				while(true)
				{
					current++;
					if (current >= bot.suspectedEnemy.size() + bot.expands.size())
						current = 0;
					BaseLocation newTarget;
					if (current < bot.suspectedEnemy.size())
						newTarget = bot.suspectedEnemy.get(current);
					else
						newTarget = bot.expands.get(current - bot.suspectedEnemy.size());
					
					boolean myBase = false;
					for(MineralWorkers base : bot.workForce.mineralWorkers)
					{
						if (base.resourceDepot.getPosition().getDistance(newTarget.getPosition()) <= 128)
						{
							myBase = true;
							break;
						}
					}
					
					if (myBase)
						continue;
					
					target = newTarget;
					break;
				}
				
				unit.attack(target.getPosition());
			}
		}
		else
		{
			state = FleeState;
			
			Position fleeTarget = retreatTarget(closestEnemy.getPosition(), 96);
			
			unit.move(fleeTarget);
			
			game.drawLineMap(closestEnemy.getX(), closestEnemy.getY(), fleeTarget.getX(), fleeTarget.getY(), Color.Red);
		}
	}
}
