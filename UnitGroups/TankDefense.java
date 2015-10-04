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

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;


public class TankDefense extends UnitGroup 
{
	public OutOfJob outOfJob = new OutOfJob();
	public Position rallyPoint = null;
	Position target = null;
	public int defensiveTanks = 2;
	
	public TankDefense(OutOfJob rejects, Position target)
	{
		super(rejects);
		this.target = target;
	}
	
	@Override
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if (target == null && bot.defensiveStructures.get(0).defenses.size() > 0)
			target = bot.defensiveStructures.get(0).defenses.get(0).getPosition();
		else if (target == null)
			target = bot.army.rallyPoint;
		else if (target == null)
			target = bot.getMainExit();
		
		for(Agent agent : units)
			if (agent.command.getClass() != DefensiveTank.class)
				agent.order(new DefensiveTank(agent, getPosition(game, self, bot, Tyr.positionToTile(target))));
	}
	
	private Position getPosition(Game game, Player self, Tyr bot, TilePosition aroundTile)
	{
		if (!BWTAInitializer.initialized)
			return Tyr.tileToPostion(aroundTile);
		
		for(int dist = 0; dist < 10; dist++)
		{
			for (int i=-dist; i <= dist; i++)
			{
				if (checkTile(game, self, bot, new TilePosition(aroundTile.getX() + i, aroundTile.getY() - dist)))
					return Tyr.tileToPostion(new TilePosition(aroundTile.getX() + i, aroundTile.getY() - dist));
				if (checkTile(game, self, bot, new TilePosition(aroundTile.getX() - dist, aroundTile.getY() + i)))
					return Tyr.tileToPostion(new TilePosition(aroundTile.getX() - dist, aroundTile.getY() + i));
				if (checkTile(game, self, bot, new TilePosition(aroundTile.getX() + i, aroundTile.getY() + dist)))
					return Tyr.tileToPostion(new TilePosition(aroundTile.getX() + i, aroundTile.getY() + dist));
				if (checkTile(game, self, bot, new TilePosition(aroundTile.getX() + dist, aroundTile.getY() + i)))
					return Tyr.tileToPostion(new TilePosition(aroundTile.getX() + dist, aroundTile.getY() + i));
			}
		}
		return Tyr.tileToPostion(aroundTile);
	}
	
	private boolean checkTile(Game game, Player self, Tyr bot,
			TilePosition pos) 
	{
		for(DefensiveStructures structures : bot.defensiveStructures)
		{
			for(Agent tank : structures.tanks.units)
			{
				if(tank.command.getClass() != DefensiveTank.class)
					continue;
				
				DefensiveTank defensive = (DefensiveTank)tank.command;
				TilePosition defendedTile = Tyr.positionToTile(defensive.target);
				if (Math.abs(defendedTile.getX() - pos.getX()) <= 1 && Math.abs(defendedTile.getY() - pos.getY()) <= 1)
					return false;
			}
		}
		
		for (int dx = -1; dx <= 1; dx++)
			for (int dy = -1; dy <= 1; dy++)
				if (pos.getX() + dx < 0 || pos.getY() + dy < 0 || pos.getX() + dx >= game.mapWidth() || pos.getY() + dy >= game.mapHeight() 
				|| bot.spaceManager.map[pos.getX() + dx][pos.getY() + dy] != 1)
					return false;
		
		return true;
	}

	@Override
	public void add(Agent agent)
	{
		super.add(agent);
		outOfJob.add(agent);
	}

	public void disable()
	{
		defensiveTanks = 0;
		while(units.size() > 0)
		{
			Agent agent = units.get(units.size()-1);
			units.remove(units.size() - 1);
			rejects.add(agent);
		}
	}
}
