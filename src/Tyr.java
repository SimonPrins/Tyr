import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;


/*
 * This is the main class, implementing the basic structure of the bot.
 *  
 */
public class Tyr extends DefaultBWListener 
{

	/**
	 * The BWAPI Mirror for connecting to the game of Brood War.
	 */
	private Mirror mirror = new Mirror();

	/**
	 * The BWAPI Game, for getting information about the current game state.
	 */
    public static Game game;
    
    /**
     * The Player object representing this bot in the game.
     */
    public Player self;
    
    /**
     * The amount of minerals currently reserved for buildings.
     */
    public int reservedMinerals;

    /**
     * The amount of gas currently reserved for buildings.
     */
    public int reservedGas;
    
    /**
     * Set of all known enemy buildings.
     */
    public HashSet<EnemyPosition> enemyBuildingMemory = new HashSet<EnemyPosition>();
    
    /**
     * List of all known enemy defensive structures. This is a subset of the known enemy buildings.
     */
    public ArrayList<EnemyPosition> enemyDefensiveStructures;
    
    /**
     * List of all known neutral structures (e.g. mineral patches, destructible buildings).
     */
    public ArrayList<EnemyPosition> neutralStructures;
    
    /**
     * List of all known BaseLocations that are not starting locations.
     * 
     * If we are not sure yet whether a base is a starting location for an enemy, (because we haven't scouted it yet), then the base is not included in this list.
     * 
     * While BWTA is still initialising we do not know the base locations, so this list will be empty until initialisation is done.
     */
    public ArrayList<BaseLocation> expands = new ArrayList<BaseLocation>();
    
    /**
     * List of all starting location where the enemy may have started.
     * As we scout these and find out whether or not they are the enemy's starting base, we update the list.
     */
    public ArrayList<BaseLocation> suspectedEnemy = new ArrayList<BaseLocation>();
    
    /**
     * List of commands for buildings that are to be built.
     * As soon as building starts, the command is removed from the list, even though the building has not finished yet.
     */
    public ArrayList<BuildCommand> buildCommands = new ArrayList<BuildCommand>();
    
    /**
     * List of locations where we have defensive structures.
     * The defensive structures class also keeps track of what structures exist at each location.
     */
    public ArrayList<DefensiveStructures> defensiveStructures = new ArrayList<DefensiveStructures>();
    
    /**
     * Determines if the bot is in defense mode, where it is trying to defend its main base.
     */
    private boolean defenseMode = false;
    
    /**
     * Timer used to make sure certain commands are updated while not being spammed every frame.
     */
    public int defenseTime = 0;
    
    /**
     * The distance to our starting location at which an enemy unit is considered an invader. 
     */
    public int invasionDist = 1024;
    
    /**
     * The closest enemy unit to our starting location, or null if no enemy is closer than invasionDist.
     */
    public Unit invader;
    
    /**
     * The number of enemy units closer than invasionDist to our starting location.
     */
    public int invaderCount = 0;
    
    /**
     * True if there is at least one enemy unit closer than invasionDist to our starting location.
     */
    public boolean areWeBeingInvaded;

    /*
     * All the different unit groups.
     * Each unit group manages a certain group of our units.
     */
    public ComsatNetwork comsatNetwork;
    public WraithSwarm swarm;
    public Army army;
    public Bunkers bunkers;
    public ScoutGroup scout;
    public DefendingWorkers militia;
    public WorkerGroup workForce;
    public BuilderGroup builders;
    public ProductionStructures production;
    public UnderConstruction underConstruction;
    public OutOfJob hobos;
    
    /**
     * List of all the unit groups.
     */
    public ArrayList<UnitGroup> groups = new ArrayList<UnitGroup>();
    
    /**
     * The number of workers that are to be sent to each gas.
     * Can be used to increase or decrease gas mining.
     */
    public int workersPerGas = 1;
    
    /**
     * The number of Siege Tanks we want defending each defensive position.
     */
    public int defensiveTanks = 0;

    /**
     * Keeps track of building placements and allows you to find new positions for buildings.
     * Also helps in finding positions for defending Siege Tanks to siege up.
     */
    public SpaceManager spaceManager;
    
    /**
     * Keeps track of which parts of the map have been seen at some point.
     * When the enemy's base is destroyed but he still has buildings somewhere on the map, this class takes over control of units to try to find them.
     */
    public Scanner scanner;
    
    /**
     * Helps finding a walloff placement and helps managing it.
     * WARNING: this system has not been used in any recent build and may no longer work.
     */
    public WallOff wallOff;
    
    /**
     * Keeps track of how many units of each unit type the player has.
     */
    public HashMap<UnitType, Integer> unitCounts = new HashMap<UnitType, Integer>();
    
