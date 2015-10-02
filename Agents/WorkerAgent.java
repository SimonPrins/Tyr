import bwapi.Color;
import bwapi.Game;
import bwapi.Order;
import bwapi.Player;
import bwapi.Unit;


public class WorkerAgent extends Agent
{
	public long resetTimer = -1;
	public boolean isReset = false;
	
	public WorkerAgent(Unit unit)
	{
		super(unit);
	}

	public void onFrame(Game game, Player self, Tyr bot) 
	{
		if(isReset && unit.getOrder() == Order.MiningMinerals && unit.getTarget() != null && distanceSquared(unit.getTarget()) <= 50*50)
			isReset = false;
		
		if(game.getFrameCount() == resetTimer)
		{
			resetTimer = -1;
			isReset = true;
		}
		
		if(resetTimer != -1)
			game.drawTextMap(unit.getX(), unit.getY()+10, (resetTimer - game.getFrameCount()) + "");
		
		if(isReset)
		{
			drawCircle(Color.Red, 6);
			if(unit.getOrder() == Order.PlaceBuilding)
			{
				unit.stop();
				System.out.println("Place building cancelled.");
			}
		}
	}
	
	

}
