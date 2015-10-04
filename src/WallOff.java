/*
* Tyr is an AI for StarCraft: Broodwar, 
* 
* Please visit https://github.com/SimonPrins/Tyr for further information.
* 
* Copyright 2015 Simon Prins
*
* This file is part of Tyr.
* Tyr is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
* Tyr is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* You should have received a copy of the GNU General Public License
* along with Tyr.  If not, see http://www.gnu.org/licenses/.
*/

import java.util.ArrayList;

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.Region;


public class WallOff
{
	public boolean placementFound = false;
	
	ArrayList<BuildCommand> buildPlan = new ArrayList<BuildCommand>();
	ArrayList<BuildCommand> underConstruction = new ArrayList<BuildCommand>();
	ArrayList<Unit> wall = new ArrayList<Unit>();
	
	TilePosition center;
	
	int[][] cliffs;
	boolean[][] buildable;
	boolean[][]cliff1;
	boolean[][]cliff2;
	
	int[][] wallPlacements;
	int[][] dims;
	
	boolean checked = false;
	boolean firstBuilding = false;
	
	public static int r = 7;

	public WallOff(TilePosition center, Region defendedRegion, Game game)
	{
		this.center = center;

		cliffs = new int[2*r+1][2*r+1];
		buildable = new boolean[2*r+1][2*r+1];
				
		for(int x=center.getX() - r; x<=center.getX() + r; x++)
			for(int y=center.getY() - r; y<=center.getY() + r; y++)
			{
				TilePosition pos = new TilePosition(x, y);
				if(game.isBuildable(pos))
				{
					buildable[x-center.getX() + r][y-center.getY() + r] = true;
				}
				else 
				{
					boolean walkable = true;
					for(int dx = 0; dx<4; dx++)
						for(int dy=0; dy<4; dy++)
							if(!game.isWalkable(x*4+dx, y*4+dy))
							{
								walkable = false;
								break;
							}
					if(!walkable)
						cliffs[x-center.getX() + r][y-center.getY() + r] = 1;
				}
					
			}
		
		ArrayList<Integer> cliffSizes = new ArrayList<Integer>();
		
		int n=2;
		for(int x=center.getX() - r; x<=center.getX() + r; x++)
			for(int y=center.getY() - r; y<=center.getY() + r; y++)
			{
				if (cliffs[x-center.getX() + r][y-center.getY() + r] == 1)
				{
					cliffSizes.add(bfs(x, y, n));
					n++;
				}
			}
		
		int c1;
		for(c1 = 0; c1<cliffSizes.size(); c1++)
			if(cliffSizes.get(c1) >= 6)
				break;
		
		int c2;
		for(c2 = c1 + 1; c2<cliffSizes.size(); c2++)
			if(cliffSizes.get(c2) >= 6)
				break;
		
		if(c2 >= cliffSizes.size())
			return;
		
		
		cliff1 = new boolean[2*r+1][2*r+1];
		cliff2 = new boolean[2*r+1][2*r+1];
		for(int x=0; x<2*r+1; x++)
			for(int y=0; y<2*r+1; y++)
			{
				if (cliffs[x][y] == c1+2)
				{
					cliff1[x][y] = true;
				}
				if (cliffs[x][y] == c2+2)
				{
					cliff2[x][y] = true;
				}
			}
		
		getWall();
	}
	
	private void getWall()
	{
		dims = new int[][]{{4, 3},{3, 2}};
		wallPlacements = getWall(dims);
		if(wallPlacements == null)
		{
			dims = new int[][]{{3, 2},{4, 3}};
			wallPlacements = getWall(dims);
		}
		if(wallPlacements == null)
		{
			dims = new int[][]{{4, 3},{3, 2},{3, 2}};
			wallPlacements = getWall(dims);
		}
		if(wallPlacements == null)
		{
			dims = new int[][]{{3, 2},{4, 3},{3, 2}};
			wallPlacements = getWall(dims);
		}
		if(wallPlacements == null)
		{
			dims = new int[][]{{3, 2},{3, 2},{4, 3}};
			wallPlacements = getWall(dims);
		}
		
		if(wallPlacements != null)
		{
			placementFound = true;
			for(int i=0; i<wallPlacements.length; i++)
			{
				buildPlan.add(new BuildCommand(null,
						dims[i][0]==4?UnitType.Terran_Barracks:UnitType.Terran_Supply_Depot,
								new TilePosition(center.getX() - r + wallPlacements[i][0], center.getY() - r + wallPlacements[i][1]),
								true));
			}
		}
		else
		{
			System.out.println("No wall placements.");
			placementFound = false;
		}
		System.out.println("buildPlan.size = " + buildPlan.size());
	}
	
