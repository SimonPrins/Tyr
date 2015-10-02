import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;


public class DefensiveTank extends Command 
{
	public Position target;
	public Agent moveAwayTarget;
	boolean sieged = false;
	private static final int maxDist = 16;
	boolean unblockExit = false;
	
	public DefensiveTank(Agent agent, Position target)
	{
		super(agent);
		this.target = target;
	}

	@Override
	public void execute(Game game, Player self, Tyr bot)
	{
		agent.drawCircle(Color.Purple);
		
		game.drawBoxMap(target.getX()-16, target.getY()-16, target.getX() + 16, target.getY() + 16, Color.Purple);
		

		boolean inRange = false;
		for(Unit unit : game.getUnitsInRadius(agent.unit.getPosition(), UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()))
		{
			if(unit.getPlayer().isEnemy(self) && !unit.isLifted() && !unit.getType().isFlyer())
			{
				inRange = true;
				break;
			}
		}
		
		if(agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
		{
			if (inRange || (target != null && agent.distanceSquared(target) <= maxDist*maxDist))
				agent.unit.siege();
			else if (target != null)
				agent.unit.attack(target);
			return;
		}
		else if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
		{
			if (!inRange && target != null && agent.distanceSquared(target) > maxDist*maxDist)
				agent.unit.unsiege();
			return;
		}
	}
	
	@Override
	public boolean replace(Command command) 
	{
		if(!command.getClass().equals(DefensiveTank.class))
			return true;
		if (target == null)
			return false;
		if (((DefensiveTank)command).target == null)
			return true;
		return ((DefensiveTank)command).target.getX() != target.getX() || ((DefensiveTank)command).target.getY() != target.getY();
	}
}
