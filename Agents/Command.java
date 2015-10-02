import bwapi.Game;
import bwapi.Player;


public abstract class Command
{
	protected Agent agent;
	public Command(Agent agent)
	{
		this.agent = agent;
	}
	
	public abstract void execute(Game game, Player self, Tyr bot);

	public abstract boolean replace(Command command);
}
