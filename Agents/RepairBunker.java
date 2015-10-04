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
import bwapi.Unit;
import bwapi.UnitType;


public class RepairBunker extends Command
{
	private Agent bunker;
	private Unit repairTarget;
	public RepairBunker(Agent agent, Agent bunker) 
	{
		super(agent);
		this.bunker = bunker;
	}

	@Override
	public void execute(Game game, Player self, Tyr bot) 
	{
		agent.drawCircle(Color.Purple);
		
		if (bunker.unit.getHitPoints() < UnitType.Terran_Bunker.maxHitPoints())
		{
			if (!agent.unit.isRepairing())
				agent.unit.repair(bunker.unit);
			return;
		}
		
		if (bot.invader == null)
		{
			if (repairTarget != null)
			{
				if(!repairTarget.exists()
						|| repairTarget.getHitPoints() <= 0
						|| repairTarget.getRemoveTimer() != 0
						|| bunker.distanceSquared(repairTarget) > 360*360 
						|| repairTarget.getHitPoints() >= repairTarget.getType().maxHitPoints()
						|| repairTarget.getPlayer() != self)
					repairTarget = null;
				else 
				{
					//if (!agent.unit.isRepairing() || agent.unit.getTarget() != repairTarget)
					//	agent.unit.repair(repairTarget);
					return;
				}
			}
			List<Unit> inRange = game.getUnitsInRadius(bunker.unit.getPosition(), 360);
			for(Unit unit : inRange)
			{
				if(unit.getPlayer() == self 
						&& (unit.getType().isBuilding() || unit.getType().isMechanical())
						&& unit.getHitPoints() < unit.getType().maxHitPoints()
						&& unit.isCompleted())
				{
					repairTarget = unit;
					agent.unit.repair(unit);
					return;
				}
			}
		}
		
		
		if (agent.distanceSquared(bunker) >= 64*64)
			agent.unit.move(bunker.unit.getPosition());
	}

	@Override
	public boolean replace(Command command)
	{
		if (command.getClass() != RepairBunker.class)
			return true;
		return ((RepairBunker)command).bunker != bunker;
	}
	
}
