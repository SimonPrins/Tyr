import java.util.List;

import bwapi.Color;
import bwapi.Game;
import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;


public class BCDefend extends Command
{
	private Position target;
	private Unit killTarget;
	
	private static final boolean stutterstep = false;
	
	public BCDefend(Agent agent, Position target)
	{
		super(agent);
		this.target = target;
	}
	
	@Override
	public void execute(Game game, Player self, Tyr bot) 
	{
		agent.drawCircle(Color.Green);

		Unit yamatoTarget = agent.getYamatoTarget();
		if(yamatoTarget != null)
		{
			agent.unit.useTech(TechType.Yamato_Gun, yamatoTarget);
			return;
		}
		
    	if (killTarget == null)
    	{
    		List<Unit> inRange = game.getUnitsInRadius(agent.unit.getPosition(), agent.unit.getType().groundWeapon().maxRange());
    		
    		for(Unit unit : inRange)
    		{
    			if (unit.getPlayer().isEnemy(self) && unit.getType() != UnitType.Protoss_Observer)
    			{
    				killTarget = unit;
    				break;
    			}
    		}
    	}
		
		if (killTarget != null)
		{
			boolean targetFlies = killTarget.getType().isFlyer() || killTarget.isLifted();
			int maxRangeSq = targetFlies?agent.unit.getType().airWeapon().maxRange(): agent.unit.getType().groundWeapon().maxRange();
			maxRangeSq = maxRangeSq * maxRangeSq;
			
			if (agent.distanceSquared(killTarget) >= maxRangeSq)
				killTarget = null;
			else
			{
				game.drawLineMap(agent.unit.getX(), agent.unit.getY(), killTarget.getX(), killTarget.getY(), Color.Red);
				
				int cooldown = targetFlies?agent.unit.getAirWeaponCooldown():agent.unit.getGroundWeaponCooldown();
			
				if (cooldown == 0)
				{
					if (agent.unit.getOrder() != Order.AttackUnit)
						agent.unit.attack(killTarget);
				}
				else if (stutterstep)
				{
					Position fleeTarget = agent.retreatTarget(killTarget.getPosition(), 32);
					agent.unit.move(fleeTarget);
				}
				
				return;
			}
		}
		
		if (bot.army.enemyThreat)
		{
			Position orderTarget = agent.unit.getOrderTargetPosition();
			if (agent.unit.isIdle()
					|| (killTarget != null &&(Math.abs(orderTarget.getX() - target.getX()) >= 128 || Math.abs(orderTarget.getY() - target.getY()) >= 128))
					|| (killTarget != null && (agent.unit.getGroundWeaponCooldown() == 1 || agent.unit.getAirWeaponCooldown() == 1)))
				
				agent.unit.attack(bot.invader.getPosition());
		}
		else
		{
			if ((agent.unit.getOrder() == Order.Move || agent.unit.isIdle()))
				agent.unit.attack(target);
		}
	}

	@Override
	public boolean replace(Command command) 
	{
		if (!command.getClass().equals(BCDefend.class))
			return true;
		
		return ((BCDefend)command).target.getX() == target.getX() && ((BCDefend)command).target.getY() == target.getY();
	}
	
}
