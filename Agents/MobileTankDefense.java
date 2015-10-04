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


public class MobileTankDefense extends Command 
{
	public Position rallyPoint = null;
	
	public MobileTankDefense(Agent agent)
	{
		super(agent);
	}

	@Override
	public void execute(Game game, Player self, Tyr bot)
	{
		agent.drawCircle(Color.Yellow);
		
		if (bot.invader == null)
		{
			Position target = getTarget();
			
			if(agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
			{
				if (target != null && agent.distanceSquared(target) <= 10000 && !agent.unit.isHoldingPosition())
					agent.unit.holdPosition();
				else if (target != null && agent.distanceSquared(target) >= 10000)
					agent.unit.attack(target);
			}
			else if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
			{
				agent.unit.unsiege();
			}
			
			return;
		}
		
		int rangeSq = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
		rangeSq = rangeSq * rangeSq;
		
		boolean inrange = false;

		
		for(Unit enemy : game.enemy().getUnits())
		{
			if (enemy.isVisible(self) && !enemy.isLifted() && !enemy.getType().isFlyer() && agent.distanceSquared(enemy) <= rangeSq)
			{
				inrange = true;
				break;
			}
		}
		
		if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
		{
			if (!inrange)
				agent.unit.unsiege();
			return;
		}
		
		if (inrange)
			agent.unit.siege();
		else
			agent.unit.attack(bot.invader.getPosition());
	}
	
	public Position getTarget()
	{
		Position target = null;
		if (Tyr.bot.defensiveStructures.get(0).defenses.size() > 0)
			target = Tyr.bot.defensiveStructures.get(0).defenses.get(0).getPosition();
		else if (target == null)
			target = Tyr.bot.army.rallyPoint;
		return target;
	}

	@Override
	public boolean replace(Command command) 
	{
		return true;
	}
}
