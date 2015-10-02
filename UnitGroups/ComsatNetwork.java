import java.util.ArrayList;

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;


public class ComsatNetwork extends UnitGroup
{
	ArrayList<ScanEvent> scanEvents = new ArrayList<ScanEvent>();
	
	public ComsatNetwork(OutOfJob rejects) 
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		Position target = null;
		
		for(int i=0; i<scanEvents.size(); i++)
		{
			if (game.getFrameCount() - scanEvents.get(i).time >= 15*15)
			{
				scanEvents.remove(i);
				i--;
			}
			else
				bot.drawCircle(scanEvents.get(i).pos, Color.Orange, 320);
		}
		
		for(Unit enemy : game.enemy().getUnits())
		{
			if (! enemy.isDetected() && enemy.getType() != UnitType.Protoss_Observer)
			{
				boolean alreadyScanned = false;
				for(ScanEvent event : scanEvents)
					if (event.inRange(enemy.getPosition()))
						alreadyScanned = true;
				if(alreadyScanned)
					break;
				target = enemy.getPosition();
				break;
			}
		}
		
		if(target != null)
		{
			bot.drawCircle(target, Color.Orange);
			for(Agent myUnit : units)
			{
				if (myUnit.unit.getEnergy() >= 50)
				{
					scanEvents.add(new ScanEvent(target, game.getFrameCount()));
					
					myUnit.unit.useTech(TechType.Scanner_Sweep, target);
					break;
				}
			}
		}
		
	}
	
}
