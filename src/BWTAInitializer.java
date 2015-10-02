import java.util.ArrayList;

import bwapi.Player;
import bwta.BWTA;
import bwta.BaseLocation;


public class BWTAInitializer implements Runnable
{
	public static boolean initialized = false;
	Tyr bot;
	Player self;
	
	public BWTAInitializer(Tyr bot, Player self)
	{
		this.bot = bot;
		this.self = self;
	}

	@Override
	public void run()
	{
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
		try
		{
			bot.expands = new ArrayList<BaseLocation>();
			bot.suspectedEnemy = new ArrayList<BaseLocation>();
			
	        System.out.println("Analyzing map...");
	        BWTA.readMap();
	        BWTA.analyze();
	        System.out.println("Map data ready");
	        
	        for(BaseLocation bloc : BWTA.getBaseLocations())
	        {
	        	if(!bloc.isStartLocation() || self.getStartLocation().getDistance(bloc.getTilePosition()) < 4)
	        		bot.expands.add(bloc);
	        	else
	        		bot.suspectedEnemy.add(bloc);
	        }
	        
	        initialized = true;
		}
		catch(Exception e)
		{
			System.out.println("Error intializing BWTA: " + e.getMessage());
			Tyr.game.printf("Error intializing BWTA: " + e.getMessage());
		}
	}

}
