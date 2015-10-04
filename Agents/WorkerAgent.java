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
import bwapi.Order;
import bwapi.Player;
import bwapi.Unit;


public class WorkerAgent extends Agent
{
	public long resetTimer = -1;
	public boolean isReset = false;
	
	public WorkerAgent(Unit unit)
	{
		super(unit);
	}

	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(isReset && unit.getOrder() == Order.MiningMinerals && unit.getTarget() != null && distanceSquared(unit.getTarget()) <= 50*50)
			isReset = false;
		
		if(game.getFrameCount() == resetTimer)
		{
			resetTimer = -1;
			isReset = true;
		}
		
		if(resetTimer != -1)
			game.drawTextMap(unit.getX(), unit.getY()+10, (resetTimer - game.getFrameCount()) + "");
		
		if(isReset)
		{
			drawCircle(Color.Red, 6);
			if(unit.getOrder() == Order.PlaceBuilding)
			{
				unit.stop();
				System.out.println("Place building cancelled.");
			}
		}
	}
	
	

}
