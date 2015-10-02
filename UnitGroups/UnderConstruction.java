import java.util.ArrayList;

import bwapi.Color;
import bwapi.Game;
import bwapi.Player;
import bwapi.Race;


public class UnderConstruction extends UnitGroup
{
	ArrayList<RepairCommand> emergencyCommands = new ArrayList<RepairCommand>();
	
	public UnderConstruction(OutOfJob rejects)
	{
		super(rejects);
	}

	@Override
	public void onFrame(Game game, Player self, Tyr bot) 
	{
		ArrayList<RepairCommand> removeRepairs = new ArrayList<RepairCommand>();
		for(RepairCommand repair : emergencyCommands)
		{
			repair.target.drawCircle(Color.Teal);
			repair.worker.drawCircle(Color.Teal);
			
			if (repair.target.isDead())
			{
				removeRepairs.add(repair);
				continue;
			}
			
			if (repair.target.unit.isCompleted() || repair.worker.isDead())
			{
				removeRepairs.add(repair);
				units.add(repair.target);
				continue;
			}
		}
		
		for(RepairCommand repair : removeRepairs)
		{
			emergencyCommands.remove(repair);
			if (!repair.worker.isDead())
				rejects.add(repair.worker);
		}
		
		ArrayList<Agent> completed = new ArrayList<Agent>();
		ArrayList<Agent> repairing = new ArrayList<Agent>();
		for(Agent building : units)
		{
			if(building.unit.isCompleted())
			{
				completed.add(building);
				continue;
			}
			building.drawCircle(Color.White);
			
			if(self.getRace() == Race.Terran && building.unit.getBuildUnit() == null)
			{
				Agent worker = bot.workForce.pop(building.unit.getPosition());
				if(worker == null)
					continue;
				
				worker.unit.rightClick(building.unit);
				RepairCommand emergencyCommand = new RepairCommand(worker, building);
				emergencyCommands.add(emergencyCommand);
				repairing.add(building);
			}
		}
		
		for(Agent building : repairing)
			units.remove(building);
			
		for(Agent building : completed)
		{
			units.remove(building);
			rejects.add(building);
		}
	}
}
