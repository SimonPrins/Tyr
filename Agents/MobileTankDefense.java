import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;


public class MobileTankDefense extends Command 
{
	public Position rallyPoint = null;
	
	public MobileTankDefense(Agent agent)
	{
		super(agent);
	}

	@Override
	public void execute(Game game, Player self, Tyr bot)
	{
		agent.drawCircle(Color.Yellow);
		
		if (bot.invader == null)
		{
			Position target = getTarget();
			
			if(agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
			{
				if (target != null && agent.distanceSquared(target) <= 10000 && !agent.unit.isHoldingPosition())
					agent.unit.holdPosition();
				else if (target != null && agent.distanceSquared(target) >= 10000)
					agent.unit.attack(target);
			}
			else if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
			{
				agent.unit.unsiege();
			}
			
			return;
		}
		
		int rangeSq = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
		rangeSq = rangeSq * rangeSq;
		
		boolean inrange = false;

		
		for(Unit enemy : game.enemy().getUnits())
		{
			if (enemy.isVisible(self) && !enemy.isLifted() && !enemy.getType().isFlyer() && agent.distanceSquared(enemy) <= rangeSq)
			{
				inrange = true;
				break;
			}
		}
		
		if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
		{
			if (!inrange)
				agent.unit.unsiege();
			return;
		}
		
		if (inrange)
			agent.unit.siege();
		else
			agent.unit.attack(bot.invader.getPosition());
	}
	
	public Position getTarget()
	{
		Position target = null;
		if (Tyr.bot.defensiveStructures.get(0).defenses.size() > 0)
			target = Tyr.bot.defensiveStructures.get(0).defenses.get(0).getPosition();
		else if (target == null)
			target = Tyr.bot.army.rallyPoint;
		return target;
	}

	@Override
	public boolean replace(Command command) 
	{
		return true;
	}
}
