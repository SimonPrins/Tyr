import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;


public class GasWorkers extends UnitGroup
{
	Unit geyser;
	

	public GasWorkers(OutOfJob rejects, Unit geyser) 
	{
		super(rejects);
		this.geyser = geyser;        
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		game.drawTextMap(geyser.getX(), geyser.getY(), "" + units.size());
		
		if (geyser.getResources() == 0)
			return;
		
		for(Agent worker : units)
		{
			worker.drawCircle(Color.Green);
			if(worker.unit.isIdle() || worker.unit.isGatheringMinerals())
				worker.unit.gather(geyser, false);
		}
		
		while(units.size() > bot.workersPerGas)
		{
			rejects.add(units.get(units.size()-1));
			units.remove(units.size()-1);
		}
	}

	@Override
	public void add(Agent agent)
	{
		super.add(agent);

		if(agent.unit.isConstructing())
			return;
		
		agent.unit.gather(geyser, false);
	}
}
