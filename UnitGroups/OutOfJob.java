import bwapi.Color;
import bwapi.Game;
import bwapi.Player;


public class OutOfJob extends UnitGroup
{
	public OutOfJob() 
	{
		super(null);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		for(Agent agent : units)
			agent.drawCircle(Color.Black);
	}

}
