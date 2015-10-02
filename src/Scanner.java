import java.util.ArrayList;

import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.UnitType;


public class Scanner 
{
	private int[][] state;
	private int scans = 0;
	private int row = 0;
	private int unseen = 0;
	final int rowsPerStep = 2;

	public final static int Initializing = 0;
	public final static int Scanning = 1;
	public final static int Eliminating = 2;
	
	public int action = Initializing;
	private int width, height;

	private ArrayList<Agent> groundUnits = new ArrayList<Agent>();
	private ArrayList<Agent> airUnits = new ArrayList<Agent>();
	
	public Scanner(Game game)
	{
		state = new int[game.mapWidth()][];
		this.width = game.mapWidth();
		this.height = game.mapHeight();
	}
	
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if (!BWTAInitializer.initialized)
			return;
		for(int i=0; i<rowsPerStep; i++)
		{
			if (action == Initializing)
				initialize(game, self, bot);
			else if (action == Scanning)
				scan(game, self, bot);
			else if (action == Eliminating)
				eliminate(game, self, bot);
			
			if (incrementRow())
			{
				if (unseen == 0)
					scans++;
				unseen = 0;
			}
		}
			
	}
	
	private boolean incrementRow()
	{
		row++;
		if (row >= width)
			row = 0;
		return row == 0;
	}
	
	private void initialize(Game game, Player self, Tyr bot)
	{
			state[row] = new int[height];
			if (row >= width - 1)
				action = Scanning;
	}

	private void scan(Game game, Player self, Tyr bot)
	{
		for(int y=0; y < height; y++)
		{
			if (state[row][y] == scans)
				continue;
			if (game.isVisible(new TilePosition(row, y)))
				state[row][y] = scans;
			else
				unseen++;
		}
	}

	private void eliminate(Game game, Player self, Tyr bot)
	{
		scan(game, self, bot);
		
		if (bot.invader != null || bot.enemyBuildingMemory.size() > 0)
			return;
		
		int groundPos = nextGroundPos(-1);
		int airPos = nextAirPos(-1);
		for(int y=0; y < height; y++)
		{
			if (state[row][y] == scans)
				continue;
			
			boolean walkable = false;
			
			for(int dx = 0; !walkable && dx<4; dx++)
				for(int dy=0; !walkable && dy<4; dy++)
					if(game.isWalkable(row*4+dx, y*4+dy))
					{
						walkable = true;
						break;
					}
			
			if (walkable)
			{
				if (groundPos < groundUnits.size())
				{
					Agent agent = groundUnits.get(groundPos);
					agent.order(new None(agent));
					agent.unit.attack(Tyr.tileToPostion(new TilePosition(row, Math.min(y+2, game.mapHeight()))));
					groundPos = nextGroundPos(groundPos);
					y+=5;
				}
			}
			else
			{
				if (airPos < airUnits.size())
				{
					Agent agent = airUnits.get(airPos);
					agent.order(new None(agent));
					agent.unit.attack(Tyr.tileToPostion(new TilePosition(row, Math.min(y+2, game.mapHeight()))));
					airPos = nextAirPos(airPos);
					y+=5;
				}
			}
		}
	}

	private int nextGroundPos(int i) 
	{
		for(i++; i<groundUnits.size() && !groundUnits.get(i).unit.isIdle(); i++);
		return i;
	}

	private int nextAirPos(int i) 
	{
		for(i++; i<airUnits.size() && !airUnits.get(i).unit.isIdle(); i++);
		return i;
	}

	public void startElimination() 
	{
		action = Eliminating;

		
		if (Tyr.bot.count(UnitType.Terran_Factory) == 0)
			Tyr.bot.build(UnitType.Terran_Factory);
		if(Tyr.bot.count(UnitType.Terran_Refinery) == 0)
			Tyr.bot.build(UnitType.Terran_Refinery);
		if(Tyr.bot.count(UnitType.Terran_Starport) == 0)
			Tyr.bot.build(UnitType.Terran_Starport);
	}

	public void addAir(Agent agent) 
	{
		airUnits.add(agent);
		agent.command = new None(agent);
	}

	public void addGround(Agent agent) 
	{
		groundUnits.add(agent);
		agent.command = new None(agent);
	}
}
