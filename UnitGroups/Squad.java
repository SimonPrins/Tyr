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

import java.util.ArrayList;
import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;


public class Squad extends UnitGroup
{
	public Squad(OutOfJob rejects) 
	{
		super(rejects);
	}

	boolean attackMode = false;
	
	Region current = null;
	ArrayList<Region> path = new ArrayList<Region>();
	Position target = null;
	
	int waitingAtCannon = 0;
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if(BWTAInitializer.initialized && current == null && units.size() > 0)
		{
			current = BWTA.getRegion(self.getStartLocation());
			
			if (bot.wallOff != null && bot.wallOff.placementFound)
			{
				if(bot.wallOff.buildPlan.size() > 0)
					target = Tyr.tileToPostion(bot.wallOff.buildPlan.get(0).position);
				else if (bot.wallOff.underConstruction.size() > 0)
					target = Tyr.tileToPostion(bot.wallOff.underConstruction.get(0).position);
				else if (bot.wallOff.wall.size() > 0 && bot.wallOff.wall.get(0) != null && bot.wallOff.wall.get(0).getPosition() != null)
					target = bot.wallOff.wall.get(0).getPosition();
				
				if(target != null)
				{
					double dist = target.getDistance(new Position(self.getStartLocation().getX()*32, self.getStartLocation().getY()*32));
					
					target = new Position((int)(target.getX() + (self.getStartLocation().getX()*32 - target.getX())*64/dist), 
							(int)(target.getY() + (self.getStartLocation().getY()*32 - target.getY())*64/dist));
				}
			}
			
			if(target == null)
			{
				if(bot.army.rallyPoint != null)
					target = bot.army.rallyPoint;
				else if (bot.defensiveStructures.get(0).defenses.size() > 0)
					target = bot.defensiveStructures.get(0).defenses.get(0).getPosition();
				else
					target = bot.defensiveStructures.get(0).getDefensePos();
			}
		}
		
		boolean defend = bot.invader != null && bot.areWeBeingInvaded;
		boolean attack = (self.supplyUsed() == 400 || bot.army.units.size() >= bot.army.requiredSize) && bot.invader == null && units.size() >= 5;
		
		if(!defend && !attackMode && !attack)
		{
			if(bot.army.rallyPoint != null && target != bot.army.rallyPoint)
				target = bot.army.rallyPoint;
		}
		
		boolean targetChanged = false;
		
		if(BWTAInitializer.initialized && !attackMode && attack)
			attackMode = true;
		
		if (!defend && BWTAInitializer.initialized && attackMode && bot.invader == null)
			targetChanged = acquireTarget(bot);
		
				
		if(!defend && BWTAInitializer.initialized && attackMode)
		{
			if(bot.wallOff != null && bot.wallOff.wall.size() > 0)
			{
				Unit breakDepot = null;
				for(Unit unit : bot.wallOff.wall)
					if(unit.getType() == UnitType.Terran_Supply_Depot)
						breakDepot = unit;
				
				bot.wallOff.wall = new ArrayList<Unit>();
				bot.wallOff.wall.add(breakDepot);
				
				for(Agent myUnit : units)
					if(myUnit.unit.isIdle())
						myUnit.unit.attack(breakDepot);
				
				return;
			}

			if (target != null)
				bot.drawCircle(target, Color.Orange, 128);
				
			
			if (targetChanged && target != null)
				for(Agent agent : units)
					agent.order(agent.unit.getType() == UnitType.Terran_Battlecruiser?new BCAttack(agent, target):new Attack(agent, target));
		}
		else
		{
			if (target != null)
			{
				bot.drawCircle(target, Color.Green, 128);
				for(Agent agent : units)
					agent.order(agent.unit.getType() == UnitType.Terran_Battlecruiser?new BCDefend(agent, target):new Defend(agent, target));
			}
		}
	}
	
	public boolean acquireTarget(Tyr bot)
	{
		if (target != null)
		{
	    	for(EnemyPosition p : bot.enemyBuildingMemory)
	    	{
	    		if (p.pos.getX() == target.getX() && p.pos.getY() == target.getY())
	    			return false;
	    	}
		}
		
		Position newTarget = null;
    	for(EnemyPosition p : bot.enemyBuildingMemory)
    	{
    		newTarget = p.pos;
			break;
    	}

    	if (newTarget == null)
    		for (BaseLocation b : bot.suspectedEnemy) 
    		{
    			newTarget = b.getPosition();
    			break;
    		}
    	
    	if (newTarget == null)
    		return false;
    	
    	if (target == null)
    	{
    		target = newTarget;
    		return true;
    	}
    	
    	
    	if (target.getX() == newTarget.getX() && target.getY() == newTarget.getY())
    		return false;
    	
    	target = newTarget;
    	return true;
	}

	public void addTanks(UnitGroup group) 
	{
		addTanks(group, units.size());
	}

	public int addTanks(UnitGroup group, int upTo)
	{
		return addTanks(group.units, upTo);
	}
	public int addTanks(ArrayList<Agent> group, int upTo) 
	{
		int count = 0;
		for(int i = 0; count < upTo && i<units.size(); i++)
		{
			Agent agent = units.get(i);
			if (agent == null)
				continue;
			if(agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
					|| agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
			{
				agent.order(new None(agent));
				group.add(agent);
				units.remove(i);
				i--;
				count++;
			}
		}
		return count;
		
	}

}
