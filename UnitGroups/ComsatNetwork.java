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
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;


public class ComsatNetwork extends UnitGroup
{
	ArrayList<ScanEvent> scanEvents = new ArrayList<ScanEvent>();
	
	public ComsatNetwork(OutOfJob rejects) 
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		Position target = null;
		
		for(int i=0; i<scanEvents.size(); i++)
		{
			if (game.getFrameCount() - scanEvents.get(i).time >= 15*15)
			{
				scanEvents.remove(i);
				i--;
			}
			else
				bot.drawCircle(scanEvents.get(i).pos, Color.Orange, 320);
		}
		
		for(Unit enemy : game.enemy().getUnits())
		{
			if (! enemy.isDetected() && enemy.getType() != UnitType.Protoss_Observer)
			{
				boolean alreadyScanned = false;
				for(ScanEvent event : scanEvents)
					if (event.inRange(enemy.getPosition()))
						alreadyScanned = true;
				if(alreadyScanned)
					break;
				target = enemy.getPosition();
				break;
			}
		}
		
		if(target != null)
		{
			bot.drawCircle(target, Color.Orange);
			for(Agent myUnit : units)
			{
				if (myUnit.unit.getEnergy() >= 50)
				{
					scanEvents.add(new ScanEvent(target, game.getFrameCount()));
					
					myUnit.unit.useTech(TechType.Scanner_Sweep, target);
					break;
				}
			}
		}
		
	}
	
}
