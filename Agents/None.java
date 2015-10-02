import bwapi.Game;
import bwapi.Player;


public class None extends Command
{
	public None(Agent agent)
	{
		super(agent);
	}
	
	@Override
	public void execute(Game game, Player self, Tyr bot) {}

	@Override
	public boolean replace(Command command) 
	{
		return true;
	}

}
