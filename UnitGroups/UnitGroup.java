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


public abstract class UnitGroup 
{
	ArrayList<Agent> units = new ArrayList<Agent>();
	OutOfJob rejects;
	
	public UnitGroup(OutOfJob rejects)
	{
		this.rejects = rejects;
	}
	
	public void cleanup()
	{
		for(int i=0; i<units.size(); i++)
			if(units.get(i) == null || units.get(i).isDead())
			{
				units.remove(i);
				i--;
			}
	}
	
	public abstract void onFrame(Game game, Player self, Tyr bot);
	
	public void add(Agent agent)
	{
		units.add(agent);
	}
	
	public void remove(Agent agent)
	{
		units.remove(agent);
	}

	public void remove(int pos) 
	{
		units.remove(pos);
	}
	
	public Agent pop()
	{
		if(units.size() == 0)
			return null;
		Agent result = units.get(units.size()-1);
		remove(units.size()-1);
		return result;
	}
}
