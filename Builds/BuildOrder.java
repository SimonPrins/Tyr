import bwapi.Game;
import bwapi.Player;


public abstract class BuildOrder
{
	public abstract void onFrame(Game game, Player self, Tyr bot);
	
	public boolean overrideStructureOrder(Game game, Player self, Tyr bot, Agent agent)
	{
		return false;
	}
}
