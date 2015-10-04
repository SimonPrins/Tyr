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

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.UnitType;


public class AntiCannonPush extends Command
{
	private Position target;
	
	public AntiCannonPush(Agent agent, Position target) 
	{
		super(agent);
		this.target = target;
	}

	@Override
	public void execute(Game game, Player self, Tyr bot) 
	{
		if(agent.unit.isIdle() || bot.defenseTime == 0)
			agent.unit.attack(target);
		
		if(agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
		{
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
			{
				if(enemy.type == UnitType.Protoss_Photon_Cannon
						&& agent.distanceSquared(enemy.pos) <= 
						UnitType.Terran_Siege_Tank_Siege_Mode.sightRange()*UnitType.Terran_Siege_Tank_Siege_Mode.sightRange())
				{
					agent.unit.siege();
					break;
				}
			}
		}
		else if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
		{
			boolean inrange = false;
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
			{
				if(enemy.type == UnitType.Protoss_Photon_Cannon
						&& agent.distanceSquared(enemy.pos) <= 
								UnitType.Terran_Siege_Tank_Siege_Mode.sightRange()*UnitType.Terran_Siege_Tank_Siege_Mode.sightRange())
				{
					inrange = true;
					break;
				}
			}
			
			if(!inrange)
				agent.unit.unsiege();
		}
	}

	@Override
	public boolean replace(Command command)
	{
		if (!command.getClass().equals(AntiCannonPush.class))
			return true;
		
		return ((AntiCannonPush)command).target.getX() == target.getX() && ((AntiCannonPush)command).target.getY() == target.getY();
	}

}
