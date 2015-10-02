import bwapi.Game;
import bwapi.Player;


public class BuildDefensive extends Command
{
	public DefensiveStructures defensePos;
	
	public BuildDefensive(Agent agent, DefensiveStructures defensePos)
	{
		super(agent);
		this.defensePos = defensePos;
	}

	@Override
	public void execute(Game game, Player self, Tyr bot) {}

	@Override
	public boolean replace(Command command) 
	{
		return true;
	}
}
