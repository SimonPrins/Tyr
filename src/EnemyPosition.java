import bwapi.Position;
import bwapi.UnitType;


public class EnemyPosition
{
	public UnitType type;
	public Position pos;
	

	public EnemyPosition(UnitType type, Position pos)
	{
		this.type = type;
		this.pos = pos;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(other.getClass() != this.getClass())
			return false;
		return pos.getX() == ((EnemyPosition)other).pos.getX() && pos.getY() == ((EnemyPosition)other).pos.getY();
	}
	
	
	@Override
	public int hashCode()
	{
		return pos.getX() + pos.getY();
	}
}
