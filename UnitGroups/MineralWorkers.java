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


public class MineralWorkers extends UnitGroup
{
	Unit resourceDepot;
	ArrayList<Unit> minerals = new ArrayList<Unit>();
	GasWorkers gasWorkers;
	int currentMineral = -1;
	
	public Position defensePos;
	DefensiveStructures defenses = null;

	public MineralWorkers(OutOfJob rejects, Unit resourceDepot) 
	{
		super(rejects);
		this.resourceDepot = resourceDepot;
		
		for (Unit neutralUnit : Tyr.game.neutral().getUnits()) 
		{
            if (neutralUnit.getType().isMineralField() && neutralUnit.getDistance(resourceDepot) <= 270) 
                minerals.add(neutralUnit);
            else if (neutralUnit.getType() == UnitType.Resource_Vespene_Geyser
            		&& neutralUnit.getDistance(resourceDepot) <= 270)
            	gasWorkers = new GasWorkers(rejects, neutralUnit);
		}
        
		if (resourceDepot == null || resourceDepot.getDistance(Tyr.tileToPostion(Tyr.game.self().getStartLocation())) < 64)
		{
			defensePos = null;
		}
		else
		{
			int totalX = 0;
			int totalY = 0;
			
			for(Unit mineral : minerals)
			{
				totalX += mineral.getPosition().getX();
				totalY += mineral.getPosition().getY();
			}
			if (minerals.size() != 0)
				defensePos = new Position(totalX / minerals.size(), totalY / minerals.size());
			else
			{
				defensePos = new Position(resourceDepot.getPosition().getX(), resourceDepot.getPosition().getY());
			}
			defensePos = new Position(2*resourceDepot.getPosition().getX() - defensePos.getX(),
					2*resourceDepot.getPosition().getY() - defensePos.getY());
		}

		boolean exists = false;
		for(DefensiveStructures structures : Tyr.bot.defensiveStructures)
		{
			if (resourceDepot.getDistance(structures.defendedPosition) < 64)
			{
				exists = true;
				defenses = structures;
				break;
			}
		}
		if (!exists)
		{
			defenses = new DefensiveStructures(resourceDepot.getPosition(), defensePos);
			Tyr.bot.defensiveStructures.add(defenses);
		}
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		for (Unit neutralUnit : Tyr.game.neutral().getUnits()) 
		{
            if (neutralUnit.getType().isMineralField() && neutralUnit.getDistance(resourceDepot) <= 270)
            {
                if (!minerals.contains(neutralUnit))
                	minerals.add(neutralUnit);
            }
            else if (gasWorkers == null && neutralUnit.getType() == UnitType.Resource_Vespene_Geyser
            		&& neutralUnit.getDistance(resourceDepot) <= 270)
            	gasWorkers = new GasWorkers(rejects, neutralUnit);
		}
		
		if (defensePos != null)
			bot.drawCircle(defensePos, Color.Green, 64);
		
		for(int i=minerals.size()-1; i>=0; i--)
		{
			if (!minerals.get(i).exists())
				minerals.remove(i);
			else
				bot.drawCircle(minerals.get(i).getPosition(), Color.Teal, 16);
		}
		
		if (minerals.size() == 0)
		{
			if(defenses != null && defenses.defendedPosition.getDistance(Tyr.tileToPostion(self.getStartLocation())) >= 200)
			{
				defenses.disable();
				defenses = null;
			}
			return;
		}
		
		for(Agent worker : units)
		{
			worker.drawCircle(Color.Cyan);
			if(worker.unit.isIdle() || worker.unit.isGatheringGas())
				worker.unit.gather(minerals.get(incrementMineralNode()), false);
		}
		
		if(gasWorkers != null && gasWorkers.geyser.getResources() == 0
				&& gasWorkers.geyser.getType().isRefinery())
		{
			for(Agent unit : gasWorkers.units)
				add(unit);
			
			gasWorkers = null;
		}
		else if(gasWorkers != null)
		{
			while (gasWorkers.geyser.isCompleted() && gasWorkers.units.size() < bot.workersPerGas
					&& gasWorkers.geyser.getType().isRefinery())
			{
				Agent gasWorker = pop(gasWorkers.geyser.getPosition());
				if(gasWorker == null)
					break;
				remove(gasWorker);
				gasWorkers.add(gasWorker);
			}
			gasWorkers.onFrame(game, self, bot);
		}
		
		
	}
	
	private int incrementMineralNode() 
	{
		currentMineral++;
		currentMineral %= minerals.size();
		return currentMineral;
	}

	@Override
	public Agent pop()
	{
		for(int i=units.size()-1; i>= 0; i--)
		{
			if (!((WorkerAgent)units.get(i)).isReset && !units.get(i).unit.isConstructing() && units.get(i).distanceSquared(resourceDepot.getPosition()) <= 1152*1152)
			{
				Agent result = units.get(i);
				remove(i);
				return result;
			}
		}
		return null;
	}

	public Agent pop(Position pos) 
	{
		Agent result = null;
		double distance = Double.MAX_VALUE;
		for(Agent worker : units)
		{
			if (((WorkerAgent)worker).isReset || worker.unit.isConstructing())
				continue;
			
			int newDist = worker.distanceSquared(pos);
			if(newDist < distance)
			{
				distance = newDist;
				result = worker;
			}
		}
		return result;
	}

	@Override
	public void add(Agent agent)
	{
		if (agent == null)
			return;
		if(agent.unit == null)
			return;
		super.add(agent);

		if(agent.unit.isConstructing())
			return;
		
		if(minerals.size() != 0)
			agent.unit.gather(minerals.get(incrementMineralNode()), false);
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		
		for(int i=minerals.size()-1; i >= 0; i--)
		{
			if (!minerals.get(i).exists() || minerals.get(i).getResources() <= 0)
			{
				minerals.remove(i);
			}
		}
		
		if (minerals.size() == 0)
		{
			for(Agent unit : units)
				rejects.add(unit);
			units = new ArrayList<Agent>();
		}
		
		if (gasWorkers != null)
			gasWorkers.cleanup();
	}
}
