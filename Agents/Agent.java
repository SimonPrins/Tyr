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
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;


public class Agent
{
	protected Unit unit;
	protected Command command;
	
	public Agent(Unit unit)
	{
		this.unit = unit;
		command = new None(this);
	}
	
	public boolean isDead()
	{
		return unit.getHitPoints() <= 0 || unit.getRemoveTimer() != 0 || !unit.exists() || unit.getPlayer() != Tyr.game.self();
	}
	
	public void drawCircle(Color color)
	{
		Tyr.bot.drawCircle(unit.getPosition(), color);
	}

	public void drawCircle(Color color, int r)
	{
		Tyr.bot.drawCircle(unit.getPosition(), color, r);
		
	}
	
	public int distanceSquared(Agent agent)
	{
		return distanceSquared(agent.unit);
	}
	
	public int distanceSquared(Unit unit)
	{
		return distanceSquared(unit.getPosition());
	}
	
	public int distanceSquared(Position pos)
	{
		int dx = unit.getPosition().getX() - pos.getX();
		int dy = unit.getPosition().getY() - pos.getY();
		return dx*dx + dy*dy;
	}

	public static Agent createAgent(Unit unit) 
	{
		if (unit.getType() == UnitType.Terran_Wraith)
			return new WraithAgent(unit);
		if (unit.getType() == UnitType.Terran_Bunker)
			return new BunkerAgent(unit);
		if(unit.getType().isWorker())
			return new WorkerAgent(unit);
		
		return new Agent(unit);
	}
	
	public Position retreatTarget(Position other, double dist)
	{
		double fleeDX = unit.getPosition().getX() - other.getX();
		double fleeDY = unit.getPosition().getY() - other.getY();
		
		double length = Math.sqrt(fleeDX*fleeDX + fleeDY*fleeDY);
		
		if (length == 0)
		{
			fleeDX = 1;
			fleeDY = 0;
		}
		else
		{
			fleeDX /= length;
			fleeDY /= length;
		}
		
		return new Position(
				(int)(unit.getPosition().getX() + fleeDX * dist),
				(int)(unit.getPosition().getY() + fleeDY * dist));
	}
	
	public void order(Command command)
	{
		if (command.replace(this.command))
			this.command = command;
	}

	private static int previousFrame = 0;
	
	public Unit getYamatoTarget()
	{	
		Game game = Tyr.game;
		if (!game.self().hasResearched(TechType.Yamato_Gun))
		{
			Tyr.bot.drawCircle(unit.getPosition(), Color.Yellow, 6);
			return null;
		}
		
		if (unit.getEnergy() < TechType.Yamato_Gun.energyUsed())
		{
			Tyr.bot.drawCircle(unit.getPosition(), Color.Blue, 6);
			return null;
		}


		boolean first = false;
		if (previousFrame != game.getFrameCount())
		{
			previousFrame = game.getFrameCount();
			first = true;
			Tyr.bot.drawCircle(unit.getPosition(), Color.Red, WeaponType.Yamato_Gun.maxRange());
		}

		Unit target = null;
		int preference = 0;
		
		List<Unit> inRange = game.getUnitsInRadius(unit.getPosition(), WeaponType.Yamato_Gun.maxRange() + 32);
		for(Unit unit : inRange)
		{
			if (!unit.getPlayer().isEnemy(game.self()))
				continue;
			
			UnitType enemyType = unit.getType();
			int newPreference = 0;
			if (enemyType == UnitType.Protoss_Carrier || enemyType == UnitType.Terran_Battlecruiser)
				newPreference = 5;
			else if (enemyType == UnitType.Protoss_Photon_Cannon)
				newPreference = 4;
			else if (enemyType == UnitType.Protoss_High_Templar || enemyType == UnitType.Protoss_Dark_Archon
					 || enemyType == UnitType.Terran_Goliath)
				newPreference = 3;
			else if (enemyType == UnitType.Protoss_Archon
					 || enemyType == UnitType.Terran_Missile_Turret)
				newPreference = 2;
			else if (enemyType == UnitType.Protoss_Dragoon || enemyType == UnitType.Terran_Bunker)
				newPreference = 1;
			
			if (first)
			{
				if(preference == 0)
					Tyr.bot.drawCircle(unit.getPosition(), Color.Red, 6);
				else
					Tyr.bot.drawCircle(unit.getPosition(), Color.Green, 6);
			}
			
			if (newPreference > preference)
			{
				preference = newPreference;
				target = unit;
			}
		}
		
		if (target == null)
		{
			Tyr.bot.drawCircle(unit.getPosition(), Color.Red, 6);
			return null;
		}

		Tyr.bot.drawCircle(unit.getPosition(), Color.Green, 6);
		Tyr.bot.drawCircle(target.getPosition(), Color.Yellow);
		game.drawLineMap(unit.getPosition().getX(), unit.getPosition().getY(), target.getPosition().getX(), target.getPosition().getY(), Color.Yellow);
		return target;
	}
}
