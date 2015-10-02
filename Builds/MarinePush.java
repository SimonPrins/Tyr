import bwapi.Game;
import bwapi.Player;
import bwapi.UnitType;


public class MarinePush extends BuildOrder
{
	boolean scoutRequested = false;

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(game.getFrameCount() >= 1600 && !scoutRequested)
		{
			scoutRequested = true;
			bot.scout.requestWorkerScout(bot);
		}
		
		bot.army.requiredSize = 20;
		bot.army.maximumSize = 60;
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Terran_Barracks)*3 + bot.ccCount * 3)
        		  && (bot.getAvailableMinerals() >= 100)
        		  && self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Terran_Supply_Depot);
		}
		
		//if we've the resources to build a barracks ...
		if (bot.getAvailableMinerals()>= 150 && (bot.count(UnitType.Terran_Barracks) < Math.min(bot.ccCount * 2, 10) + 1)) 
		{
			bot.build(UnitType.Terran_Barracks);
		}
		
		if (bot.getAvailableMinerals() >= 400)
		{
			bot.build(UnitType.Terran_Command_Center);
		}
	}
}
