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


public class BuildDefensive extends Command
{
	public DefensiveStructures defensePos;
	
	public BuildDefensive(Agent agent, DefensiveStructures defensePos)
	{
		super(agent);
		this.defensePos = defensePos;
	}

	@Override
	public void execute(Game game, Player self, Tyr bot) {}

	@Override
	public boolean replace(Command command) 
	{
		return true;
	}
}
