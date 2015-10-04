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

import bwapi.Game;
import bwapi.Player;


public class DefendingWorkers extends UnitGroup
{
	public DefendingWorkers(OutOfJob rejects) 
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(bot.invaderCount == 0 || bot.army.units.size() >= 5 || (bot.bunkers.mannedBunkerExists() && bot.army.units.size() >= 2) 
				|| bot.invader.distanceTo(self.getStartLocation().getX()*32, self.getStartLocation().getY()*32) >= 480)
		{
			for(Agent agent : units)
				rejects.add(agent);
			if (units.size() != 0)
				units = new ArrayList<Agent>();
			return;
		}
		
		if (bot.army.units.size() >= 5 || (bot.bunkers.mannedBunkerExists() && bot.army.units.size() >= 2) 
				|| bot.invader.distanceTo(self.getStartLocation().getX()*32, self.getStartLocation().getY()*32) >= 480)
			return;
		
		int desiredDefenders = 0;
		int halfWorkers = (units.size() + bot.workForce.units.size())/2;
		if(bot.invaderCount == 1 && !bot.invader.getType().isFlyer())
		{
			if(bot.invader.getType().isWorker())
				desiredDefenders = Math.min(1, halfWorkers);
			else
				desiredDefenders = Math.min(3, halfWorkers);
		}
		else if (bot.invaderCount == 2)
			desiredDefenders = Math.min(5, halfWorkers);
		else
			desiredDefenders = halfWorkers;
		
		while(units.size() < desiredDefenders)
			units.add(bot.workForce.pop(bot.invader.getPosition()));
		while(units.size() > desiredDefenders)
		{
			rejects.add(units.get(units.size()-1));
			units.remove(units.size()-1);
		}
		
		if (bot.areWeBeingInvaded && bot.invader != null)
			for(Agent worker : units)
				if(worker != null)
				{
					if(bot.invaderCount > 1)
						worker.unit.attack(bot.invader.getPosition());
					else
						worker.unit.attack(bot.invader);
				}
	}

}
