import java.util.ArrayList;

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;


public class Army extends UnitGroup 
{
	public int requiredSize = 15;
	public int maximumSize;
	public ArrayList<Squad> squads = new ArrayList<Squad>();
	public ArrayList<BCSquad> bcsquads = new ArrayList<BCSquad>();
	public BCSquad retreatSquad;
	public BCSquad repairSquad;
    public ArrayList<DropHarass> dropHarass = new ArrayList<DropHarass>();
	public OutOfJob outOfJob = new OutOfJob();
	public Position rallyPoint = null;
	boolean enemyThreat = false;
	
	public ArrayList<Agent> bcrepair = new ArrayList<Agent>();
	private Agent damagedbc = null;
	
	public int mobileTankCount = 0;
	public ArrayList<Agent> mobileTankDefense = new ArrayList<Agent>();
	

	public TankForce tankForce = null;
	
	private StopWatch stopWatch = new StopWatch();
	
	public Army(OutOfJob rejects, int requiredSize, int maximumSize)
	{
		super(rejects);
		this.requiredSize = requiredSize;
		this.maximumSize = maximumSize;
	}
	
	public Squad addSquad()
	{
		Squad s = new Squad(outOfJob);
		squads.add(s);
		return s;
	}
	
	public BCSquad addBCSquad()
	{
		BCSquad s = new BCSquad(outOfJob);
		bcsquads.add(s);
		return s;
	}
	
	public void initializeTankForce()
	{
		if(tankForce != null)
			return;
		tankForce = new TankForce(rejects);
		
		for(Squad s : squads)
		{
			s.addTanks(tankForce);
		}
	}
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		stopWatch.start();
		
		if(retreatSquad == null)
		{
			retreatSquad = new BCSquad(outOfJob);
			repairSquad = new BCSquad(outOfJob);
		}
		
		if (bot.enemyBuildingMemory.size() == 0 && bot.invader == null)
		{
			if (bot.scanner.action != Scanner.Eliminating && bot.suspectedEnemy.size() == 1 && game.isVisible(bot.suspectedEnemy.get(0).getTilePosition()))
			{
				bot.scanner.startElimination();
				for (Squad squad : squads)
				{
					for(Agent agent : squad.units)
					{
						if (agent.unit.getType().isFlyer())
							bot.scanner.addAir(agent);
						else 
							bot.scanner.addGround(agent);
					}
				}
				for (BCSquad squad : bcsquads)
				{
					for(Agent agent : squad.units)
					{
						bot.scanner.addAir(agent);
					}
				}
			}
			
			if (bot.scanner.action == Scanner.Eliminating)
			{
				long time = stopWatch.time();
				
				if (time > 50)
				{
					DebugMessages.addMessage("Army is taking too long: " + time + "ms.");
				}
				return;
			}
		}
		
		if(mobileTankDefense.size() < mobileTankCount)
		{
			for(Squad s : bot.army.squads)
			{
				s.addTanks(mobileTankDefense, mobileTankCount - mobileTankDefense.size());
			}
			for(Agent agent : mobileTankDefense)
			{
				agent.order(new MobileTankDefense(agent));
			}
		}
		
		if(mobileTankDefense.size() < mobileTankCount)
		{
			for(DefensiveStructures defPos : bot.defensiveStructures)
			{
				if (defPos.tanks != null)
				{
					while(defPos.tanks.units.size() > 0 && mobileTankDefense.size() < mobileTankCount)
					{
						Agent tank = defPos.tanks.pop();
						if (tank == null)
							break;
						tank.order(new MobileTankDefense(tank));
						mobileTankDefense.add(tank);
					}
				}
			}
		}
		
		enemyThreat = false;
		if (bot.invader != null)
		{
			int enemyRange = 4*32 + 10;
			for(Unit enemyUnit : game.enemy().getUnits())
			{
				enemyRange = Math.max(enemyRange, enemyUnit.getType().groundWeapon().maxRange() + 10);
			}
			for(Unit unit : self.getUnits())
			{
				if (!unit.getType().isBuilding())
					continue;
				if(unit.getDistance(bot.invader.getPosition()) <= enemyRange)
				{
					enemyThreat = true;
					break;
				}
			}
		}

