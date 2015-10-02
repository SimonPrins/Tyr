import bwapi.Game;
import bwapi.Player;
import bwapi.UnitType;


public class ProtossTech extends BuildOrder
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
		
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Protoss_Gateway)*3 + bot.ccCount * 3)
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Protoss_Pylon);
		}
		
		//if we've the resources to build a Gateway ...
		if (bot.getAvailableMinerals()>= 150 && bot.count(UnitType.Protoss_Gateway) < 2*bot.count(UnitType.Protoss_Nexus)) 
		{
			bot.build(UnitType.Protoss_Gateway);
		}
		
		//if we've the resources to build a Cybernetics Core...
		if (bot.getAvailableMinerals()>= 150 && bot.count(UnitType.Protoss_Gateway) >= 1
				&& bot.count(UnitType.Protoss_Cybernetics_Core) < 1) 
		{
			bot.build(UnitType.Protoss_Cybernetics_Core);
		}
		
		if(bot.getAvailableMinerals()>= 100 && bot.count(UnitType.Protoss_Assimilator) == 0 && bot.count(UnitType.Protoss_Gateway) >= 1) 
		{
			bot.build(UnitType.Protoss_Assimilator);
		}
		
		if (bot.getAvailableMinerals() >= 400)
		{
			bot.build(UnitType.Protoss_Nexus);
		}
	}
}
