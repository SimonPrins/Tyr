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
import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;


public class Defend extends Command
{
	private Position target;
	private Unit killTarget;
	
	public Defend(Agent agent, Position target)
	{
		super(agent);
		this.target = target;
	}
	
	@Override
	public void execute(Game game, Player self, Tyr bot) 
	{
		agent.drawCircle(Color.Green);
		
		if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
		{
			int radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
			boolean found = false;
			for(Unit unit : Tyr.game.enemy().getUnits())
			{
				if (unit.getType().isFlyer() || unit.isLifted())
					continue;
				if (agent.distanceSquared(unit) <= radius * radius)
				{
					found = true;
					break;
				}
			}
			
			if(!found)
			{
				agent.unit.unsiege();
				return;
			}
		}
		else if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
		{
			int radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
			boolean found = false;
			for(Unit unit : Tyr.game.enemy().getUnits())
			{
				if (unit.getType().isFlyer() || unit.isLifted())
					continue;
				if (agent.distanceSquared(unit) <= radius * radius)
				{
					found = true;
					break;
				}
			}
			
			if(found)
			{
				agent.unit.siege();
				return;
			}
		}
		
		if(bot.wallOff != null && bot.wallOff.wall.size() > 1
				&& BWTAInitializer.initialized
				&& BWTA.getRegion(bot.invader.getPosition()) != BWTA.getRegion(self.getStartLocation())
				&& agent.distanceSquared(target) <= 256*256
				&& agent.unit.getOrder() != Order.HoldPosition)
		{
			agent.unit.holdPosition();
		}
		
    	if (killTarget == null)
    	{
    		List<Unit> inRange = game.getUnitsInRadius(agent.unit.getPosition(), agent.unit.getType().groundWeapon().maxRange());
    		
    		for(Unit unit : inRange)
    		{
    			if (unit.getPlayer().isEnemy(self) && unit.getType() != UnitType.Protoss_Observer)
    			{
    				killTarget = unit;
    				break;
    			}
    		}
    	}
		
		if (killTarget != null)
		{
			boolean targetFlies = killTarget.getType().isFlyer() || killTarget.isLifted();
			int maxRangeSq = targetFlies?agent.unit.getType().airWeapon().maxRange(): agent.unit.getType().groundWeapon().maxRange();
			maxRangeSq = maxRangeSq * maxRangeSq;
			
			if (agent.distanceSquared(killTarget) >= maxRangeSq)
				killTarget = null;
			else
			{
				game.drawLineMap(agent.unit.getX(), agent.unit.getY(), killTarget.getX(), killTarget.getY(), Color.Red);
				
				int cooldown = targetFlies?agent.unit.getAirWeaponCooldown():agent.unit.getGroundWeaponCooldown();
			
				if (cooldown == 0)
				{
					if (agent.unit.getOrder() != Order.AttackUnit)
						agent.unit.attack(killTarget);
				}
				else
				{
					Position fleeTarget = agent.retreatTarget(killTarget.getPosition(), 32);
					agent.unit.move(fleeTarget);
				}
				
				return;
			}
		}
		
		if (bot.army.enemyThreat)
		{
			Position orderTarget = agent.unit.getOrderTargetPosition();
			if (agent.unit.isIdle()
					|| (killTarget != null &&(Math.abs(orderTarget.getX() - target.getX()) >= 128 || Math.abs(orderTarget.getY() - target.getY()) >= 128))
					|| (killTarget != null && (agent.unit.getGroundWeaponCooldown() == 1 || agent.unit.getAirWeaponCooldown() == 1)))
				
				agent.unit.attack(bot.invader.getPosition());
		}
		else
		{
			if ((agent.unit.getOrder() == Order.Move || agent.unit.isIdle()))
				agent.unit.attack(target);
		}
	}

	@Override
	public boolean replace(Command command) 
	{
		if (!command.getClass().equals(Defend.class))
			return true;
		
		return ((Defend)command).target.getX() == target.getX() && ((Defend)command).target.getY() == target.getY();
	}
	
}
