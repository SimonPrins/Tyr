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
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;


public class BCAttack extends Command
{
	private Position target;
	
	private static final boolean stutterstep = false;
	
	public BCAttack(Agent agent, Position target)
	{
		super(agent);
		this.target = target;
	}
	
	@Override
	public void execute(Game game, Player self, Tyr bot) 
	{
		agent.drawCircle(Color.Red);
		
		attack();
	}

	@SuppressWarnings("unused")
	private void attack()
	{
		if (agent.distanceSquared(target) <= 128*128)
			return;
		
		Unit yamatoTarget = agent.getYamatoTarget();
		if(yamatoTarget != null)
		{
			agent.unit.useTech(TechType.Yamato_Gun, yamatoTarget);
			return;
		}
		
		Order order = agent.unit.getOrder(); 
		Position orderTarget = agent.unit.getOrderTargetPosition();
		
		Unit closest = getClosest();
		double distSq = UnitType.Terran_Battlecruiser.groundWeapon().maxRange() - 64;
		distSq *= distSq;
		
		if (stutterstep && closest != null && agent.unit.getGroundWeaponCooldown() > 1 && agent.unit.getAirWeaponCooldown() > 1 && agent.distanceSquared(closest) <= distSq)
		{
			agent.unit.move(Tyr.tileToPostion(Tyr.game.self().getStartLocation()));
		}
		else if (agent.unit.isHoldingPosition() || agent.unit.isIdle() || (stutterstep && (agent.unit.getGroundWeaponCooldown() == 1 || agent.unit.getAirWeaponCooldown() == 1)))
		{
			agent.unit.attack(target);
		}
		else 
		{
			double dist = target.getDistance(orderTarget);
			if (order == Order.AttackMove 
					&& orderTarget != null
					&& agent.unit.getTarget() == null
					&&
				(Math.abs(target.getX() - orderTarget.getX()) >= 10 
					|| Math.abs(target.getY() - orderTarget.getY()) >= 10
					|| dist >= 10))
			{
				agent.unit.attack(target);
			}
		}
	}
	
	
	private Unit getClosest()
	{
		Game game = Tyr.game;
		List<Unit> inRange = game.getUnitsInRadius(agent.unit.getPosition(), UnitType.Terran_Battlecruiser.groundWeapon().maxRange());
		Unit result = null;
		double distSq = game.mapHeight()*32 + game.mapWidth()*32;
		distSq = distSq * distSq;
		for (Unit unit : inRange)
		{
			if (!unit.getPlayer().isEnemy(game.self()))
				continue;
			
			double newDist = agent.distanceSquared(unit);
			
			if (newDist < distSq)
			{
				result = unit;
				distSq = newDist;
			}
		}
		return result;
	}

	@Override
	public boolean replace(Command command) 
	{
		if (!command.getClass().equals(BCAttack.class))
			return true;
		
		return ((BCAttack)command).target.getX() != target.getX() || ((BCAttack)command).target.getY() != target.getY();
	}
	
}
