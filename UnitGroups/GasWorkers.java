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
import bwapi.Unit;


public class GasWorkers extends UnitGroup
{
	Unit geyser;
	

	public GasWorkers(OutOfJob rejects, Unit geyser) 
	{
		super(rejects);
		this.geyser = geyser;        
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		game.drawTextMap(geyser.getX(), geyser.getY(), "" + units.size());
		
		if (geyser.getResources() == 0)
			return;
		
		for(Agent worker : units)
		{
			worker.drawCircle(Color.Green);
			if(worker.unit.isIdle() || worker.unit.isGatheringMinerals())
				worker.unit.gather(geyser, false);
		}
		
		while(units.size() > bot.workersPerGas)
		{
			rejects.add(units.get(units.size()-1));
			units.remove(units.size()-1);
		}
	}

	@Override
	public void add(Agent agent)
	{
		super.add(agent);

		if(agent.unit.isConstructing())
			return;
		
		agent.unit.gather(geyser, false);
	}
}
