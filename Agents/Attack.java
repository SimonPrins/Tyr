import bwapi.Color;
import bwapi.Game;
import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;


public class Attack extends Command
{
	private Position target;
	
	private static int waitFrame;
	private static int waitingAtCannon;
	private static int waitingAtCannonPrev;
	
	
	public Attack(Agent agent, Position target)
	{
		super(agent);
		this.target = target;
	}
	
	@Override
	public void execute(Game game, Player self, Tyr bot) 
	{
		agent.drawCircle(Color.Red);
		
		if (waitFrame != game.getFrameCount())
		{
			waitingAtCannonPrev = waitingAtCannon;
			DebugMessages.addMessage("Waiting at cannon: " + waitingAtCannon);
			waitingAtCannon = 0;
			waitFrame = game.getFrameCount();
		}
		
		boolean inrange = false;

		boolean proceed = (bot.scout.opponentStrategy != ScoutGroup.cannons && waitingAtCannonPrev >= 10) 
				|| agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode;


		int dist = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + 64;
		int smallDist = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() - 32;
		if (bot.scout.opponentStrategy != ScoutGroup.cannons)
			smallDist += 32;
		
		for(EnemyPosition enemy : bot.enemyDefensiveStructures)
		{
			if (agent.unit.getType() == UnitType.Terran_Battlecruiser)
				break;
			
			if(enemy.type == UnitType.Protoss_Photon_Cannon
					&& Math.abs(agent.unit.getX() - enemy.pos.getX()) <= dist
					&& Math.abs(agent.unit.getY() - enemy.pos.getY()) <= dist
					&& agent.distanceSquared(enemy.pos)<= dist * dist)
			{
				inrange = agent.distanceSquared(enemy.pos)<= (smallDist) * (smallDist);
				waitingAtCannon++;
				break;
			}
		}
		
		if (inrange && !agent.unit.isHoldingPosition() && !proceed)
			agent.unit.holdPosition();
		else if(proceed || !inrange)
			attack();
		else
			Tyr.bot.drawCircle(agent.unit.getPosition(), Color.Blue, 6);
	}

	private void attack()
	{
		if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
		{
			int radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
			for(Unit unit : Tyr.game.enemy().getUnits())
			{
				if (unit.getType().isFlyer() || unit.isLifted())
					continue;
				if (agent.distanceSquared(unit) <= radius * radius)
				{
					Tyr.game.drawLineMap(agent.unit.getX(), agent.unit.getY(), unit.getX(), unit.getY(), Color.Red);
					agent.unit.siege();
					agent.drawCircle(Color.Red, 6);
					return;
					
				}
			}

			if(agent.unit.isIdle() || Tyr.bot.defenseTime == 0)
			{
				agent.drawCircle(Color.White, 6);
				agent.unit.attack(target);
				return;
			}
			
			agent.drawCircle(Color.Blue, 6);
			return;
		}
		else if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
		{
			int radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
			for(Unit unit : Tyr.game.enemy().getUnits())
			{
				if (unit.getType().isFlyer() || unit.isLifted())
					continue;
				if (agent.distanceSquared(unit) <= radius * radius)
				{
					Tyr.game.drawLineMap(agent.unit.getX(), agent.unit.getY(), unit.getX(), unit.getY(), Color.Red);
					agent.drawCircle(Color.Red, 6);
					return;
					
				}
			}
			agent.drawCircle(Color.White, 6);
			agent.unit.unsiege();
			return;
		}
		
		if (agent.distanceSquared(target) <= 128*128)
			return;
		
		Order order = agent.unit.getOrder(); 
		Position orderTarget = agent.unit.getOrderTargetPosition();
		
		if (agent.unit.getGroundWeaponCooldown() > 1 && agent.unit.getAirWeaponCooldown() > 1)
		{
			agent.unit.move(Tyr.tileToPostion(Tyr.game.self().getStartLocation()));
		}
		else if (agent.unit.isHoldingPosition() || agent.unit.isIdle() || agent.unit.getGroundWeaponCooldown() == 1 || agent.unit.getAirWeaponCooldown() == 1)
		{
			Tyr.bot.drawCircle(agent.unit.getPosition(), Color.Green, 6);
			agent.unit.attack(target);
		}
		else 
		{
			double dist = target.getDistance(orderTarget);
			if (order == Order.AttackMove 
					&& orderTarget != null
					&& agent.unit.getTarget()== null
					&&
				(Math.abs(target.getX() - orderTarget.getX()) >= 10 
					|| Math.abs(target.getY() - orderTarget.getY()) >= 10
					|| dist >= 10))
			{
				Tyr.bot.drawCircle(agent.unit.getPosition(), Color.White, 6);
				agent.unit.attack(target);
			}
		}
	}
	
	@Override
	public boolean replace(Command command) 
	{
		if (!command.getClass().equals(Attack.class))
			return true;
		
		return ((Attack)command).target.getX() != target.getX() || ((Attack)command).target.getY() != target.getY();
	}
	
}