	public BuildCommand getCommand(UnitType building)
	{
		if (firstBuilding && !checked)
			return null;
		firstBuilding = true;
		
		BuildCommand result = null;
		for(BuildCommand com : buildPlan)
		{
			if(com.building == building)
			{
				result = com;
				break;
			}
		}
		
		if(result != null)
		{
			buildPlan.remove(result);
			underConstruction.add(result);
		}
		return result;
	}
	
	public int[][] getWall(int[][] dims)
	{
		boolean[][][] buildingPlacements = new boolean[dims.length][][];
		buildingPlacements[0] = getSurroundingPlacements(cliff1, 1, 1, dims[0][0], dims[0][1]);
		boolean[][] lastBuilding = getSurroundingPlacements(cliff2, 1, 1, dims[dims.length-1][0], dims[dims.length-1][1]);
		
		for(int i=0; i<dims.length-1; i++)
			buildingPlacements[i+1] = getSurroundingPlacements(buildingPlacements[i], dims[i][0], dims[i][1], dims[i+1][0], dims[i+1][1]);
		
		int[][]result = new int[dims.length][2];
		boolean[][] closingPlacements = lastBuilding; 
		
		for(int i=dims.length-1; i>=0; i--)
		{
			int x=0, y=0;
			boolean found = false;
			for(x=0; x<2*r+1; x++)
			{
				for(y=0; y<2*r+1; y++)
				{
					if (buildingPlacements[i][x][y] && closingPlacements[x][y])
					{
						found = true;
						break;
					}
				}

				if(found)
					break;
			}
			
			if(!found) return null;
			result[i][0] = x;
			result[i][1] = y;
			
			if(i > 0)
			{
				closingPlacements = new boolean[2*r+1][2*r+1];
				closingPlacements[x][y] = true;
				closingPlacements = getSurroundingPlacements(closingPlacements, dims[i][0], dims[i][1], dims[i-1][0], dims[i-1][1]);
			}
		}
		
		return result;
	}
	
	public int bfs(int x, int y, int n)
	{
		if(Math.abs(x-center.getX()) > r || Math.abs(y-center.getY()) > r)
			return 0;
		if (cliffs[x-center.getX() + r][y-center.getY() + r] != 1)
			return 0;
		cliffs[x-center.getX() + r][y-center.getY() + r] = n;
		
		int total = 1;
		
		total += bfs(x-1, y, n);
		total += bfs(x+1, y, n);
		total += bfs(x, y-1, n);
		total += bfs(x, y+1, n);
		
		return total;
	}
	
