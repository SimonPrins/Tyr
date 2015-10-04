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
import bwapi.UnitType;


public class Bunkers extends UnitGroup
{
	ArrayList<BunkerCrew> bunkers = new ArrayList<BunkerCrew>();
	public boolean disabled;

	public int minimumRepair = 1;
	public int maximumRepair = 3;
	
	public Bunkers(OutOfJob rejects)
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if (disabled)
		{
			for(int i=bunkers.size()-1; i>=0; i--)
			{
				BunkerCrew crew = bunkers.get(i);
				while (crew.repairingSCVCount() > 0)
					rejects.add(crew.removeRepairSCV());

				crew.bunker.unit.unloadAll();
				
				for (int j=crew.defenders.size()-1; j>=0; j--)
				{
					Agent defender = crew.defenders.get(j);
					rejects.add(defender);
					crew.defenders.remove(j);
				}
			}
			return;
		}
		
		int requiredRepair = (bot.invader == null || (bot.invaderCount == 1 && bot.invader.getType().isWorker()))?minimumRepair:maximumRepair;
		
		
		ArrayList<BunkerCrew> destroyedBunkers = new ArrayList<BunkerCrew>();
		for(BunkerCrew crew : bunkers)
		{
			if (crew.bunker == null && crew.defenders.size() == 0 && crew.repairingSCVCount() == 0)
			{
				destroyedBunkers.add(crew);
				continue;
			}
			
			while(crew.defenders.size() < 4)
			{
				Agent def = bot.army.pop();
				if(def == null)
					break;
				if(def.unit.getType() != UnitType.Terran_Marine)
					rejects.add(def);
				else
					crew.addDefender(def);
			}
			
			while(crew.repairingSCVCount() < requiredRepair)
			{
				Agent scv = bot.workForce.pop(crew.bunker.unit.getPosition());
				if(scv == null)
					return;
				crew.addRepairSCV(scv);
			}
			
			while(crew.repairingSCVCount() > requiredRepair)
				rejects.add(crew.removeRepairSCV());
			
			crew.onFrame(game, self, bot);
		}
		
		for(BunkerCrew crew : destroyedBunkers)
			bunkers.remove(crew);
	}

	@Override
	public void add(Agent agent)
	{
		super.add(agent);
		BunkerCrew crew = new BunkerCrew(rejects);
		crew.bunker = agent;
		bunkers.add(crew);
	}
	
	public boolean mannedBunkerExists()
	{
		for (BunkerCrew bunker : bunkers)
		{
			if (bunker.defenders.size() >= 4)
				return true;
		}
		return false;
	}

	public boolean bunkersAreManned() 
	{
		for (BunkerCrew bunker : bunkers)
		{
			if (bunker.defenders.size() < 4)
				return false;
		}
		return true;
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		for(BunkerCrew bunker : bunkers)
			bunker.cleanup();
	}
}
