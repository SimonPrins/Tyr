/*
* Tyr is an AI for StarCraft: Broodwar, 
* 
* Please visit https://github.com/SimonPrins/Tyr for further information.
* 
* Copyright 2015 Simon Prins
*
* This file is part of Tyr.
* Tyr is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
* Tyr is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* You should have received a copy of the GNU General Public License
* along with Tyr.  If not, see http://www.gnu.org/licenses/.
*/

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
