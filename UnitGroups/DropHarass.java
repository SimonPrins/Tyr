import java.util.ArrayList;
import java.util.HashMap;
import bwapi.Color;
import bwapi.Game;
import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;


public class DropHarass extends UnitGroup 
{
	private Agent dropship;
	
	int mode = gather;

	private static final int gather = 0;
	private static final int load = 1;
	private static final int moveOut = 2;
	private static final int drop = 3;
	
	private  ArrayList<Position> targets = null;
	private int targetPos = -1;
	private Position target = null;

	private static ArrayList<Position> leftTargets;
	private static ArrayList<Position> rightTargets;
	private static boolean goLeft = true;
	
	private static boolean debug = false;
	
	public DropHarass(OutOfJob rejects)
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if (leftTargets == null)
			createTargetOrder(bot);
		
		if (debug && leftTargets != null)
		{
			for(int i=1; i<leftTargets.size(); i++)
			{
				game.drawLineMap(leftTargets.get(i-1).getX(), leftTargets.get(i-1).getY(), leftTargets.get(i).getX(), leftTargets.get(i).getY(), Color.Red);
				game.drawTextMap(leftTargets.get(i-1).getX(), leftTargets.get(i-1).getY(), (i-1) + "");
			}
			for(int i=1; i<rightTargets.size(); i++)
			{
				game.drawLineMap(rightTargets.get(i-1).getX(), rightTargets.get(i-1).getY(), rightTargets.get(i).getX(), rightTargets.get(i).getY(), Color.Blue);
				game.drawTextMap(rightTargets.get(i-1).getX(), rightTargets.get(i-1).getY(), (i-1) + "");
			}
		}
		
		
		