		int squadPos = 0;
		
		for(int i=squads.size()-1; i>= 0; i--)
			if(squads.get(i).units.size() == 0)
				squads.remove(i);
		
		int bcsquadPos = 0;
		
		for(BCSquad squad : bcsquads)
		{
			for(int i=0; i < squad.units.size(); i++)
			{
				Agent agent = squad.units.get(i);
				if (squad.attackMode && agent.unit.getHitPoints() <= 125)
				{
					squad.remove(i);
					i--;
					retreatSquad.add(agent);
				}
				if(!squad.attackMode &&  agent.unit.getHitPoints() < agent.unit.getType().maxHitPoints())
				{
					squad.remove(i);
					i--;
					repairSquad.add(agent);
				}
			}
			
			if ((squad.units.size() < BCSquad.fleeMinimum && squad.attackMode) || squad.retreatMode)
			{
				while(squad.units.size() > 0)
				{
					retreatSquad.add(squad.units.get(squad.units.size()-1));
					squad.units.remove(squad.units.size()-1);
				}
			}
			
			for(int i=0; i<retreatSquad.units.size(); i++)
			{
				Agent agent = retreatSquad.units.get(i);
				if (retreatSquad.target != null && agent.distanceSquared(retreatSquad.target) <= 200*200)
				{
					repairSquad.add(agent);
					retreatSquad.remove(i);
					i--;
				}
			}
			
			for(int i=0; i<repairSquad.units.size(); i++)
			{
				Agent agent = repairSquad.units.get(i);
				if (agent.unit.getHitPoints() >= agent.unit.getType().maxHitPoints())
				{
					outOfJob.add(agent);
					repairSquad.remove(i);
					i--;
				}
				else if (agent.unit.getHitPoints() <= 125 && retreatSquad.target != null && agent.distanceSquared(retreatSquad.target) > 200*200)
				{
					retreatSquad.add(agent);
					repairSquad.remove(i);
					i--;
				}
			}
			repairSquad.attackMode = false;
			repairSquad.retreatMode = false;
			
			retreatSquad.retreatMode = true;
			retreatSquad.attackMode = false;
		}
		
		for(int i=bcsquads.size()-1; i>= 0; i--)
			if(bcsquads.get(i).units.size() == 0)
				bcsquads.remove(i);
		
		for(Agent agent = outOfJob.pop();agent != null; agent = outOfJob.pop())
		{
			if(tankForce != null && 
					(agent.unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || agent.unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode))
			{
				tankForce.add(agent);
				continue;
			}
			
			if (agent.unit.getType() == UnitType.Terran_Battlecruiser)
			{
				for(;bcsquadPos < bcsquads.size() && (bcsquads.get(bcsquadPos).units.size() >= BCSquad.maximum || bcsquads.get(bcsquadPos).attackMode);bcsquadPos++);
				
				if (bcsquadPos >= bcsquads.size())
					addBCSquad();
				
				bcsquads.get(bcsquadPos).add(agent);
				continue;
			}
			
			for(;squadPos < squads.size() && squads.get(squadPos).units.size() >= 5 && squads.get(squadPos).attackMode;squadPos++);
			
			if (squadPos >= squads.size())
				addSquad();
			
			squads.get(squadPos).add(agent);
		}
		
		if(units.size() >= requiredSize + 5 && requiredSize < maximumSize)
			requiredSize += 5;
		
		for(Squad s : squads)
			s.onFrame(game, self, bot);
		
