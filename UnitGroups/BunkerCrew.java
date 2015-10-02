import java.util.ArrayList;
import bwapi.Color;
import bwapi.Game;
import bwapi.Player;


public class BunkerCrew extends UnitGroup
{
	Agent bunker;

	ArrayList<Agent> defenders = new ArrayList<Agent>();
	private ArrayList<Agent> repairCrew = new ArrayList<Agent>();

	public BunkerCrew(OutOfJob rejects)
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(((BunkerAgent)bunker).disabled)
		{
			bunker.unit.unloadAll();
			bunker = null;
		}
		
		if (bunker == null || bunker.isDead())
		{
			bunker = null;
			
			for(Agent def : defenders) rejects.add(def);
			defenders = new ArrayList<Agent>();

			for(Agent scv : repairCrew) rejects.add(scv);
			repairCrew = new ArrayList<Agent>();
			
			return;
		}
		
		for(Agent def : defenders)
		{
			def.drawCircle(Color.Purple);
			if(def.unit.isIdle() || ! def.unit.isMoving())
				bunker.unit.load(def.unit);
		}
	}
	
	public void addRepairSCV(Agent scv)
	{
		repairCrew.add(scv);
		scv.order(new RepairBunker(scv, bunker));
	}
	
	public Agent removeRepairSCV()
	{
		Agent result = repairCrew.get(repairCrew.size()-1);
		repairCrew.remove(repairCrew.size()-1);
		result.order(new None(result));
		return result;
	}
	
	public int repairingSCVCount()
	{
		return repairCrew.size();
	}
	
	public void addDefender(Agent def)
	{
		if (bunker != null)
		{
			def.order(new None(def));
			defenders.add(def);
			def.unit.stop();
			bunker.unit.load(def.unit);
		}
		else
			rejects.add(def);
	}

	@Override
	public void cleanup()
	{
		super.cleanup();
		for(int i=0; i<repairCrew.size(); i++)
		{
			Agent worker = repairCrew.get(i); 
			if(worker.isDead())
			{
				repairCrew.remove(i);
				rejects.add(worker);
				i--;
			}
		}
	}
}
