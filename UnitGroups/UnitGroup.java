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