	public void onFrame(Game game, Player self, Tyr bot)
	{
		ArrayList<Unit> deadWall = new ArrayList<Unit>();
		for(Unit unit : wall)
		{
			if(unit.getHitPoints() <= 0 || !unit.exists())
				deadWall.add(unit);
		}
		for(Unit unit : deadWall)
		{
			wall.remove(unit);
		}
		
		ArrayList<BuildCommand> finishedCommands = new ArrayList<BuildCommand>();
		ArrayList<BuildCommand> removeCommands = new ArrayList<BuildCommand>();
		ArrayList<BuildCommand> addCommands = new ArrayList<BuildCommand>();
		
		
		boolean goCheck = false;
		BuildCommand replacedCommand = null;
		
		for(BuildCommand com : underConstruction)
		{
			if(com.worker == null)
				continue;
			
			if(com.worker.unit.isIdle())
			{
				if(!checked)
				{
					checked = true;
					goCheck = true;
					replacedCommand = com;
					break;
				}
			}
		}
		
		if(goCheck)
		{
			for(Unit unit : game.getNeutralUnits())
			{
				for(int dx=0; dx<unit.getType().tileWidth(); dx++)
				{
					int x = unit.getTilePosition().getX() + dx;
					x = x-center.getX() + r;
					if(x < 0 || x >= 2*r+1)
						continue;
					
					for(int dy=0; dy<unit.getType().tileHeight(); dy++)
					{
						int y = unit.getTilePosition().getY() + dy;
						y = y-center.getY() + r;
						if(y < 0 || y >= 2*r+1)
							continue;
						buildable[x][y] = false;
					}
				}
			}

			buildPlan = new ArrayList<BuildCommand>();
			
			
			getWall();
			
			BuildCommand newCom = getCommand(replacedCommand.building);
			newCom.worker = replacedCommand.worker;
			replacedCommand.worker.unit.build(newCom.position, newCom.building);
			removeCommands.add(replacedCommand);
			addCommands.add(newCom);
		}
		
		for(BuildCommand com : underConstruction)
		{
			if(com.worker == null)
				continue;
			
			com.worker.drawCircle(Color.Yellow);
			game.drawTextMap(com.worker.unit.getPosition().getX(), com.worker.unit.getPosition().getY(), com.worker.unit.getOrder().toString());
			
			if(com.worker.unit.isConstructing() && com.worker.unit.getBuildUnit()!= null && !wall.contains(com.worker.unit.getBuildUnit()))
			{
				bot.reservedMinerals -= com.building.mineralPrice();
				bot.reservedGas -= com.building.gasPrice();
				wall.add(com.worker.unit.getBuildUnit());
				bot.hobos.add(com.worker);
				removeCommands.add(com);
				continue;
			}
			if(com.worker.unit.isIdle())
			{
				if(checked)
				{
					boolean done = false;
					for(Unit building : wall)
					{
						if(building == null)
							System.out.println("building is null!");
						else if (building.getTilePosition() == null)
							System.out.println("building.getTilePosition() is null!");
						else if (com == null)
							System.out.println("com is null!");
						else if (com.position == null)
							System.out.println("com.position is null!");
						
						if(building.getTilePosition().getX() 
								== 
								com.position.getX() && 
						   building.getTilePosition().getY() == com.position.getY())
							done = true;
					}
					if(!done)
						com.worker.unit.build(com.position, com.building);
					else
						finishedCommands.add(com);
				}
			}
		}
		
		for(BuildCommand com : finishedCommands)
		{
			bot.hobos.add(com.worker);
			com.worker = null;
			removeCommands.add(com);
		}
		
		for(BuildCommand com : removeCommands)
			underConstruction.remove(com);
		
		for(BuildCommand com : addCommands)
			underConstruction.add(com);
		
		
		for(int i=0; i<buildPlan.size(); i++)
		{
			int x = buildPlan.get(i).position.getX()*32, y = buildPlan.get(i).position.getY()*32;
			game.drawBoxMap(x, y, x+buildPlan.get(i).building.tileWidth()*32, y+buildPlan.get(i).building.tileHeight()*32, Color.Blue);
			game.drawTextMap(x, y, buildPlan.get(i).building.toString());
		}
	}
	
	public boolean[][] getSurroundingPlacements(boolean[][] firstBuilding, int w1, int h1, int w2, int h2)
	{
		boolean[][] result = new boolean[2*r+1][2*r+1];
		
		for(int x=0; x<2*r+1; x++)
			for(int y=0; y<2*r+1; y++)
			{
				if(firstBuilding[x][y])
				{
					for(int dx=-w2+1; dx < w1; dx++)
					{
						if(x+dx < 0)
							continue;
						if(x + dx + w2 > 2*r+1)
							continue;
						if(y-h2 > 0 && buildingClear(x+dx, y-h2, w2, h2))
							result[x+dx][y-h2] = true;
						if(y+h1 + h2 <= 2*r + 1 && buildingClear(x+dx, y+h1, w2, h2))
							result[x+dx][y+h1] = true;
					}
					

					for(int dy=-h2+1; dy < h1; dy++)
					{
						if(y+dy < 0)
							continue;
						if(y + dy + h2 > 2*r+1)
							continue;
						if(x-w2 > 0 && buildingClear(x-w2, y+dy, w2, h2))
							result[x-w2][y+dy] = true;
						if(x+w1 + w2 <= 2*r + 1 && buildingClear(x+w1, y+dy, w2, h2))
							result[x+w1][y+dy] = true;
					}
				}
			}
		
		return result;
	}

	private boolean buildingClear(int x, int y, int w, int h)
	{
		if (x + w > 2*r+1)
			return false;
		if (y + h > 2*r+1)
			return false;
		for(int dx = 0; dx < w; dx++)
			for(int dy = 0; dy<h; dy++)
				if(!buildable[x+dx][y+dy])
					return false;
		return true;
	}
}
