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
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;


public class ScoutGroup extends UnitGroup
{
	int scoutBase = 0;
	public int opponentStrategy = 0;
	
	public static int unknown = 0;
	public static int zealotPush = 1;
	public static int cannons = 2;
	public static int tech = 3;
	public static int defensive = 4;
	public static int besiege = 5;
	
	int scoutCount = 1;
	
	public int workerScoutTiming = -1;
	public int nscouts = 0;
	private boolean scoutsSent = false;
	
	public ScoutGroup(OutOfJob rejects) 
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(game.enemy().getRace() == Race.Protoss)
		{
			if(opponentStrategy != tech && opponentStrategy != cannons)
			{
				int nexusCount = 0;
				for(EnemyPosition enemy : bot.enemyBuildingMemory)
				{
					if (enemy.type == UnitType.Protoss_Nexus)
						nexusCount++;
				}
				if(nexusCount >= 2)
					opponentStrategy = tech;
				for(Unit enemy : game.enemy().getUnits())
				{
					if(enemy.getType().gasPrice() > 0 
							|| enemy.getType() == UnitType.Protoss_Assimilator 
							|| enemy.getType() == UnitType.Protoss_Cybernetics_Core)
						opponentStrategy = tech;
				}
			}
			if (game.getFrameCount() <= 5200 && opponentStrategy != tech && opponentStrategy != cannons)
			{
				int gatewayCount = 0;
				for(EnemyPosition enemy : bot.enemyBuildingMemory)
				{
					if (enemy.type == UnitType.Protoss_Photon_Cannon || enemy.type == UnitType.Protoss_Forge)
						opponentStrategy = cannons;
					if (enemy.type == UnitType.Protoss_Gateway)
						gatewayCount++;
				}
				if(gatewayCount >= 2)
					opponentStrategy = zealotPush;
			}
		}
		if(game.enemy().getRace() == Race.Terran)
		{
			if (opponentStrategy != defensive)
			{
				for(EnemyPosition enemy : bot.enemyBuildingMemory)
					if (enemy.type == UnitType.Terran_Bunker)
						opponentStrategy = defensive;
				
				int tankCount = 0;
				for(Unit enemy : game.enemy().getUnits())
				{
					if(enemy.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
							|| enemy.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
						tankCount++;
				}
				if(tankCount >= 3)
					opponentStrategy = defensive;
			}
		}
		
		if (!scoutsSent && game.getFrameCount() >= workerScoutTiming && workerScoutTiming != -1)
		{
			scoutsSent = true;
			for(int i=0; i<nscouts; i++)
				requestWorkerScout(bot);
		}
		
		if (bot.suspectedEnemy.size() > 1)
		{
			ArrayList<BaseLocation> cleared = new ArrayList<BaseLocation>();
			
			BaseLocation enemyBase = null;
			for(BaseLocation se : bot.suspectedEnemy)
			{
				for(EnemyPosition p : bot.enemyBuildingMemory)
				{
					if (se.getPosition().distanceTo(p.pos.getX(), p.pos.getY()) <= 256)
					{
						enemyBase = se;
						break;
					}
				}
				if (enemyBase != null)
					break;
				
				if (Tyr.game.isVisible(se.getTilePosition()))
					cleared.add(se);
			}
			
			if (enemyBase != null)
			{
				for(int i=bot.suspectedEnemy.size()-1; i>= 0; i--)
				{
					if (bot.suspectedEnemy.get(i) != enemyBase)
					{
						bot.expands.add(bot.suspectedEnemy.get(i));
						bot.suspectedEnemy.remove(i);
					}
				}
			}
			else
			{
				for(BaseLocation se : cleared)
				{
					bot.expands.add(se);
					bot.suspectedEnemy.remove(se);
				}
			}
		}
		
		ArrayList<Agent> done = new ArrayList<Agent>();
		for(Agent scout :units)
		{	
			scout.drawCircle(Color.Purple);
	        
			if (scout.command.getClass() == KillWorkers.class)
			{
				if (((KillWorkers)scout.command).breakOff)
				{
					scout.order(new None(scout));
					done.add(scout);
				}
				
				continue;
			}
			
			if (!scout.unit.isIdle())continue;
			
			ArrayList<BaseLocation> scoutLocations = null;
			
			if (bot.suspectedEnemy.size() > 1 || scout.unit.getType().isWorker())
				scoutLocations = bot.suspectedEnemy;
			else
				scoutLocations = bot.expands;
			
			if (scoutLocations.size() == 0)
				continue;
			int fullcycle = 0;
			while(fullcycle < 2)
			{
				scoutBase++;
				if (scoutBase >= scoutLocations.size())
				{
					scoutBase = 0;
					fullcycle++;
				}
				if (!Tyr.game.isVisible(scoutLocations.get(scoutBase).getTilePosition()))
					break;
			}
			
			BaseLocation b = scoutLocations.get(scoutBase);
			if(!scout.unit.getType().isWorker())
				scout.unit.attack(b.getPosition());
			else
				scout.unit.move(b.getPosition());
			
			if(scout.unit.getType().isWorker() && game.isVisible(b.getTilePosition()) && bot.suspectedEnemy != null 
					&& bot.suspectedEnemy.size() <= 1 && scout.distanceSquared(b.getPosition()) <= 128*128)
			{
				scout.order(new KillWorkers(scout));
			}
		}
		
		for (Agent scout : done)
		{
			rejects.add(scout);
			units.remove(scout);
		}
	}
	
	public void requestWorkerScout(Tyr bot)
	{
		Agent scout = bot.workForce.pop();
		if(scout != null)
		{
			units.add(scout);
			scout.unit.stop();
		}
	}

	public String getEnemyStrategy() 
	{
		if(opponentStrategy == zealotPush)
			return "Zealot push";
		if(opponentStrategy == cannons)
			return "Cannons";
		return "Unknown";
	}

}
