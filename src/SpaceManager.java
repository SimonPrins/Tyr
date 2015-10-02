import java.util.ArrayList;

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;


public class SpaceManager
{
	public int[][] map;
	
	public int width, height;
	
	Game game;
	ArrayList<TilePosition> potentialDepots = new ArrayList<TilePosition>();
	
	static final boolean debug = false;
	
	public SpaceManager(Game game)
	{
		this.width = game.mapWidth();
		this.height = game.mapHeight();
		
		this.game = game;
		
		map = new int[width][height];
		for(int x=0; x<width; x++)
			for(int y=0; y<height; y++)
				if(game.isBuildable(x, y))
					map[x][y] = 1;
		
	}

	public void onFrame(Game game, Player self, Tyr bot)
	{
		if (debug)
			for(TilePosition pos : potentialDepots)
				game.drawBoxMap(pos.getX()*32, pos.getY()*32, (pos.getX()+3)*32, (pos.getY()+2)*32, Color.White);
		
		for(int x=0; x<width; x++)
			for(int y=0; y<height; y++)
				if(map[x][y] != 0 && map[x][y] != 3)
					map[x][y] = 1;
		
		for(Unit neutralUnit : game.getNeutralUnits())
		{
			reserveSpace(neutralUnit);
		}
		
		for(Unit myUnit : self.getUnits())
		{
			
			if(!myUnit.getType().isBuilding())
				continue;
			
			if(myUnit.isLifted())
				continue;
			
			reserveSpace(myUnit);
		}
		
		for(BuildCommand com : bot.buildCommands)
			reserveSpace(com);
		
		if(bot.wallOff != null && bot.wallOff.placementFound)
		{
			for(BuildCommand com : bot.wallOff.buildPlan)
				reserveSpace(com);
		
			for(BuildCommand com : bot.wallOff.underConstruction)
				reserveSpace(com);
		}
		
		if (debug)
		{
			for(int x=0; x<width; x++)
				for(int y=0; y<height; y++)
				{
					if(map[x][y] == 0)
						game.drawBoxMap(x*32, y*32, x*32 + 32, y*32 + 32, Color.Red);
					else if (map[x][y] == 2)
						game.drawBoxMap(x*32, y*32, x*32 + 32, y*32 + 32, Color.Blue);
					else if (map[x][y] == 3)
						game.drawBoxMap(x*32, y*32, x*32 + 32, y*32 + 32, Color.Grey);
				}
		}
		
		findDepots(game, self, bot);
	}
	
	private int x = 0;
	
	private void findDepots(Game game, Player self, Tyr bot)
	{
		if (!BWTAInitializer.initialized)
			return;
		
		if (bot.workForce.mineralWorkers.size() == 0 || bot.workForce.mineralWorkers.get(0).units.size() == 0)
			return;
		
		if (x >= game.mapWidth())
			return;
		
		Unit worker;
		try
		{
			worker = bot.workForce.mineralWorkers.get(0).units.get(0).unit;
		}
		catch(NullPointerException e)
		{
			return;
		}
		
		TilePosition start = self.getStartLocation();
		if (start == null)
		{
			DebugMessages.addMessage("Startlocation is null!");
			return;
		}
		
		Region startRegion = BWTA.getRegion(start);
		if (startRegion == null)
		{
			DebugMessages.addMessage("Startregion is null!");
			return;
		}
		
		for (int y = 0; y <game.mapHeight(); y += 2)
		{
			TilePosition pos = new TilePosition(x, y);
			
			Region depotRegion = BWTA.getRegion(pos);
			
			if (depotRegion == null)
				continue;
			
			if (!depotRegion.equals(startRegion))
				continue;
			
			if (!canBuildHere(worker, pos, UnitType.Terran_Supply_Depot))
				continue;
			
			potentialDepots.add(pos);
		}
		x+=3;
	}
	
	public TilePosition findDepotPlacement(Unit builder)
	{
		if (!BWTAInitializer.initialized)
			return null;
		
		if (potentialDepots.size() == 0)
			return null;
		
		if (!game.isVisible(game.self().getStartLocation()))
			return null;
		
		int bestDist = 0;
		TilePosition result = null;
		
		TilePosition mainExit = Tyr.positionToTile(Tyr.bot.getMainExit());
		
		for(TilePosition pos : potentialDepots)
		{
			if (!Tyr.bot.canBuildHere(builder, pos.getX(), pos.getY(), UnitType.Terran_Supply_Depot))
				continue;
			int newDist = Math.abs(pos.getX() - mainExit.getX()) + Math.abs(pos.getY() - mainExit.getY());
			if (newDist > bestDist)
			{
				bestDist = newDist;
				result = pos;
			}
		}
		
		if(result != null)
			potentialDepots.remove(result);
		
		return result;
	}
	
	public void reserveSpace(Unit unit)
	{
		reserveSpace(unit.getTilePosition(), unit.getType());
	}
	
	public void reserveSpace(BuildCommand com)
	{
		reserveSpace(com.position, com.building);
	}
	
	public void reserveSpace(TilePosition pos, UnitType building)
	{
		if(building.canBuildAddon())
			for(int dx=0; dx<2; dx++)
				for(int dy=0; dy<2; dy++)
					map[pos.getX() + building.tileWidth() + dx][pos.getY() + building.tileHeight() - 2 + dy] = 2;

		for(int dx = 0; dx < building.tileWidth(); dx++)
			for(int dy = 0; dy < building.tileHeight(); dy++)
				map[pos.getX() + dx][pos.getY() + dy] = building.isNeutral()?3:2;
	}

	public boolean canBuildHere(Unit builder, TilePosition pos,
			UnitType building)
	{
		if(pos.getX() < 0 || pos.getY()<0 
				|| pos.getX() + building.tileWidth () >= width 
				|| pos.getY() + building.tileHeight() >= height)
			return false;
		
		for(int dx = 0; dx < building.tileWidth(); dx++)
			for(int dy = 0; dy < building.tileHeight(); dy++)
				if(map[pos.getX()+dx][pos.getY()+dy] != 1)
					return false;

		if(building.canBuildAddon())
		{
			if (pos.getX() + building.tileWidth() + 2 >= width)
				return false;
			
			for(int dx=0; dx<2; dx++)
				for(int dy=0; dy<2; dy++)
					if (map[pos.getX() + building.tileWidth() + dx][pos.getY() + building.tileHeight() - 2 + dy] != 1)
						return false;
		}
		
		if (!building.isResourceDepot())
		{
			for(BaseLocation loc : Tyr.bot.expands)
			{
				TilePosition locpos = loc.getTilePosition();
				if (pos.getX() + building.tileWidth() <= locpos.getX() - 1)
					continue;
				if (pos.getX() > locpos.getX() + UnitType.Terran_Command_Center.tileWidth() + 3)
					continue;
				if (pos.getY() + building.tileHeight() <= locpos.getY() - 1)
					continue;
				if (pos.getY() > locpos.getY() + UnitType.Terran_Command_Center.tileHeight() + 1)
					continue;
				
				return false;
			}
		}
		
		
		return game.canBuildHere(builder, pos, building, false);
	}
}