    /**
     * Keeps track of the number of command centers, nexi, and hatcheries.
     */
    public int ccCount = 0;
    
    /**
     * Keeps track of the number of free geysers with a nearby resource depot.
     */
    public int geyserCount = 0;
    
    /**
     * The amount of supply that will be added when all currently constructing buildings finish.
     */
    public int supplyConstructing = 0;
    
    /**
     * The buildorder that is being executed.
     */
    public BuildOrder build;

    /**
     * The maximum number of workers that is to be constructed.
     */
	public int maximumWorkers = 45;
	
	/**
	 * Static reference to the Singleton Tyr object.
	 */
	public static Tyr bot;
	
	/**
	 * Stopwatch for measuring the amount of time various parts of the program take.
	 */
	private StopWatch stopWatch = new StopWatch();
	
	/**
	 * The amount of wins and losses as recorded in the /read/ folder.
	 */
    public int wins = 0;
    public int losses = 0;
    
    /**
     * Class that tries to determine what strategy the opponent is using.
     * This is only used in the recording in the /read/ folder.
     * For in game purposes, the Scout class is used.
     */
    StrategyDetector strategyDetector = new StrategyDetector();

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    
    public HashMap<Integer, Agent> agentMap = new HashMap<Integer, Agent>();
    
    
    @Override
    public void onUnitCreate(Unit unit) 
    {
    	Agent agent = Agent.createAgent(unit);
    	agentMap.put(unit.getID(), agent);
    	
    	hobos.add(agent);
    	if (unit.getType() == UnitType.Terran_Bunker
    			|| unit.getType() == UnitType.Terran_Missile_Turret)
    	{
    		
    		DefensiveStructures structures = null;
    		for (Unit builder : self.getUnits())
    		{
    			if (!builder.getType().isWorker())
    				continue;
    			if (builder.getBuildUnit() != unit)
    				continue;
    			Agent builderAgent = agentMap.get(builder.getID());
    			if (builderAgent.command == null)
    				continue;
    			
    			if (!builderAgent.command.getClass().equals(BuildDefensive.class))
        			continue;
    			
    			

				structures = ((BuildDefensive)builderAgent.command).defensePos;
				break;
    		}
    		if (structures != null)
    			structures.add(unit);
    		else
    			System.out.println("No matching BuildDefensive command found.");
    	}
    	else if(unit.getType().isResourceDepot())
			workForce.newBase(unit);
    }
    
    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();

        
        try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Thread t = new Thread(new BWTAInitializer(this, self));
        t.start();
        
