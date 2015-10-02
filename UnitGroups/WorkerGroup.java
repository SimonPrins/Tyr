import java.util.ArrayList;

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;


public class WorkerGroup extends UnitGroup
{
	public WorkerGroup(OutOfJob rejects) 
	{
		super(rejects);
	}
	
	ArrayList<MineralWorkers> mineralWorkers = new ArrayList<MineralWorkers>();
	OutOfJob outOfJob = new OutOfJob();

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if (mineralWorkers.size() > 0)
		{
			ArrayList<MineralWorkers> minedOut = new ArrayList<MineralWorkers>();
			for(MineralWorkers mw : mineralWorkers)
				if(mw.minerals.size() == 0)
					minedOut.add(mw);
			
			for(MineralWorkers mw : minedOut)
			{
				mineralWorkers.remove(mw);
				while(mw.units.size() != 0)
				{
					outOfJob.add(mw.units.get(mw.units.size()-1));
					mw.remove(mw.units.size()-1);
				}
			}
			
			int total = 0;
			for (MineralWorkers mw : mineralWorkers)
				if (mw.resourceDepot.isCompleted())
					total++;
			
			int average = 0;
			if(total != 0)
				average = units.size() / total;
			
			
			for(MineralWorkers mw : mineralWorkers)
				while(mw.units.size() > average + 1)
					{
						outOfJob.add(mw.units.get(mw.units.size() - 1));
						mw.remove(mw.units.size()-1);
					}
			
			int filled = 0;
			while(outOfJob.units.size() > 0)
			{
				Agent worker = outOfJob.pop();
				boolean done = false;
				for(;filled < mineralWorkers.size();)
				{
					if (mineralWorkers.get(filled).resourceDepot.isCompleted() && mineralWorkers.get(filled).units.size() < average+1)
					{
						mineralWorkers.get(filled).add(worker);
						done = true;
						break;
					}
					else
						filled++;
				}
				if (!done)
					break;
			}
			
			for(MineralWorkers mw : mineralWorkers)
				mw.onFrame(game, self, bot);
		}
		
		for(Agent agent : outOfJob.units)
			agent.drawCircle(Color.Brown);
	}
	
	@Override
	public Agent pop()
	{
		Agent result = null;
		
		for(MineralWorkers mw : mineralWorkers)
		{
			result = mw.pop();
			if (result != null)
			{
				units.remove(result);
				return result;
			}
		}
		
		return null;
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		for(MineralWorkers base : mineralWorkers)
			base.cleanup();
	}
	
	public Agent pop(Position pos)
	{
		if (pos == null)
		{
			System.out.println("Error, pos is null.");
			return pop();
		}
		
		
		Agent result = null;
		MineralWorkers mwResult = null;
		double distance = Double.MAX_VALUE;
		
		for(MineralWorkers mw : mineralWorkers)
		{
			Agent newWorker = mw.pop(pos);
			if (newWorker == null)
				continue;
			newWorker.drawCircle(Color.Green, 6);
			double newDist = newWorker.distanceSquared(pos);
			if (newDist < distance)
			{
				result = newWorker;
				mwResult = mw;
				distance = newDist;
			}
		}

		if (result != null)
		{
			units.remove(result);
			mwResult.remove(result);
		}
		return result;
	}

	@Override
	public void add(Agent agent)
	{
		super.add(agent);
		outOfJob.add(agent);
	}
	
	public void newBase(Unit base)
	{
		mineralWorkers.add(new MineralWorkers(outOfJob, base));
	}
}
