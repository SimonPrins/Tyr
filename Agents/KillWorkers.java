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

import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;


public class KillWorkers extends Command 
{
	private Unit enemyWorker;
	public boolean breakOff = false;
	
	public KillWorkers(Agent agent)
	{
		super(agent);
	}

	@Override
	public void execute(Game game, Player self, Tyr bot)
	{
		if (agent.unit.isUnderAttack())
			breakOff = true;
		
		if (enemyWorker != null)
		{
			if (enemyWorker.getHitPoints() <= 0 || enemyWorker.getRemoveTimer() != 0 || !enemyWorker.exists())
				enemyWorker = null;
			
			if (enemyWorker != null && agent.distanceSquared(enemyWorker) >= 320*320)
				enemyWorker = null;
		}
		
		if (enemyWorker == null)
		{
			findWorker(game);
		
			if (enemyWorker != null)
				agent.unit.attack(enemyWorker);
		}
	}

	private void findWorker(Game game) 
	{
		List<Unit> enemies = game.enemy().getUnits();
		
		double bestDistance = Integer.MAX_VALUE;
		
		for(Unit enemy : enemies)
		{
			if (!(enemy.getType().isWorker()))
				continue;
			
			double newDist = agent.distanceSquared(enemy);
			
			if (enemyWorker == null || newDist < bestDistance)
			{
				enemyWorker = enemy;
				bestDistance = newDist;
			}
		}
	}

	@Override
	public boolean replace(Command command) 
	{
		return true;
	}
}
