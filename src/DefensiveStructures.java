import java.util.ArrayList;

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;


public class DefensiveStructures
{
	Position defendedPosition;
	private Position defensePos;
	ArrayList<Unit> defenses = new ArrayList<Unit>();
	public boolean tooFar = false;
	public boolean disabled = false;
	
	TankDefense tanks;
	
	public DefensiveStructures(Position defendedPosition, Position defensePos)
	{
		this.defendedPosition = defendedPosition;
		this.defensePos = defensePos;
		tanks = new TankDefense(Tyr.bot.hobos, defensePos);
		if (BWTAInitializer.initialized && Tyr.bot.self != null)
			tooFar = Tyr.tileToPostion(Tyr.bot.self.getStartLocation()).getDistance(defensePos) > 1200;
	}
	
	public void add(Unit unit)
	{
		defenses.add(unit);
	}

	public void onFrame(Game game, Player self, Tyr bot) 
	{		
		if(tanks.units.size() < bot.defensiveTanks)
		{
			for(Squad s : bot.army.squads)
				s.addTanks(tanks, bot.defensiveTanks - tanks.units.size());
		}
		
		tanks.cleanup();
		
		tanks.onFrame(game, self, bot);
		
        for(int i=0; i<defenses.size(); i++)
        {
        	if(defenses.get(i) == null 
        			|| defenses.get(i).getHitPoints() <= 0 
        			|| !defenses.get(i).exists() 
        			|| defenses.get(i).getRemoveTimer() != 0)
        	{
        		defenses.remove(i);
        		i--;
        	}
        }
        
        for(Unit structure : defenses)
        {
        	game.drawBoxMap(structure.getTilePosition().getX()*32, structure.getTilePosition().getY()*32,
        			structure.getTilePosition().getX()*32 + structure.getType().tileWidth()*32,
        			structure.getTilePosition().getY()*32 + structure.getType().tileHeight()*32,
        			Color.Orange);
        }
		
	}
	
	public int getUnitCount(UnitType type)
	{
		int count = 0;
		
		for(Unit unit : defenses)
			if(unit.getType() == type)
				count++;
		
		for(BuildCommand com : Tyr.bot.buildCommands)
			if(com.building == type && com.position.getDistance(Tyr.positionToTile(defensePos)) < 128)
				count++;
		
		return count;
	}

	public Position getDefensePos() 
	{
		if (defensePos == null)
		{
			defensePos = Tyr.bot.getMainExit();
			Position start = Tyr.tileToPostion(Tyr.bot.self.getStartLocation());
			defensePos = new Position((defensePos.getX() + start.getX())/2, 
					(defensePos.getY() + start.getY())/2);
		}
		return defensePos;
	}

	public void disable() 
	{
		tanks.disable();
		for(Unit unit : defenses)
		{
			if(unit.getType() == UnitType.Terran_Bunker)
			{
				Agent bunker = Tyr.bot.agentMap.get(unit.getID());
				((BunkerAgent)bunker).disabled = true;
			}
		}
		disabled = true;
	}
}
