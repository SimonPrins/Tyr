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
import bwapi.Race;


public class UnderConstruction extends UnitGroup
{
	ArrayList<RepairCommand> emergencyCommands = new ArrayList<RepairCommand>();
	
	public UnderConstruction(OutOfJob rejects)
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		ArrayList<RepairCommand> removeRepairs = new ArrayList<RepairCommand>();
		for(RepairCommand repair : emergencyCommands)
		{
			repair.target.drawCircle(Color.Teal);
			repair.worker.drawCircle(Color.Teal);
			
			if (repair.target.isDead())
			{
				removeRepairs.add(repair);
				continue;
			}
			
			if (repair.target.unit.isCompleted() || repair.worker.isDead())
			{
				removeRepairs.add(repair);
				units.add(repair.target);
				continue;
			}
		}
		
		for(RepairCommand repair : removeRepairs)
		{
			emergencyCommands.remove(repair);
			if (!repair.worker.isDead())
				rejects.add(repair.worker);
		}
		
		ArrayList<Agent> completed = new ArrayList<Agent>();
		ArrayList<Agent> repairing = new ArrayList<Agent>();
		for(Agent building : units)
		{
			if(building.unit.isCompleted())
			{
				completed.add(building);
				continue;
			}
			building.drawCircle(Color.White);
			
			if(self.getRace() == Race.Terran && building.unit.getBuildUnit() == null)
			{
				Agent worker = bot.workForce.pop(building.unit.getPosition());
				if(worker == null)
					continue;
				
				worker.unit.rightClick(building.unit);
				RepairCommand emergencyCommand = new RepairCommand(worker, building);
				emergencyCommands.add(emergencyCommand);
				repairing.add(building);
			}
		}
		
		for(Agent building : repairing)
			units.remove(building);
			
		for(Agent building : completed)
		{
			units.remove(building);
			rejects.add(building);
		}
	}
}