		if (repairSquad.units.size() > 0 && bot.invader == null)
		{
			while (bcrepair.size() < 3)
			{
				Agent scv = bot.workForce.pop(bot.getMainExit());
				if(scv == null)
					break;
				bcrepair.add(scv);
				
				scv.order(new None(scv));
			}
			
			if (damagedbc != null && (damagedbc.isDead() || damagedbc.unit.getHitPoints() >= damagedbc.unit.getType().maxHitPoints()))
				damagedbc = null;
			if (damagedbc == null)
			{
				int hp = 500;
				for(Agent agent : repairSquad.units)
				{
					if (agent.unit.getHitPoints() < hp)
					{
						damagedbc = agent;
						hp = agent.unit.getHitPoints();
					}
				}
			}
			
			if(damagedbc != null)
			{
				boolean first = true;
				for(Agent scv : bcrepair)
				{
					if (first)
					{
						damagedbc.unit.attack(scv.unit.getPosition());
						first = false;
					}
					if (!scv.unit.isRepairing())
						scv.unit.repair(damagedbc.unit);
				}
			}

			for(Agent scv : bcrepair)
			{
				scv.drawCircle(Color.Yellow);
				if(!scv.unit.isRepairing())
					continue;
				Unit bcTarget = scv.unit.getTarget();
				if(bcTarget != null)
					game.drawLineMap(scv.unit.getX(), scv.unit.getY(), bcTarget.getX(), bcTarget.getY(), Color.Yellow);
			}
		}
		else
		{
			while (bcrepair.size() > 0)
			{
				Agent scv = bcrepair.get(bcrepair.size() -1);
				bcrepair.remove(bcrepair.size() - 1);
				scv.order(new None(scv));
				rejects.add(scv);
			}
		}
		
		retreatSquad.onFrame(game, self, bot);
		repairSquad.onFrame(game, self, bot);
		for(BCSquad s :bcsquads)
			s.onFrame(game, self, bot);
		
		if(tankForce != null)
			tankForce.onFrame(game, self, bot);
		
		for(DropHarass dh : dropHarass)
			dh.onFrame(game, self, bot);
		
		long time = stopWatch.time();
		
		if (time > 50)
		{
			DebugMessages.addMessage("Army is taking too long: " + time + "ms.");
		}
		DebugMessages.addMessage("Battlecruiser squads: " + bcsquads.size());
	}
	
	@Override
	public void add(Agent agent)
	{
		super.add(agent);
		outOfJob.add(agent);
		if (Tyr.bot.scanner.action == Scanner.Eliminating)
		{
			if (agent.unit.getType().isFlyer())
				Tyr.bot.scanner.addAir(agent);
			else 
				Tyr.bot.scanner.addGround(agent);
		}
	}
	
	@Override
	public Agent pop()
	{
		for(int i=squads.size()-1; i >= 0; i--)
		{
			if(!squads.get(i).attackMode && squads.size()> 0)
			{
				Agent result = squads.get(i).pop();
				units.remove(result);
				if (result != null)
					result.order(new None(result));
				return result;
			}
		}
		
		return null;
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		if(tankForce != null)
			tankForce.cleanup();
		
		for(int i=0; i<dropHarass.size(); i++)
		{
			DropHarass dh = dropHarass.get(i);
			dh.cleanup();
			if(!dh.hasDropship() && dh.units.size() == 0)
			{
				dropHarass.remove(i);
				i--;
			}
		}

		if (repairSquad != null)
			repairSquad.cleanup();
		if (retreatSquad != null)
			retreatSquad.cleanup();
		
		for(int i=0; i<bcrepair.size(); i++)
		{
			if(bcrepair.get(i).isDead())
			{
				bcrepair.remove(i);
				i--;
			}
		}
		
		for(int i=0; i<mobileTankDefense.size(); i++)
		{
			if (mobileTankDefense.get(i).isDead())
			{
				mobileTankDefense.remove(i);
				i--;
			}
		}
	}

	public ArrayList<Agent> getTanks(int i) 
	{
		ArrayList<Agent> result = new ArrayList<Agent>();
		for(Squad s : squads)
			s.addTanks(result, i - result.size());
		for(Agent agent : result)
			units.remove(agent);
		return result;
	}
}
