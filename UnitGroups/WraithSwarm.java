import bwapi.Color;
import bwapi.Game;
import bwapi.Player;


public class WraithSwarm extends UnitGroup 
{
	public OutOfJob outOfJob = new OutOfJob();
	
	int bases = 1;
	int nextBase = 0;
	
	public WraithSwarm(OutOfJob rejects)
	{
		super(rejects);
	}
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if(BWTAInitializer.initialized)
		{
			if (bot.scanner.action == Scanner.Eliminating)
			{
				for(Agent agent = pop(); agent != null; agent = pop())
					bot.scanner.addAir(agent);
			}
			for(Agent unit : units)
			{
				unit.drawCircle(Color.Cyan);
				
				WraithAgent agent = (WraithAgent)unit;
				agent.onFrame(game, self, bot);
			}
		}
	}
	
	@Override
	public void add(Agent unit)
	{
		if (bases <= 1)
			bases = Math.max(1, Tyr.bot.suspectedEnemy.size() + Tyr.bot.expands.size());
		
		nextBase--;
		super.add(unit);
		((WraithAgent)unit).current = nextBase;
		outOfJob.add(unit);
		
		if (nextBase < 0)
			nextBase += bases;
	}
}
