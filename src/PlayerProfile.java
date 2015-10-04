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

import java.util.ArrayList;

import bwapi.Game;
import bwapi.Player;
import bwapi.Race;


public class PlayerProfile 
{
	private Race race;
	private String[][] names;
	private BuildOrder counter;
	private int opponentStrategy;
	private int scoutTiming;
	private int nscouts;
	public boolean triggerDebug = false;
	
	public PlayerProfile(Race race, BuildOrder counter, String name)
	{
		this(race, counter, new String[][]{new String[]{name}});
	}
	
	public PlayerProfile(Race race, BuildOrder counter, String[][] names)
	{
		this(race, counter, names, ScoutGroup.unknown);
	}

	public PlayerProfile(Race race, BuildOrder counter, String[][] names, int opponentStrategy)
	{
		this(race, counter, names, opponentStrategy, -1, 0);
	}
	
	public PlayerProfile(Race race, BuildOrder counter, String[][] names, int opponentStrategy, int scoutTiming, int nscouts)
	{
		this.race = race;
		this.names = names;
		this.counter = counter;
		
		for(int i=0; i<names.length; i++)
		{
			for(int j=0; j<names[i].length; j++)
				names[i][j] = names[i][j].toLowerCase();
		}
		
		this.opponentStrategy = opponentStrategy;
		this.scoutTiming = scoutTiming;
		this.nscouts = nscouts;
	}
	
	public boolean match(Game game, Tyr bot)
	{	
		Player enemy = game.enemy();
		if (enemy.getRace() != race)
			return false;
		
		String nameLower = enemy.getName().toLowerCase();
		
		for (String[] name : names)
		{
			boolean matches = true;
			for (String namePart : name)
			{
				if (!nameLower.contains(namePart))
				{
					matches = false;
					break;
				}
			}
			
			if (triggerDebug)
				matches = true;
			
			if (matches)
			{
				bot.build = counter;
				bot.scout.opponentStrategy = opponentStrategy;
				if(nscouts != 0)
				{
					bot.scout.nscouts = nscouts;
					bot.scout.workerScoutTiming = scoutTiming;
				}
				return true;
			}
		}
		
		return false;
	}
	
	public static ArrayList<PlayerProfile> getProfiles()
	{
		ArrayList<PlayerProfile> result = new ArrayList<PlayerProfile>();
		
		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new TvP(), new String[][]{ 
			new String[]{"Tomas", "Vajda"},
			new String[]{"Thomas", "Vajda"},
			new String[] {"XIMP"}
		},
		ScoutGroup.cannons));
		
		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new TvP(), new String[][]{ 
			new String[]{"Jakub", "Trancik"}
		},
		ScoutGroup.cannons));
		
		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new TwoBaseMech(), new String[][]{ 
			new String[]{"Aiur"}, 
			new String[]{"Florian", "Richoux"}
		}));
		
		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new TvP(), new String[][]{ 
			new String[]{"Black", "White"}
		},
		ScoutGroup.zealotPush));
		
		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new Mech(), new String[][]{ 
			new String[]{"Odin"}
		}));
		
		//result.get(result.size()-1).triggerDebug = true;
		
		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new TvP(), new String[][]{ 
			new String[]{"Soeren", "Klett"},
			new String[]{"Soren", "Klett"},
			new String[]{"Sören", "Klett"}
		},
		ScoutGroup.zealotPush));
		
		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new TvP(), new String[][]{ 
			new String[]{"Dave", "Churchill"},
			new String[]{"ualbertabot"}
		},
		ScoutGroup.zealotPush));

		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new DefensiveBC(), new String[][]{ 
			new String[]{"Ian", "DaCosta"}
		},
		ScoutGroup.unknown));
		
		result.add(new PlayerProfile(Race.Protoss, (BuildOrder)new TvP(), new String[][]{ 
			new String[]{"Carsten", "Nielsen"}
		},
		ScoutGroup.zealotPush));
		
		result.add(new PlayerProfile(Race.Terran, (BuildOrder)new DefensiveBC(), new String[][]{ 
			new String[]{"Marek", "Kadek"}
		}));
		
		result.add(new PlayerProfile(Race.Terran, (BuildOrder)new GreedyBC(), new String[][]{ 
			new String[]{"Matej", "Istenik"}
		}));
		
		result.add(new PlayerProfile(Race.Terran, (BuildOrder)new GreedyBC(), new String[][]{ 
			new String[]{"Igor", "Lacik"}
		}));
		
		result.add(new PlayerProfile(Race.Terran, (BuildOrder)new GreedyBC(), new String[][]{ 
			new String[]{"Rafal", "Poniatowski"}
		}));

		result.add(new PlayerProfile(Race.Terran, (BuildOrder)new Mech(), new String[][]{ 
			new String[]{"tscmoo"}
		},
		ScoutGroup.unknown));

		result.add(new PlayerProfile(Race.Terran, (BuildOrder)new BBS(), new String[][]{ 
			new String[]{"ICE"}
		}));

		result.add(new PlayerProfile(Race.Terran, (BuildOrder)new GreedyBC(), new String[][]{ 
			new String[]{"Krasimir", "Krystev"},
			new String[]{"Krasibot"},
			new String[]{"Krasi0bot"}
		},
		ScoutGroup.unknown));

		result.add(new PlayerProfile(Race.Terran, (BuildOrder)new DefensiveBC(), new String[][]{ 
			new String[]{"Martin", "Rooijackers"},
			new String[]{"LetaBot"}
		},
		ScoutGroup.unknown));

		result.add(new PlayerProfile(Race.Zerg, (BuildOrder)new DefensiveBC(), new String[][]{ 
			new String[]{"tscmooz"}
		},
		ScoutGroup.unknown));
		
		return result;
	}
}
