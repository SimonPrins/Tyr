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

import bwapi.TilePosition;
import bwapi.UnitType;


public class BuildCommand 
{
	public Agent worker;
	public UnitType building;
	public TilePosition position;
	public boolean fixed;
	
	public boolean removing = false;
	
	public BuildCommand(Agent worker, UnitType building, TilePosition position)
	{
		this.worker = worker;
		this.building = building;
		this.position = position;
		
		WorkerAgent wa = (WorkerAgent)worker;
		wa.resetTimer = getTimer();
	}
	
	public BuildCommand(Agent worker, UnitType building, TilePosition position, boolean fixed)
	{
		this.worker = worker;
		this.building = building;
		this.position = position;
		this.fixed = fixed;
		
		WorkerAgent wa = (WorkerAgent)worker;
		wa.resetTimer = getTimer();
	}
	
	public int mineralCost()
	{
		return building.mineralPrice();
	}
	
	public int gasCost()
	{
		return building.gasPrice();
	}
	
	public void remove(Tyr bot)
	{
		if(removing)
			return;
		
		removing = true;
		
		bot.reservedMinerals -= mineralCost();
		bot.reservedGas -= gasCost();

		
		if (!worker.isDead())
		{
			if(((WorkerAgent)worker).resetTimer == Tyr.game.getFrameCount())
				((WorkerAgent)worker).isReset = true;
			
			if(((WorkerAgent)worker).isReset)
				worker.unit.stop();
			((WorkerAgent)worker).resetTimer = -1;
			worker.order(new None(worker));
		}
	}

	public long getTimer() 
	{
		return Tyr.game.getFrameCount() + 400 + worker.unit.getDistance(Tyr.tileToPostion(position))/4;
	}
}
