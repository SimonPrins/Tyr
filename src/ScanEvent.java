import bwapi.Position;


public class ScanEvent 
{
	public Position pos;
	public int time;
	
	public ScanEvent(Position pos, int time)
	{
		this.pos = pos;
		this.time = time;
	}

	public boolean inRange(Position position)
	{
		return pos.getDistance(position) <= 320;
	}
}
