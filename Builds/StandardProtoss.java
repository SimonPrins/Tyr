import bwapi.Game;
import bwapi.Player;
import bwapi.UnitType;


public class StandardProtoss extends BuildOrder
{

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		//if we're running out of supply and have enough minerals ...
		if ((self.supplyTotal() + bot.supplyConstructing - self.supplyUsed() <= bot.count(UnitType.Protoss_Gateway)*3 + bot.ccCount * 3)
				&& (bot.getAvailableMinerals() >= 100)
				&& self.supplyTotal() + bot.supplyConstructing < 400)
		{
			bot.build(UnitType.Protoss_Pylon);
		}
		
		//if we've the resources to build a Gateway ...
		if (bot.getAvailableMinerals()>= 150) 
		{
			bot.build(UnitType.Protoss_Gateway);
		}
		
		if (bot.getAvailableMinerals() >= 400)
		{
			bot.build(UnitType.Protoss_Nexus);
		}
	}
}