        try
        {

        spaceManager = new SpaceManager(game);
        scanner = new Scanner(game);
        wallOff = null;

        if (self.getRace() == Race.Terran)
        {
        	if (game.enemy().getRace() == Race.Zerg)
        		build = new TvZ();
        	else if (game.enemy().getRace() == Race.Protoss)
        		build = new TwoBaseBC();
        	else
        		build = new Mech();
        }
        else if (self.getRace() == Race.Protoss)
        	build = new ProtossTech();
        else
        	build = new StandardZerg();
        
        hobos = new OutOfJob();
        bunkers = new Bunkers(hobos);
        underConstruction = new UnderConstruction(hobos);
        production = new ProductionStructures(hobos);
        builders = new BuilderGroup(hobos);
        militia = new DefendingWorkers(hobos);
        workForce = new WorkerGroup(hobos);
        
        int requiredArmySize = -1;
        if (self.getRace() == Race.Zerg)
        	requiredArmySize = 5;
        else if (self.getRace() == Race.Protoss)
        	requiredArmySize = 15;
        else if(game.enemy().getRace() == Race.Zerg)
        	requiredArmySize = 15;
        else if(game.enemy().getRace() == Race.Protoss)
        	requiredArmySize = 40;
        else
        	requiredArmySize = 30;

        int maxArmySize = -1;
        if (self.getRace() == Race.Zerg)
        	maxArmySize = 5;
        else if (self.getRace() == Race.Protoss)
        	maxArmySize = 30;
        else if(game.enemy().getRace() == Race.Terran)
        	maxArmySize = 60;
        else if (game.enemy().getRace() == Race.Zerg)
        	maxArmySize = 30;
        else
        	maxArmySize = 60;
        
        army = new Army(hobos, requiredArmySize, maxArmySize);
        comsatNetwork = new ComsatNetwork(hobos);
        scout = new ScoutGroup(hobos);
		
        swarm = new WraithSwarm(hobos);
        
        
        groups.add(hobos);
        groups.add(bunkers);
        groups.add(underConstruction);
        groups.add(production);
        groups.add(builders);
        groups.add(militia);
        groups.add(workForce);
        groups.add(army);
        groups.add(comsatNetwork);
        groups.add(scout);
        groups.add(swarm);
        
        ArrayList<String> records = DebugMessages.readFile();
        for(String s : records)
        {
        	if (s.startsWith("win"))
        		wins++;
        	else
        		losses++;
        }
        
        
        
        ArrayList<PlayerProfile> profiles = PlayerProfile.getProfiles();
        for(PlayerProfile pp : profiles)
        	if (pp.match(game, this))
        		break;
        }
        catch(Exception e)
        {
        	DebugMessages.addMessagePermanent("Error starting up: " + e.toString());
        	e.printStackTrace();
            quickStartup();
            throw e;
        }
    }
    
    public void quickStartup()
    {
    	DebugMessages.addMessagePermanent("Error starting, doing quick startup.");
    	System.out.println("Error starting, doing quick startup.");
    	
    	groups = new ArrayList<UnitGroup>();
    	
        spaceManager = new SpaceManager(game);
        scanner = new Scanner(game);
        wallOff = null;

        if (self.getRace() == Race.Terran)
        	build = new TvP();
        else if (self.getRace() == Race.Protoss)
        	build = new ProtossTech();
        else
        	build = new StandardZerg();
        
        hobos = new OutOfJob();
        bunkers = new Bunkers(hobos);
        underConstruction = new UnderConstruction(hobos);
        production = new ProductionStructures(hobos);
        builders = new BuilderGroup(hobos);
        militia = new DefendingWorkers(hobos);
        workForce = new WorkerGroup(hobos);
        
        int requiredArmySize = 20;

        int maxArmySize = 40;
        
        army = new Army(hobos, requiredArmySize, maxArmySize);
        comsatNetwork = new ComsatNetwork(hobos);
        scout = new ScoutGroup(hobos);
		
        swarm = new WraithSwarm(hobos);
        
        
        groups.add(hobos);
        groups.add(bunkers);
        groups.add(underConstruction);
        groups.add(production);
        groups.add(builders);
        groups.add(militia);
        groups.add(workForce);
        groups.add(army);
        groups.add(comsatNetwork);
        groups.add(scout);
        groups.add(swarm);
    }
    
    public int getAvailableMinerals()
    {
    	return self.minerals() - reservedMinerals;
    }
    
    public int getAvailableGas()
    {
    	return self.gas() - reservedGas;
    }
    
    long initTime;
    long buildTime;
    long unitGroupsTime;
    long agentTime;

    @Override
    public void onFrame() {
    	if (game.isReplay())
    		return;
    	
        game.setTextSize(4);
        
        stopWatch.start();
        
        if (neutralStructures == null)
        {
        	neutralStructures = new ArrayList<EnemyPosition>();
        	
        	for(Unit unit : game.getNeutralUnits())
        		neutralStructures.add(new EnemyPosition(unit.getType(), unit.getPosition()));
        }
    	
        if (BWTAInitializer.initialized)
			drawCircle(this.getMainExit(), Color.Green, 128);
        
        unitCounts = new HashMap<UnitType, Integer>();
        ccCount = 0;
        supplyConstructing = 0;
        
        List<Unit> myUnits = self.getUnits();
        

		geyserCount = 0;
		if (bot == null)
			System.out.println("bot is null.");
		else if (bot.workForce == null)
			System.out.println("bot.workForce is null.");
		else if (bot.workForce.mineralWorkers == null)
			System.out.println("bot.workForce.mineralWorkers is null.");
		for (MineralWorkers base : bot.workForce.mineralWorkers)
		{
			if (base.gasWorkers != null && base.gasWorkers.geyser.getType() == UnitType.Resource_Vespene_Geyser) 
			{
				boolean alreadyBuilt = false;
				for(BuildCommand command : bot.buildCommands)
				{
					if (command.position.getX() == base.gasWorkers.geyser.getX() && command.position.getY() == base.gasWorkers.geyser.getY())
					{
						alreadyBuilt = true;
						break;
					}
				}
				if (!alreadyBuilt)
					geyserCount++;
			}
		}
        
        if (myUnits == null)
        {
        	DebugMessages.addMessage("Whoops, getUnits is null!");
        }
        else
        {
	        for (Unit myUnit : myUnits) 
	        {
	        	if(!unitCounts.containsKey(myUnit.getType()))
	        		unitCounts.put(myUnit.getType(), 1);
	        	else
	        		unitCounts.put(myUnit.getType(), unitCounts.get(myUnit.getType())+1);
	        	if (myUnit.getType().isResourceDepot())
	        		ccCount++;
	        	if (myUnit.isBeingConstructed() && !myUnit.getType().isResourceDepot())
	        		supplyConstructing += myUnit.getType().supplyProvided();
	        	if (myUnit.isMorphing() && myUnit.getBuildType() == UnitType.Zerg_Overlord)
	        		supplyConstructing += UnitType.Zerg_Overlord.supplyProvided();
	        }
        }
        
        for(BuildCommand command : buildCommands)
        {
        	if(!unitCounts.containsKey(command.building))
        		unitCounts.put(command.building, 1);
        	else
        		unitCounts.put(command.building, unitCounts.get(command.building)+1);
        	
        	if (command.building.isResourceDepot())
        		ccCount++;
        	
        	if (!command.building.isResourceDepot())
        		supplyConstructing += command.building.supplyProvided();
        }
        
        if (wallOff != null)
        {
        	for(BuildCommand command : wallOff.underConstruction)
        	{
        		if(!unitCounts.containsKey(command.building))
        			unitCounts.put(command.building, 1);
        		else
        			unitCounts.put(command.building, unitCounts.get(command.building)+1);
        	
        		if (command.building.isResourceDepot())
        			ccCount++;
        	
        		if (!command.building.isResourceDepot())
        			supplyConstructing += command.building.supplyProvided();
        	}
        }
        
        for(UnitGroup group : groups)
        	group.cleanup();

        for(int i=0; i<defensiveStructures.size(); i++)
        {
        	if (defensiveStructures.get(i).disabled)
        	{
        		defensiveStructures.remove(i);
        		i--;
        	}
        }
    	
        ArrayList<Agent> reEmployed = new ArrayList<Agent>();
        for(Agent agent : hobos.units)
        {
        	if(agent.unit.getType() == UnitType.Terran_Comsat_Station)
        	{
        		comsatNetwork.add(agent);
        		reEmployed.add(agent);
        	}
        	else if(agent.unit.getType() == UnitType.Terran_Wraith)
        	{
        		swarm.add(agent);
        		reEmployed.add(agent);
        	}
        	else if(agent.unit.getType() == UnitType.Terran_Dropship)
        	{
        		DropHarass dh = new DropHarass(hobos);
        		dh.setDropship(agent);
            	reEmployed.add(agent);
        		army.dropHarass.add(dh);
        	}
        	else if (agent.unit.getType().canAttack() 
        			&& ! agent.unit.getType().isWorker() 
        			&& !agent.unit.getType().isBuilding() 
        			&& agent.unit.isCompleted())
        	{
        		army.add(agent);
        		reEmployed.add(agent);
        	}
        	else if(agent.unit.getType().isWorker() && agent.unit.isCompleted())
        	{
        		workForce.add(agent);
        		reEmployed.add(agent);
        	}
        	else if((agent.unit.getType().canProduce() 
        			|| agent.unit.getType() == UnitType.Terran_Engineering_Bay
        			|| agent.unit.getType() == UnitType.Terran_Machine_Shop
        			|| agent.unit.getType() == UnitType.Terran_Science_Facility
        			|| agent.unit.getType() == UnitType.Terran_Physics_Lab
        			|| agent.unit.getType() == UnitType.Terran_Armory
        			) && agent.unit.isCompleted())
        	{
        		production.add(agent);
        		reEmployed.add(agent);
        	}
        	else if(agent.unit.getType().isBuilding() && !agent.unit.isCompleted())
        	{
        		underConstruction.add(agent);
        		reEmployed.add(agent);
        	}
        	else if(agent.unit.getType() == UnitType.Terran_Bunker && agent.unit.isCompleted())
        	{
        		bunkers.add(agent);
        		reEmployed.add(agent);
        	}
        }
        for(Agent agent : reEmployed)
        	hobos.remove(agent);
        
        if (scout.units.size() < scout.scoutCount && army.units.size() > 4 && bunkers.bunkersAreManned()
        		&& (self.getRace() != Race.Zerg || army.units.size() >= 12))
        {
        	Agent agent = army.pop();
        	if(agent != null)
        	{
        		agent.order(new None(agent));
        		scout.add(agent);
        	}
        }
        
        invader = null;
        areWeBeingInvaded = false;
        
        ArrayList<EnemyPosition> removePositions = new ArrayList<EnemyPosition>();
        
        for(EnemyPosition p : enemyBuildingMemory)
        	if (game.isVisible(p.pos.getX()/32, p.pos.getY()/32))
        		removePositions.add(p);
        
        for(EnemyPosition p : removePositions)
        	enemyBuildingMemory.remove(p);
        
        invaderCount = 0;
        
        //always loop over all currently visible enemy units (even though this set is usually empty)
        for (Unit u : game.enemy().getUnits()) {
        	
        	if (u.getType() == UnitType.Protoss_Observer)
        		continue;
        	
        	double uDist = u.distanceTo(self.getStartLocation().getX()*32, self.getStartLocation().getY()*32); 
        	
			if (uDist <= invasionDist)
        	{
        		if (invader == null || uDist < invader.distanceTo(self.getStartLocation().getX()*32, self.getStartLocation().getY()*32))
        			invader = u;
        		invaderCount++;
        	}
        	
        	//if this unit is in fact a building
        	if (u.getType().isBuilding() && !u.isLifted()) 
        	{
        		EnemyPosition enemyPos = new EnemyPosition(u.getType(), u.getPosition());
        		//check if we have it's position in memory and add it if we don't
        		if (!enemyBuildingMemory.contains(enemyPos)) enemyBuildingMemory.add(enemyPos);
        	}
        }
        
        enemyDefensiveStructures = new ArrayList<EnemyPosition>();
        
        for(EnemyPosition p : enemyBuildingMemory)
        {
        	drawCircle(p.pos, Color.Red);
        	if(p.type == UnitType.Protoss_Photon_Cannon)
        	{
            	enemyDefensiveStructures.add(p);
        		drawCircle(p.pos, Color.Red, WeaponType.Phase_Disruptor_Cannon.maxRange());
        	}
        }
        
        areWeBeingInvaded = invader != null && !defenseMode;
        if (areWeBeingInvaded)
        	defenseTime = -1;
        defenseMode = invader != null;
        defenseTime++;
        if (defenseTime >= 200)
        {
        	defenseTime = 0;
        	if (defenseMode)
        		areWeBeingInvaded = true;
        }
        
        if(invader != null)
        	drawCircle(invader.getPosition(), Color.Red);
        
        spaceManager.onFrame(game, self, this);
        scanner.onFrame(game, self, this);
        strategyDetector.onFrame(game, self, bot);
        
        
        initTime = stopWatch.time();
        
       	build.onFrame(game, self, this);
        
        for(DefensiveStructures structures : defensiveStructures)
        	structures.onFrame(game, self, this);
        
        buildTime = stopWatch.time();

           
       	for(UnitGroup group : groups)
       		group.onFrame(game, self, this);
           
       	unitGroupsTime = stopWatch.time();
       	
       	for(Unit unit : self.getUnits())
       	{
       		Agent agent = agentMap.get(unit.getID());
       		agent.command.execute(game, self, bot);
       		if (agent.unit.getType().isWorker())
       		{
       			((WorkerAgent)agent).onFrame(game, self, bot);
       		}
       	}

       	agentTime = stopWatch.time();
       	
       	game.setTextSize(6);

       	DebugMessages.addMessage("Reserved Minerals: " + reservedMinerals);
       	DebugMessages.addMessage("Army: " + army.units.size());
       	DebugMessages.addMessage("Workers: " + (workForce.units.size() + builders.units.size()));
       	if (suspectedEnemy.size() != 1)
       		DebugMessages.addMessage("Suspected enemy bases: " + suspectedEnemy.size());
       	DebugMessages.addMessage("Bases: " + (workForce.mineralWorkers.size()));
       	DebugMessages.addMessage("Frame count: " + game.getFrameCount());
       	if (!BWTAInitializer.initialized)
           	DebugMessages.addMessage("Initializing BWTA.");
       	//DebugMessages.addMessage("Enemy strategy: " + (scout.getEnemyStrategy()));
       	DebugMessages.addMessage("Initialization Time: " + initTime);
       	DebugMessages.addMessage("Build Time: " + buildTime);
       	DebugMessages.addMessage("Unit Groups Time: " + unitGroupsTime);
       	DebugMessages.addMessage("Agent Time: " + agentTime);
       	
       	DebugMessages.toScreen(game);
       	
       	if(game.getFrameCount() <= 300)
           	game.drawTextScreen(230, 210, "Good luck, have fun!!! :D");
    }
    
    @Override
    public void onEnd(boolean win)
    {
    	DebugMessages.saveMessage((win?"win":"loss") + " " + strategyDetector.opponentStrategy);
    }
    
	public void drawCircle(Position position, Color color, int r)
	{
    	game.drawCircleMap(position.getX(),
    			position.getY(),
    			r, color);
	}
    
    public void drawCircle(Position position, Color color)
    {
    	drawCircle(position, color, 10);
	}
    
    public int count(UnitType type)
    {
    	Integer result = unitCounts.get(type);
    	if(result == null)
    		return 0;
    	else
    		return result;
    }
    
    private Position mainExit = null;
    
    public Position getMainExit()
    {
    	if(!BWTAInitializer.initialized)
    		return null;
    	
    	if (mainExit != null)
    		return mainExit;
    	
    	int baseCount = BWTA.getStartLocations().size();
    	
    	Region current = BWTA.getRegion(self.getStartLocation());
		if (current == null)
			return null;

		Position target = null;
		double bestDistance = -1;
		List<Chokepoint> chokepoints = current.getChokepoints();
		if (chokepoints == null)
			return null;
		
		if (chokepoints.size() == 1)
		{
			mainExit = chokepoints.get(0).getCenter();
			return mainExit;
		}
		for(Chokepoint choke : chokepoints)
		{
			double distance = getClosestNeutralDistance(choke.getCenter());
			if(distance != -1 && distance < 128 && baseCount > 2)
				continue;
			
			distance = Math.min(choke.getX(), choke.getY());
			distance = Math.min(distance, game.mapWidth()*32 - choke.getX());
			distance = Math.min(distance, game.mapHeight()*32 - choke.getY());
			
			if (distance >= 1024)
				continue;
			
			if (target == null || distance > bestDistance)
			{
				target = choke.getCenter();
				bestDistance = distance;
			}
		}
		
		if (target == null)
		{
			for(Chokepoint choke : chokepoints)
			{
				double distance;
				
				distance = Math.min(choke.getX(), choke.getY());
				distance = Math.min(distance, game.mapWidth()*32 - choke.getX());
				distance = Math.min(distance, game.mapHeight()*32 - choke.getY());
				
				if (target == null || distance > bestDistance)
				{
					target = choke.getCenter();
					bestDistance = distance;
				}
			}
		}
		
		mainExit = target;
		return mainExit;
    }
    
    double getClosestNeutralDistance(Position pos)
    {
    	double result = -1;
    	for(EnemyPosition neutral : neutralStructures)
    	{
    		if (neutral.type == UnitType.Resource_Mineral_Field || neutral.type == UnitType.Resource_Mineral_Field_Type_2
   				 || neutral.type == UnitType.Resource_Mineral_Field_Type_3 || neutral.type == UnitType.Resource_Vespene_Geyser)
    			continue;
    		double distance = pos.getDistance(neutral.pos);
    		if(result == -1)
    			result = distance;
    		else result = Math.min(result, distance);
    	}
    	return result;
    }
    
    public TilePosition addDefensePos(Unit builder, UnitType building, DefensiveStructures defenses)
    {
    	for(Unit structure : defenses.defenses)
    	{
    		TilePosition pos = structure.getTilePosition();
    		for(int dx = 1-building.tileWidth(); dx < building.tileWidth(); dx++)
    		{
    			int x1 = pos.getX() + dx;
    			int y1 = pos.getY() - building.tileHeight();
    			int y2 = pos.getY() + structure.getType().tileHeight();

    			if(canBuildHere(builder, x1, y1, building))
    				return new TilePosition(x1, y1);
    			if(canBuildHere(builder, x1, y2, building))
    				return new TilePosition(x1, y2);
    		}
    		
    		for(int dy = 1-building.tileHeight(); dy < building.tileHeight(); dy++)
    		{
    			int x1 = pos.getX() - building.tileWidth();
    			int x2 = pos.getX() + structure.getType().tileWidth();
    			int y1 = pos.getY() +dy;

    			if(canBuildHere(builder, x1, y1, building))
    				return new TilePosition(x1, y1);
    			if(canBuildHere(builder, x2, y1, building))
    				return new TilePosition(x2, y1);
    		}
    	}
    	
    	return null;
    }
    
    public TilePosition getDefensePos(DefensiveStructures structures)
    {
		Position center = structures.getDefensePos();
		if (center == null)
			return null;
		
		double dist = center.getDistance(structures.defendedPosition);
		
		center = new Position((int)(center.getX() + (structures.defendedPosition.getX() - center.getX())*100/dist), 
				(int)(center.getY() + (structures.defendedPosition.getY() - center.getY())*100/dist));
		return new TilePosition(center.getX()/32, center.getY()/32);
    }
    
    public static Unit findClosestMineral(Position p)
    {
    	Unit closestMineral = null;
        
        //find the closest mineral
        for (Unit neutralUnit : game.neutral().getUnits()) 
            if (neutralUnit.getType().isMineralField()) 
                if (closestMineral == null || neutralUnit.getDistance(p) < closestMineral.getDistance(p)) 
                    closestMineral = neutralUnit;
        
        return closestMineral;
    }
    
    public boolean buildDefensive(UnitType building, DefensiveStructures defensePos)
    {
    	Position pos = defensePos.getDefensePos();
    	if (pos == null)
    		pos = Tyr.tileToPostion(self.getStartLocation());
		Agent worker = workForce.pop(pos);
		if (worker == null)
			return false;
		
		TilePosition target = positionToTile(defensePos.getDefensePos());
		if (target == null)
		{
			workForce.add(worker);
			return false;
		}
		

		TilePosition newDefensePos = addDefensePos(worker.unit, building, defensePos);
		if(newDefensePos != null)
		{
			BuildCommand com = new BuildCommand(worker, building, newDefensePos);
			buildCommands.add(com);
			spaceManager.reserveSpace(com);
			reservedMinerals += building.mineralPrice();
			reservedGas += building.gasPrice();
			worker.command = new BuildDefensive(worker, defensePos);
			return true;
		}
		
  		//get a nice place to build the building 
  		TilePosition buildTile = 
  			getBuildTile(worker.unit, building, target);
  		
  		if(buildTile == null)
  		{
  			workForce.add(worker);
  			return false;
  		}
  		
		worker.unit.move(new Position(buildTile.getX()*32, buildTile.getY()*32));
		
		BuildCommand com = new BuildCommand(worker, building, buildTile);
		buildCommands.add(com);
		spaceManager.reserveSpace(com);
		reservedMinerals += building.mineralPrice();
		reservedGas += building.gasPrice();
		worker.command = new BuildDefensive(worker, defensePos);
		return true;
    }
	
	public boolean build(UnitType building)
	{
		return build(building, null);
	}
	
	public boolean build(UnitType building, Position desiredPosition)
    {
		Agent worker = workForce.pop(desiredPosition == null? Tyr.tileToPostion(self.getStartLocation()):desiredPosition);
		if (worker == null)
			return false;
		
		BuildCommand com = null;
		if (wallOff != null)
			com = wallOff.getCommand(building);
		if(com != null)
		{
			com.worker = worker;
			worker.unit.move(new Position(com.position.getX()*32, com.position.getY()*32));

			spaceManager.reserveSpace(com);
			reservedMinerals += building.mineralPrice();
			reservedGas += building.gasPrice();
			return true;
		}
		
		
		if(desiredPosition == null && building == UnitType.Terran_Supply_Depot || building == UnitType.Terran_Armory || building == UnitType.Terran_Academy)
		{
			TilePosition target = spaceManager.findDepotPlacement(worker.unit);
			if (target != null)
			{	
				worker.unit.move(new Position(target.getX()*32, target.getY()*32));
				
				com = new BuildCommand(worker, building, target);
				buildCommands.add(com);
				spaceManager.reserveSpace(com);
				reservedMinerals += building.mineralPrice();
				reservedGas += building.gasPrice();
				return true;
			}
		}
				
		
		TilePosition target;
		if(desiredPosition == null)
		{
			if(game.isVisible(self.getStartLocation()))
				target = self.getStartLocation();
			else
			{
				if(workForce.mineralWorkers.size() > 0)
					target = workForce.mineralWorkers.get(0).resourceDepot.getTilePosition();
				else
					target = worker.unit.getTilePosition();
			}
		}
		else
			target = positionToTile(desiredPosition);
		
		TilePosition buildTile;
		if (!game.isVisible(target))
		{
			buildTile = target;
			System.out.println("Build position not yet visible: " + target.getX() + ", " + target.getY());
		}
		else
		buildTile = getBuildTile(worker.unit, building, target);
  		
  		if(buildTile == null)
  		{
  			workForce.add(worker);
  			return false;
  		}
		
		builders.add(worker);

		if (building.isResourceDepot())
		{
			List<Unit> inRange = game.getUnitsInRadius(Tyr.tileToPostion(buildTile), 270);
			boolean success = false;
			for(Unit mineral : inRange)
			{
				if (mineral.getType().isMineralField())
				{
					worker.unit.gather(mineral);
					success = true;
					break;
				}
			}
			
			if (!success)
				worker.unit.move(Tyr.tileToPostion(buildTile));
		}
		
		worker.unit.move(new Position(buildTile.getX()*32, buildTile.getY()*32));
		
		com = new BuildCommand(worker, building, buildTile);
		buildCommands.add(com);
		spaceManager.reserveSpace(com);
		reservedMinerals += building.mineralPrice();
		reservedGas += building.gasPrice();
		
		return true;
    }

	 // Returns a suitable TilePosition to build a given building type near 
	 // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
	 public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) 
	 {
	 	TilePosition ret = null;
	 	int maxDist = 3;
	 	int stopDist = 40;
	 	
	 	// Refinery, Assimilator, Extractor
	 	if (buildingType.isRefinery()) {
	 		for (Unit n : game.neutral().getUnits()) {
	 			if ((n.getType() == UnitType.Resource_Vespene_Geyser)) 
	 				{
	 					boolean hasBase = false;
	 					for(MineralWorkers base : workForce.mineralWorkers)
	 					{
	 						if (base.resourceDepot.distanceTo(n)  <= 270)
	 						{
	 							hasBase = true;
	 							break;
	 						}
	 					}
	 					if(!hasBase)
	 						continue;
	 					return n.getTilePosition();
	 				}
	 		}
	 	}
	 	
	 	if (buildingType.isResourceDepot())
	 	{
	 		BaseLocation loc = null;
	 		for (BaseLocation b : expands)
	 		{
	 			if (game.canBuildHere(builder, b.getTilePosition(), buildingType, false))
	 			{
	 				boolean enemyBase = false;
	 				for(EnemyPosition p : enemyBuildingMemory)
	 				{
	 					enemyBase =b.getPosition().distanceTo(p.pos.getX(), p.pos.getY()) < 256;
	 					if(enemyBase)
	 						break;
	 				}
	 				
	 				if (enemyBase)
	 					continue;
	 				
	 				boolean unitsInWay = false;
					for(BuildCommand cmd : buildCommands)
					{
						if (cmd.position.getDistance(b.getTilePosition()) <= 1)
						{
							unitsInWay = true;
							break;
						}
					}
					if (unitsInWay)
						continue;
	 				
	 				if (loc == null || BWTA.getGroundDistance(aroundTile, b.getTilePosition()) < BWTA.getGroundDistance(aroundTile, loc.getTilePosition()))
	 					loc = b;
	 			}
	 		}
	
	 	 	//if (loc == null) game.printf("Unable to find suitable build position for "+buildingType.toString());
	 	 	return loc==null?null:loc.getTilePosition();
	 	}
	 	
	 	if (!game.isVisible(aroundTile))
	 		return aroundTile;
	 	
	 	while ((maxDist < stopDist) && (ret == null))
	 	{
	 		for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++)
	 			for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++)
	 				if(canBuildHere(builder, i, j, buildingType))
	 					return new TilePosition(i, j);
	 		
 			maxDist += 2;
 		}
 	
 		return ret;
 	}
	 
	 public boolean canBuildHere(Unit builder, int i, int j, UnitType building)
	 {
		 if (!spaceManager.canBuildHere(builder, new TilePosition(i,j), building)
				|| !(!building.canBuildAddon() || spaceManager.canBuildHere(builder, new TilePosition(i+2,j), building)))
		 return false;
		 
		// creep for Zerg
		if (building.requiresCreep()) 
		{
			boolean creepMissing = false;
			for (int k=i; k<=i+building.tileWidth(); k++) {
				for (int l=j; l<=j+building.tileHeight(); l++) {
					if (!game.hasCreep(k, l)) 
					{
						creepMissing = true;
						break;
					}
				}
			}
			if (creepMissing) return false; 
		}
		
		if (bot.getMainExit().getDistance(Tyr.tileToPostion(new TilePosition(i,j))) <= 64)
			return false;
		
		// units that are blocking the tile
		for (Unit u : game.getAllUnits()) 
		{
			if (u.getID() == builder.getID()) continue;
			
			if ((building == UnitType.Terran_Bunker || building == UnitType.Terran_Missile_Turret) 
					&& (u.getType() == UnitType.Terran_Bunker || u.getType() == UnitType.Terran_Missile_Turret))
					continue;
			
			if (building == UnitType.Terran_Supply_Depot
					&& (u.getType() == UnitType.Terran_Supply_Depot 
					|| u.getType() == UnitType.Terran_Armory || u.getType() == UnitType.Terran_Academy))
					continue;
			
			if ((u.getTilePosition().getX()-i < building.tileWidth()+1 && i - u.getTilePosition().getX() < u.getType().tileWidth()+1)
					&& (u.getTilePosition().getY()-j < building.tileHeight()+1 && j - u.getTilePosition().getY() < u.getType().tileHeight() + 1))
				return false;
		}
		return  true;
	 }
	 
	 public static Position tileToPostion(TilePosition pos)
	 {
		 return new Position(pos.getX()*32+16, pos.getY()*32+16);
	 }
	 
	 public static TilePosition positionToTile(Position pos)
	 {
		 return new TilePosition(pos.getX()/32, pos.getY()/32);
	 }
    
    public static void main(String[] args) {
    	bot = new Tyr();
        bot.run();
    }
}