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
import bwta.BWTA;
import bwta.Region;


public class BCSquad extends UnitGroup
{
	public BCSquad(OutOfJob rejects) 
	{
		super(rejects);
	}

	boolean attackMode = false;
	boolean retreatMode = false;
	
	Region current = null;
	ArrayList<Region> path = new ArrayList<Region>();
	Position target = null;
	
	public static int fleeMinimum = 3;
	public static int moveOutMinimum = 6;
	public static int maximum = 8;
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		
		if(BWTAInitializer.initialized && current == null && units.size() > 0)
		{
			current = BWTA.getRegion(self.getStartLocation());
			
			if(target == null)
				getDefenseTarget(bot);
		}
		
		boolean defend = bot.invader != null && bot.areWeBeingInvaded;
		boolean attack = bot.invader == null && units.size() >= moveOutMinimum;

		
		if(attack)
		{
			for(Agent agent : units)
			{
				if(agent.unit.getHitPoints() < agent.unit.getType().maxHitPoints())
				{
					attack = false;
					break;
				}
			}
		}
		
		if (defend)
		{
			for(Agent agent : units)
				bot.drawCircle(agent.unit.getPosition(), Color.Green, 32);
		}
		else if (!attackMode && !retreatMode)
		{
			for(Agent agent : units)
				bot.drawCircle(agent.unit.getPosition(), Color.Yellow, 32);
		}
		else if (retreatMode)
		{
			for(Agent agent : units)
				bot.drawCircle(agent.unit.getPosition(), Color.White, 32);
		}
		else if (attackMode)
		{
			for(Agent agent : units)
				bot.drawCircle(agent.unit.getPosition(), Color.Red, 32);
		}
		
		if (attackMode)
		{
			if (units.size() < fleeMinimum)
			{
				attackMode = false;
				retreatMode = true;
				getDefenseTarget(bot);
			}
			else
			{
				for(Agent unit : units)
					if (unit.unit.getHitPoints() <= 125)
					{
						attackMode = false;
						retreatMode = true;
						getDefenseTarget(bot);
					}
			}
		}
		
		if (retreatMode)
		{
			if(target != null)
				bot.drawCircle(target, Color.Blue, 160);
			boolean returned = true;
			for(Agent agent : units)
			{
				agent.order(new None(agent));
				if(target != null && agent.distanceSquared(target) >= 200*200)
				{
					returned = false;
					bot.drawCircle(agent.unit.getPosition(), Color.Red, 6);
					game.drawLineMap(target.getX(), target.getY(), agent.unit.getX(), agent.unit.getY(), Color.Red);
				}
				else
				{
					bot.drawCircle(agent.unit.getPosition(), Color.Green, 6);
				}
			}
			
			if(returned)
				retreatMode = false;
			else
			{
				for(Agent agent : units)
				{
					agent.drawCircle(Color.White);
					agent.unit.move(target);
				}
				return;
			}
		}
		
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
			if (target != null)
				bot.drawCircle(target, Color.Orange, 128);
			
			
			if (targetChanged && target != null)
				for(Agent agent : units)
					agent.order(new BCAttack(agent, target));
		}
		else
		{
			if (target != null)
			{
				bot.drawCircle(target, Color.Blue, 160);
				for(Agent agent : units)
					agent.order(new BCDefend(agent, target));
			}
		}
	}
	
	public boolean getDefenseTarget(Tyr bot)
	{
		Position newTarget = null;
		if(bot.army.rallyPoint != null)
			newTarget = bot.army.rallyPoint;
		else if (bot.defensiveStructures.get(0).defenses.size() > 0)
			newTarget = bot.defensiveStructures.get(0).defenses.get(0).getPosition();
		else
			newTarget = bot.defensiveStructures.get(0).getDefensePos();
		
		if (newTarget == null)
			return false;
		
		if(target != null && newTarget.getX() == target.getX() && newTarget.getY() == target.getY())
			return false;
		
		target = newTarget;
		return true;
	}
	
	public boolean acquireTarget(Tyr bot)
	{
		if (target != null)
	    	for(EnemyPosition p : bot.enemyBuildingMemory)
	    		if (p.pos.getX() == target.getX() && p.pos.getY() == target.getY())
	    			return false;
		
		Position newTarget = null;
		Position mainPos = bot.suspectedEnemy.get(0).getPosition();
		if (bot.suspectedEnemy.size() == 0)
			return false;
		int dist = 0;

		for (EnemyPosition p : bot.enemyBuildingMemory)
		{
			int newDist = (int)p.pos.getDistance(mainPos);
			if (newDist > dist)
			{
				newTarget = p.pos;
				dist = newDist;
			}
		}
		
		if(newTarget == null)
			return false;
		
    	if (target != null && target.getX() == newTarget.getX() && target.getY() == newTarget.getY())
    		return false;
    	
    	target = newTarget;
    	return true;
	}
}
