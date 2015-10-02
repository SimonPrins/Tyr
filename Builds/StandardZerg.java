import bwapi.Game;
import bwapi.Player;
import bwapi.UnitType;


public class StandardZerg extends BuildOrder
{

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		bot.maximumWorkers = 12;
		
		if (bot.getAvailableMinerals() >= 200 && bot.count(UnitType.Zerg_Spawning_Pool) == 0)
		{
			bot.build(UnitType.Zerg_Spawning_Pool);
		}
		
		//if (bot.getAvailableMinerals() >= 300)
		//{
		//	bot.build(UnitType.Zerg_Hatchery);
		//}
	}

}
