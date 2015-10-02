import bwapi.Game;
import bwapi.Player;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;


public class StrategyDetector 
{
	public int opponentStrategy = ScoutGroup.unknown;
	
	public void onFrame(Game game, Player self, Tyr bot)
	{
		if (opponentStrategy != ScoutGroup.unknown)
			return;
		
		if(game.enemy().getRace() == Race.Protoss)
		{
			int nexusCount = 0;
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
			{
				if (enemy.type == UnitType.Protoss_Nexus)
					nexusCount++;
			}
			if(nexusCount >= 2)
			{
				opponentStrategy = ScoutGroup.tech;
				return;
			}
			for(Unit enemy : game.enemy().getUnits())
			{
				if(enemy.getType().gasPrice() > 0 
						|| enemy.getType() == UnitType.Protoss_Assimilator 
						|| enemy.getType() == UnitType.Protoss_Cybernetics_Core)
				{
					opponentStrategy = ScoutGroup.tech;
					return;
				}
			}
				int gatewayCount = 0;
				for(EnemyPosition enemy : bot.enemyBuildingMemory)
				{
					if (enemy.type == UnitType.Protoss_Photon_Cannon || enemy.type == UnitType.Protoss_Forge)
					{
						opponentStrategy = ScoutGroup.cannons;
						return;
					}
					if (enemy.type == UnitType.Protoss_Gateway)
						gatewayCount++;
				}
				if(gatewayCount >= 2)
				{
					opponentStrategy = ScoutGroup.zealotPush;
					return;
				}
		}
		if(game.enemy().getRace() == Race.Terran)
		{
			for(EnemyPosition enemy : bot.enemyBuildingMemory)
				if (enemy.type == UnitType.Terran_Bunker)
				{
					opponentStrategy = ScoutGroup.defensive;
					return;
				}
			
			int tankCount = 0;
			for(Unit enemy : game.enemy().getUnits())
			{
				if(enemy.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
						|| enemy.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
					tankCount++;
			}
			if(tankCount >= 3)
			{
				opponentStrategy = ScoutGroup.defensive;
				return;
			}
		}
	}
}
