import java.util.List;

import bwapi.Color;
import bwapi.Game;
import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;


public class BuilderGroup extends UnitGroup
{
	boolean clearEmptyMineralPatches = true;
	
	public BuilderGroup(OutOfJob rejects) 
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		for(BuildCommand buildCommand : bot.buildCommands)
		{
			if(buildCommand.removing)
				continue;
			if (((WorkerAgent)buildCommand.worker).isReset || buildCommand.worker.isDead()
				|| buildCommand.worker.unit.getBuildUnit() != null || buildCommand.worker.unit.isMorphing())
			{
				buildCommand.remove(bot);
				continue;
			}
			
			for(Unit unit : self.getUnits())
			{
				if (unit.getTilePosition().getX() == buildCommand.position.getX()
						&& unit.getTilePosition().getY() == buildCommand.position.getY()
					&& unit.getType() == buildCommand.building
						)
				{
					buildCommand.remove(bot);
					break;
				}
			}
		}
		
		for(BuildCommand buildCommand : bot.buildCommands)
		{
			if(buildCommand.removing)
				continue;
			
			buildCommand.worker.drawCircle(Color.Blue);
			bot.drawCircle(Tyr.tileToPostion(buildCommand.position), Color.Blue, 
					(int)Tyr.tileToPostion(buildCommand.position).getDistance(buildCommand.worker.unit.getPosition()));
			game.drawLineMap(Tyr.tileToPostion(buildCommand.position).getX(), Tyr.tileToPostion(buildCommand.position).getY(),
					buildCommand.worker.unit.getPosition().getX(), buildCommand.worker.unit.getPosition().getY(), Color.Blue);
        	game.drawTextMap(buildCommand.worker.unit.getPosition().getX(), buildCommand.worker.unit.getPosition().getY(),
        			buildCommand.worker.unit.getOrder().c_str());
			game.drawBoxMap(buildCommand.position.getX()*32, buildCommand.position.getY()*32,
					buildCommand.position.getX()*32 + 32, buildCommand.position.getY()*32 + 32,
					Color.Orange);
			
			Unit mineral = getMineralInRange(buildCommand.worker.unit.getPosition(), 50, game, true);
			if(mineral != null && clearEmptyMineralPatches)
			{
				if (!buildCommand.worker.unit.isGatheringMinerals())
					buildCommand.worker.unit.gather(mineral);
				((WorkerAgent)buildCommand.worker).resetTimer = buildCommand.getTimer();
				continue;
			}
        	
        	if (!buildCommand.worker.unit.isConstructing() &&  
        			(buildCommand.worker.unit.isGatheringMinerals() ||
        			(buildCommand.worker.unit.getOrder() != Order.PlaceBuilding 
        			&& (!buildCommand.worker.unit.isMoving() || buildCommand.worker.unit.getOrder() == Order.PlayerGuard))))
			{
				if (game.canBuildHere(buildCommand.worker.unit, buildCommand.position, buildCommand.building, false))
				{
					if (buildCommand.worker.unit.getTilePosition().getDistance(buildCommand.position) <= 60)
						buildCommand.worker.unit.build(buildCommand.position, buildCommand.building);
					else
						buildCommand.worker.unit.move(Tyr.tileToPostion(buildCommand.position));
					continue;
				}
				
				TilePosition newpos = null;
				if(!buildCommand.fixed)
					newpos = bot.getBuildTile(buildCommand.worker.unit, buildCommand.building, buildCommand.position);
				
				if (newpos == null)
				{
					buildCommand.remove(bot);
					game.printf("No suitable placement found.");
					continue;
				}
				
				buildCommand.position = newpos;
				if (buildCommand.worker.unit.getTilePosition().getDistance(newpos) <= 20)
				{
					buildCommand.worker.unit.build(newpos, buildCommand.building);
					continue;
				}
				
				if (!(buildCommand.building.isResourceDepot() && buildCommand.worker.unit.getDistance(Tyr.tileToPostion(newpos)) > 300))
				{
					buildCommand.worker.unit.move(new Position(newpos.getX()*32, newpos.getY()*32));
					continue;
				}
				mineral = getMineralInRange(Tyr.tileToPostion(newpos), 270, game, false);
				if (mineral != null)
					buildCommand.worker.unit.gather(mineral);
				else
					buildCommand.worker.unit.move(new Position(newpos.getX()*32, newpos.getY()*32));
			}
		}
		
		for(int i=0; i<bot.buildCommands.size(); i++)
		{
			BuildCommand com = bot.buildCommands.get(i);
			if(com.removing)
			{
				bot.buildCommands.set(i, bot.buildCommands.get(bot.buildCommands.size()-1));
				bot.buildCommands.remove(bot.buildCommands.size()-1);
				
				rejects.add(com.worker);
				remove(com.worker);
			}
		}
	}
	
	private Unit getMineralInRange(Position pos, int range, Game game, boolean blocking)
	{
		List<Unit> inRange = game.getUnitsInRadius(pos, 270);
		for(Unit mineral : inRange)
			if (mineral.getType().isMineralField() && (!blocking || mineral.getResources() == 0))
				return mineral;
		return null;
	}

}