		for(Agent agent : units)
			agent.drawCircle(Color.Orange);
		if(mode == gather)
		{
			if (dropship == null)
			{
				while(units.size() > 0)
				{
					rejects.add(units.get(units.size()-1));
					units.remove(units.size()-1);
				}
			}
			
			if(dropship != null)
				dropship.drawCircle(Color.Orange, 16);
			
			int total = 0;
			for(Agent agent : units)
				total += getSpace(agent.unit.getType());
			if (total >= 8)
				mode = load;
			else if (hasDropship())
			{
				for(Agent agent : bot.army.units)
				{
					if(agent.unit.getType() == UnitType.Terran_Vulture)
					{
						units.add(agent);
						total += getSpace(agent.unit.getType());
						bot.army.remove(agent);
					}
					if (total >= 8)
						break;
				}
				if (false && bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) + bot.count(UnitType.Terran_Siege_Tank_Siege_Mode) 
						> bot.defensiveTanks * bot.defensiveStructures.size())
				{
					ArrayList<Agent> tanks = bot.army.getTanks((8 - total) / 4);
					for(Agent tank : tanks)
						units.add(tank);
				}
			}
		}
		
		if(mode != gather && units.size() == 0)
		{
			mode = gather;
			targetPos = -1;
			targets = null;
			
			if(dropship != null)
				dropship.unit.move(Tyr.tileToPostion(self.getStartLocation()));
		}
		
		if(mode == load)
		{
			if (dropship == null)
			{
				mode = gather;
				return;
			}
			dropship.drawCircle(Color.White, 16);
			boolean done = true;
			for(Agent agent : units)
			{
				if (agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode)
				{
					done = false;
					agent.order(new None(agent));
					agent.unit.unsiege();
					continue;
				}
				if(!agent.unit.isLoaded())
				{
					done = false;
					dropship.unit.load(agent.unit);
					if (agent.unit.getTargetPosition() == null || agent.unit.getTargetPosition().getDistance(dropship.unit.getPosition()) >= 100)
						agent.unit.move(dropship.unit.getPosition());
				}
			}
			
			if(done)
			{
				mode = moveOut;
				acquireTarget(bot);
			}
		}
		
		if(mode == moveOut)
		{
			if(dropship == null)
			{
				mode = gather;
				return;
			}
			dropship.drawCircle(Color.Yellow, 16);
			
			if(target == null)
				acquireTarget(bot);
			
			if(target != null)
			{
				bot.drawCircle(target, Color.White, 300);
				int rangeSq = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() 
						* UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
				
				boolean enemyLeft = false;
				boolean inRange = dropship.distanceSquared(target) <= 300*300;
				for(Unit enemy : Tyr.game.enemy().getUnits())
				{
					if (enemy.getDistance(target) <= 300)
					{
						enemyLeft = true;
						if(inRange || dropship.distanceSquared(enemy) <= rangeSq)
						{
							mode = drop;
							dropship.unit.stop();
							break;
						}
					}
				}
				
				if(inRange && !enemyLeft)
					acquireTarget(bot);
				else if(!inRange && mode == moveOut)
					dropship.unit.move(target);
			}
		}
		
		if(mode == drop)
		{
			if(dropship != null)
				dropship.drawCircle(Color.Red, 16);
			
			if (target == null)
			{
				mode = load;
				return;
			}
			
			boolean enemyLeft = false;
			for(Unit enemy : Tyr.game.enemy().getUnits())
			{
				if (enemy.getDistance(target) <= 300)
				{
					enemyLeft = true;
					break;
				}
			}
			
			if (!enemyLeft)
			{
				mode = load;
				return;
			}

			if (dropship != null)
			{
				boolean isCarrying = false;
				for(Agent agent : units)
					if(agent.unit.isLoaded())
					{
						dropship.drawCircle(Color.Green, 6);
						dropship.unit.unload(agent.unit);
						isCarrying = true;
						break;
					}
				if (isCarrying && bot.spaceManager.map[dropship.unit.getTilePosition().getX()][dropship.unit.getTilePosition().getY()] != 1)
					dropship.unit.move(target);
				if(!isCarrying || bot.spaceManager.map[dropship.unit.getTilePosition().getX()][dropship.unit.getTilePosition().getY()] == 1)
				{
					if(dropship.unit.getOrder() == Order.Move)
						dropship.unit.stop();
				}
			}
			
				
			
			for (Agent agent : units)
			{
				if(agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
					agent.unit.siege();
				else if (agent.unit.getType() != UnitType.Terran_Siege_Tank_Siege_Mode)
					agent.unit.attack(target);
			}
		}
	}
	
	private int getSpace(UnitType type) 
	{
		if (type == UnitType.Terran_Siege_Tank_Siege_Mode || type == UnitType.Terran_Siege_Tank_Tank_Mode)
			return 4;
		if(type == UnitType.Terran_Vulture)
			return 2;
		if(type == UnitType.Terran_Marine || type == UnitType.Terran_SCV || type == UnitType.Terran_Medic)
			return 1;
		System.out.println("Required space in dropship unknown for UnitType: " + type);
		DebugMessages.addMessage("Required space in dropship unknown for UnitType: " + type);
		return 8;
	}

	@Override
	public void cleanup()
	{
		super.cleanup();
		if(dropship != null && dropship.isDead())
			dropship = null;
	}
	
	public boolean hasDropship()
	{
		return dropship != null;
	}
	
	public void setDropship(Agent dropship)
	{
		this.dropship = dropship;
	}
	public boolean acquireTarget(Tyr bot)
	{
		if (targets == null || targets.size() == 0 || targetPos >= targets.size())
			acquireTargets(bot);

		if (targets == null || targets.size() == 0)
			return false;
		
		if (!nextTarget())
			return false;
		
		while(true)
		{
			if (target == null)
			{
				if (!nextTarget())
					return false;
			}
			
			if (!Tyr.game.isVisible(Tyr.positionToTile(target)))
				return true;
			

			for(Unit enemy : Tyr.game.enemy().getUnits())
			{
				if (enemy.getDistance(target) <= 300)
					return true;
			}
			
			if (!nextTarget())
				return false;
		}
		
	}
	
	public boolean nextTarget()
	{
		targetPos++;
		if (targetPos >= targets.size())
			return false;
		target = targets.get(targetPos);
		return true;
	}
	
	public boolean acquireTargets(Tyr bot)
	{
		if (leftTargets == null)
			createTargetOrder(bot);
		
		if (leftTargets == null)
			return false;
		
		targetPos = -1;
		
		goLeft = !goLeft;
		
		targets = goLeft? leftTargets:rightTargets;
		return true;
	}

	private void createTargetOrder(Tyr bot) 
	{
		if (bot.suspectedEnemy.size() != 1)
			return;
		ArrayList<Position> positions = new ArrayList<Position>();
		HashMap<Position, Double> projection = new HashMap<Position, Double>();
		
		for(BaseLocation b : bot.expands)
		{
			if(b.getPosition().getDistance(Tyr.tileToPostion(bot.self.getStartLocation())) <= 100)
				continue;
			positions.add(b.getPosition());
			projection.put(b.getPosition(), projectToBorder(b.getPosition()));
		}

		double selfProjection = projectToBorder(Tyr.tileToPostion(bot.self.getStartLocation()));
		double enemyProjection = projectToBorder(bot.suspectedEnemy.get(0).getPosition());

		for(boolean changes = true; changes;)
		{
			changes = false;
			for(int i=0; i<positions.size()-1; i++)
			{
				double p1 = projection.get(positions.get(i));
				double p2 = projection.get(positions.get(i+1));
				if (p1 > selfProjection && p2 < selfProjection)
					continue;
				
				if ((p2 > selfProjection && p1 < selfProjection) || projection.get(positions.get(i)) > projection.get(positions.get(i+1)))
				{
					Position temp = positions.get(i);
					positions.set(i, positions.get(i+1));
					positions.set(i+1, temp);
					changes = true;
				}
			}
		}
		
		rightTargets = new ArrayList<Position>();
		for(int i=positions.size()-1;
				i >= 0 && 
						(
							(selfProjection >= enemyProjection && (projection.get(positions.get(i)) > enemyProjection && projection.get(positions.get(i)) < selfProjection)) ||
							(selfProjection < enemyProjection && (projection.get(positions.get(i)) > enemyProjection || projection.get(positions.get(i)) < selfProjection)) 
						)
				;i--)
		{
			rightTargets.add(positions.get(i));
			positions.remove(i);
		}
		leftTargets = positions;
		
		leftTargets.add(bot.suspectedEnemy.get(0).getPosition());
		rightTargets.add(bot.suspectedEnemy.get(0).getPosition());
	}
	
	private double projectToBorder(Position p)
	{
		int width = Tyr.game.mapWidth()*32;
		int height = Tyr.game.mapHeight()*32;
		int xd1 = p.getX();
		int xd2 = width - p.getX();
		int yd1 = p.getY();
		int yd2 = height - p.getY();
		if (Math.min(xd1, xd2) < Math.min(yd1, yd2))
		{
			if (xd1 < xd2)
				return 0.5 + (double)yd1/4.0/(double)height;
			else
				return (double)yd2/4.0/(double)height;
		}
		else
		{
			if (yd1 < yd2)
				return 0.25 + (double)xd2/4.0/(double)width;
			else
				return 0.75 + (double)xd1/4.0/(double)width;
		}
	}

}
