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
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;


public class TankForce extends UnitGroup 
{
	public OutOfJob outOfJob = new OutOfJob();
	public Position rallyPoint = null;
	Position target = null;
	
	public TankForce(OutOfJob rejects)
	{
		super(rejects);
	}
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if(BWTAInitializer.initialized)
		{
			acquireTarget(bot);
			if(target == null)
			{
				if(bot.army.rallyPoint != null)
					target = bot.army.rallyPoint;
				else
					target = bot.getMainExit();
			}
			
			for(Agent agent : units)
			{
				agent.drawCircle(Color.Yellow);
				
				if(agent.unit.isIdle() || bot.defenseTime == 0)
					agent.unit.attack(target);
				

				boolean targetExists = false;
				int radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
				for(Unit unit : Tyr.game.enemy().getUnits())
				{
					if (unit.getType().isFlyer() || unit.isLifted())
						continue;
					if (agent.distanceSquared(unit) <= radius * radius)
					{
						targetExists = true;
						break;
						
					}
				}
				
				if(agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
				{
					
					if (targetExists)
						agent.unit.siege();
					/*
					for(EnemyPosition enemy : bot.enemyBuildingMemory)
					{
						if(enemy.type == UnitType.Protoss_Photon_Cannon
								&& agent.distanceSquared(enemy.pos) <= 
								UnitType.Terran_Siege_Tank_Siege_Mode.sightRange()*UnitType.Terran_Siege_Tank_Siege_Mode.sightRange())
						{
							agent.unit.siege();
						}
					}
					*/
				}
				else if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
				{
					/*
					boolean inrange = false;
					for(EnemyPosition enemy : bot.enemyBuildingMemory)
					{
						if(enemy.type == UnitType.Protoss_Photon_Cannon
								&& agent.distanceSquared(enemy.pos) <= 
										UnitType.Terran_Siege_Tank_Siege_Mode.sightRange()*UnitType.Terran_Siege_Tank_Siege_Mode.sightRange())
						{
							inrange = true;
							break;
						}
					}
					*/
					if(!targetExists)
						agent.unit.unsiege();
				}
			}
		}
	}
	
	public void acquireTarget(Tyr bot)
	{
		Region end = null;
		
		target = null;
    	for(EnemyPosition p : bot.enemyBuildingMemory)
    	{
    		target = p.pos;
    		end = BWTA.getRegion(p.pos);
			continue;
    	}

    	if (end == null)
    		for (BaseLocation b : bot.suspectedEnemy) 
    		{
    			target = b.getPosition();
    			end = b.getRegion();
    			break;
    		}
	}
	
	@Override
	public void add(Agent agent)
	{
		super.add(agent);
		outOfJob.add(agent);
	}
}
