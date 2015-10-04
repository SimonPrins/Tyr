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


public class WraithSwarm extends UnitGroup 
{
	public OutOfJob outOfJob = new OutOfJob();
	
	int bases = 1;
	int nextBase = 0;
	
	public WraithSwarm(OutOfJob rejects)
	{
		super(rejects);
	}
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if(BWTAInitializer.initialized)
		{
			if (bot.scanner.action == Scanner.Eliminating)
			{
				for(Agent agent = pop(); agent != null; agent = pop())
					bot.scanner.addAir(agent);
			}
			for(Agent unit : units)
			{
				unit.drawCircle(Color.Cyan);
				
				WraithAgent agent = (WraithAgent)unit;
				agent.onFrame(game, self, bot);
			}
		}
	}
	
	@Override
	public void add(Agent unit)
	{
		if (bases <= 1)
			bases = Math.max(1, Tyr.bot.suspectedEnemy.size() + Tyr.bot.expands.size());
		
		nextBase--;
		super.add(unit);
		((WraithAgent)unit).current = nextBase;
		outOfJob.add(unit);
		
		if (nextBase < 0)
			nextBase += bases;
	}
}
