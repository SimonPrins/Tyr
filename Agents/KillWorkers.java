import java.util.List;

import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;


public class KillWorkers extends Command 
{
	private Unit enemyWorker;
	public boolean breakOff = false;
	
	public KillWorkers(Agent agent)
	{
		super(agent);
	}

	@Override
	public void execute(Game game, Player self, Tyr bot)
	{
		if (agent.unit.isUnderAttack())
			breakOff = true;
		
		if (enemyWorker != null)
		{
			if (enemyWorker.getHitPoints() <= 0 || enemyWorker.getRemoveTimer() != 0 || !enemyWorker.exists())
				enemyWorker = null;
			
			if (enemyWorker != null && agent.distanceSquared(enemyWorker) >= 320*320)
				enemyWorker = null;
		}
		
		if (enemyWorker == null)
		{
			findWorker(game);
		
			if (enemyWorker != null)
				agent.unit.attack(enemyWorker);
		}
	}

	private void findWorker(Game game) 
	{
		List<Unit> enemies = game.enemy().getUnits();
		
		double bestDistance = Integer.MAX_VALUE;
		
		for(Unit enemy : enemies)
		{
			if (!(enemy.getType().isWorker()))
				continue;
			
			double newDist = agent.distanceSquared(enemy);
			
			if (enemyWorker == null || newDist < bestDistance)
			{
				enemyWorker = enemy;
				bestDistance = newDist;
			}
		}
	}

	@Override
	public boolean replace(Command command) 
	{
		return true;
	}
}
