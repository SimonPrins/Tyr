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
