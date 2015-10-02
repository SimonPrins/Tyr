import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.UnitType;


public class AntiCannonPush extends Command
{
	private Position target;
	
	public AntiCannonPush(Agent agent, Position target) 
	{
		super(agent);
		this.target = target;
	}

	@Override
	public void execute(Game game, Player self, Tyr bot) 
	{
		if(agent.unit.isIdle() || bot.defenseTime == 0)
			agent.unit.attack(target);
		
		if(agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
		{
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
			{
				if(enemy.type == UnitType.Protoss_Photon_Cannon
						&& agent.distanceSquared(enemy.pos) <= 
						UnitType.Terran_Siege_Tank_Siege_Mode.sightRange()*UnitType.Terran_Siege_Tank_Siege_Mode.sightRange())
				{
					agent.unit.siege();
					break;
				}
			}
		}
		else if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
		{
			boolean inrange = false;
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
			{
				if(enemy.type == UnitType.Protoss_Photon_Cannon
						&& agent.distanceSquared(enemy.pos) <= 
								UnitType.Terran_Siege_Tank_Siege_Mode.sightRange()*UnitType.Terran_Siege_Tank_Siege_Mode.sightRange())
				{
					inrange = true;
					break;
				}
			}
			
			if(!inrange)
				agent.unit.unsiege();
		}
	}

	@Override
	public boolean replace(Command command)
	{
		if (!command.getClass().equals(AntiCannonPush.class))
			return true;
		
		return ((AntiCannonPush)command).target.getX() == target.getX() && ((AntiCannonPush)command).target.getY() == target.getY();
	}

}
