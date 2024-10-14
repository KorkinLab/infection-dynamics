package CruiseShipModel;

import static CruiseShipModel.Ship.*;
import static CruiseShipModel.Person.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.Scanner;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.GeomPlanarGraphDirectedEdge;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;

//This class represents every agent on the cruise ship. Specifically, these agents represent
//passengers or crew members among the ship. This contains their implementation for movement
//and their implementation for any interactions/reactions to other agents
public class Agent implements Steppable
{
    protected static final long serialVersionUID = 3705661848016825633L;
    public double shedding = 0;
    public double SHED_START = 1000000000;

    public String[] behavior = new String[24];
    
    protected MasonGeometry geomObject;
    public static final double foot = 0.3048;
    protected double baseMoveRate = (foot * 3);//3 feet per second based off of scale model of 17.794x. (1 step = 1 second)
    protected double savedMoveRate = (foot * 3);
    protected double moveRate = baseMoveRate;
    protected double zombieSpeed = (foot/10);
    protected double timeToSpend = 0;
    private LengthIndexedLine segment = null;
    double startIndex = 0.0; // start position of current line
    double endIndex = 0.0; // end position of current line
    double currentIndex = 0.0; // current location along line
    PointMoveTo pointMoveTo = new PointMoveTo();
    
    static protected GeometryFactory fact = new GeometryFactory();

    protected boolean pathMoving, atHome, atDining, atFree, atWork;
    protected boolean hadBreakfast, hadLunch, hadDinner, hadFun, hadWork;
    protected Vector<Integer> path;
    protected int numPosition = 1;
    
    protected Node homeNode = null, diningNode = null, freeNode = null, workNode = null, safeNode = null, boardingNode = null;
    protected Node destination = null;//the destination node that the current agent needs to travel to
    
    static Node starterInfectedNode = null;//The starter infected node
    
    protected double randomness;//The randomness with which agent behavior changes in a day
    protected int timeStationary = 0;//The time an agent has been stationary

    private int isolationThreshold = (int) (0.03 * Ship.totalOnboard);

    //CONSTRUCTOR
    public Agent(Ship state)
    {	   
        randomness = state.random.nextDouble()*2 - 1; //By default, they have a 2 hour random variance
        
        for(int i = 0; i < 24; i++) //Behavior patterns for agents
        {
            behavior[i] = "Sleep";//by default
        }
              
        path = new Vector<Integer>();
        
        setGeomObject(new MasonGeometry(fact.createPoint(new Coordinate(10, 10)))); //magic numbers
        getGeomObject().isMovable = true;
        
        //initializeNodes(state);done by extended class
        //setMoveRate(state); done by extended class

        resetVisited();
        resetAt();
    	pathMoving = false;
    }

    public Node getHomeNode(){return this.homeNode;}
    public Node getBoardingNode(){return this.boardingNode;}
    
    public void setMoveRate(double num)
    {
        baseMoveRate = num;
        if(moveRate > 0)
            moveRate = num;
        else
            moveRate = -num;
        //System.out.println("new move rate: " + moveRate);
        getGeomObject().addDoubleAttribute("MOVE RATE", moveRate);
    }
    
    public void initMoveRate(Ship state)
    {
        baseMoveRate = baseMoveRate + ((state.random.nextDouble()/5) - (.2));
        savedMoveRate = baseMoveRate;
        moveRate = baseMoveRate;
        //System.out.println("starting move rate: " + moveRate);
        getGeomObject().addDoubleAttribute("MOVE RATE", moveRate);
    }
    
    //PURPOSE: to get the geometry of this agent
    //PARAMETERS: nothing
    //RETURN: the MasonGeometry location of this agent
    public MasonGeometry getGeometry()
    {
        return getGeomObject();
    }
    
    public void initializeNodes(Ship state)
    {
        int rando = 0;
        
        //calculate each agents room
        if(homeNode == null)//only set at beginning of program once
        {
            int setRoom = 0;
            
            switch(getAgentType())
            {
                case "Passenger":
                    rando = state.random.nextInt(state.nodeData.get(1).size());
                    setRoom = state.nodeData.get(1).get(rando); 
                    break;
                case "Crew: Structured":
                case "Crew: Unstructured":
                    rando = state.random.nextInt(state.nodeData.get(0).size());
                    setRoom = state.nodeData.get(0).get(rando); 
                    break;
                default:
                    System.out.println("No homeNode set! Error!");
                    break;
            }
 
            //System.out.println("Room Assigned");
            homeNode = (Node)(state.getNodeArr()[setRoom]);
        }
        
        if(workNode == null)//only set at beginning of program once
        {
            int freeOrDine = state.random.nextInt(2) + 2; 
            rando = state.random.nextInt(state.nodeData.get(freeOrDine).size());
            int setWork = state.nodeData.get(freeOrDine).get(rando); 
            
            workNode = (Node)(state.getNodeArr()[setWork]);
        }
        
        if(boardingNode == null)
        {
            rando = state.random.nextInt(state.nodeData.get(5).size());
            int setBoarding = state.nodeData.get(5).get(rando);
            
            boardingNode = (Node)(state.getNodeArr()[setBoarding]);
        }
        
        //calculate each agents preferred dining
//        int day = ((int)state.schedule.getTime() / dayInSteps) + 1;
        rando = state.random.nextInt(state.nodeData.get(2).size());

        if(diningRestricted && (totalIll >= isolationThreshold) ) {
        	rando = state.random.nextInt(state.nodeData.get(2).size()-1);   
        }

        int setDining = state.nodeData.get(2).get(rando);
        diningNode = (Node)(state.getNodeArr()[setDining]);

        if(diningClosed && (totalIll >= isolationThreshold) ) {
        	diningNode = homeNode;
        }
        
        //calculate each agents preferred free time area
        rando = state.random.nextInt(state.nodeData.get(3).size());
        int setFree = state.nodeData.get(3).get(rando); 

        //System.out.println("Free Assigned");
        freeNode = (Node)(state.getNodeArr()[setFree]);
        
        if(state.getRunCode() == 2)
        {
            rando = state.random.nextInt(state.safeNodes.size());
            int setSafe = state.safeNodes.get(rando);
            
            safeNode = (Node)(state.getNodeArr()[setSafe]);
        }
        
        this.geomObject.addAttribute("SAFE", 0);
    }
    
    //PURPOSE: to determine whether the agent is moving along a line, or if it is arrived at the next node
    //PARAMETERS: nothing
    //RETURN: boolean true if it has arrive, false if else
    public boolean arrived()
    {
        // If we have a negative move rate the agent is moving from the end to
        // the start, else the agent is moving in the opposite direction.
        if ((moveRate > 0 && currentIndex >= endIndex)
            || (moveRate < 0 && currentIndex <= startIndex))
        {
            return true;
        }

        return false;
    }

    //PURPOSE: to do a breadth-first-search shortest path calculation for this agent to find the optimal path to take
    //PARAMETERS: the starting node it is currently at, the destination node it is trying to get to, the queue of nodes that represents
                //where it has traveled, and which nodes it has visited, and a string pathTo that acts as memory for each possible path it can take
    //RETURN: boolean true if we have found a path to the destination, and false if else
    public boolean runBFS(Node start, Node dest, Queue<Node> queue, String[] pathTo)
    {
    	if(start.isVisited())
            return false;
    	else if(start.getData() == dest.getData())
            return true;
    	else
    	{
            start.setVisited(true);
            DirectedEdgeStar directedEdgeStar = start.getOutEdges();
            Object[] edges = directedEdgeStar.getEdges().toArray();
            for(int i = 0; i < edges.length; i++)
            {
            	GeomPlanarGraphEdge graphEdge = (GeomPlanarGraphEdge)((GeomPlanarGraphDirectedEdge)edges[i]).getEdge();
            	Node newJunc = graphEdge.getOppositeNode(start);
                if(newJunc.isVisited() == false)
                {
                    pathTo[(int)newJunc.getData()] = pathTo[(int)start.getData()] + (int)newJunc.getData() + " ";
                    queue.add(newJunc);
                }
            }
    	}
    	return false;
    }
    
    //PURPOSE: to do a depth-first-search shortest path calculation for this agent to find the optimal path to take
             //Note: it is inconsistent on a closed network such as ours
    //PARAMETERS: the starting node it is currently at, the destination node it is trying to get to, the stack of nodes that represents
                //where it has traveled, and which nodes it has visited, and a string pathTo that acts as memory for each possible path it can take
    //RETURN: boolean true if we have found a path to the destination, and false if else
    public boolean runDFS(Node start, Node dest, Stack<Node> stack, String[] pathTo)
    {
        if(start.getData() == dest.getData())
            return true;
        else
        {
            start.setVisited(true);
            DirectedEdgeStar directedEdgeStar = start.getOutEdges();
            Object[] edges = directedEdgeStar.getEdges().toArray();
            Node closeJunc = null;
            double smallestDist = -1;
            for(int i = 0; i < edges.length; i++)
            {
                GeomPlanarGraphEdge graphEdge = (GeomPlanarGraphEdge)((GeomPlanarGraphDirectedEdge)edges[i]).getEdge();
            	Node newJunc = graphEdge.getOppositeNode(start);
                double distance = calcDistance(newJunc, dest);
                if(!newJunc.isVisited() && (smallestDist < 0 || distance < smallestDist))
                {
                    //System.out.println("This Junc: " + start.getData());
                    //System.out.println("Smaller Distance: " + distance);
                    smallestDist = distance;
                    closeJunc = newJunc;
                }
            }
            
            if(closeJunc != null)
            {
                //System.out.println("pushed");
                pathTo[(int)closeJunc.getData()] = pathTo[(int)start.getData()] + (int)closeJunc.getData() + " ";
                stack.push(closeJunc);
            }
            else
            {
                //System.out.println("popped");
                stack.pop();
            }
        }
        return false;
    }
    
    //PURPOSE: to calculate the distance from the current agent's node to its destination node
    //PARAMETERS: its current node and its destination node
    //RETURN: double distance
    public double calcDistance(Node newJunc, Node dest)
    {
        double x = dest.getCoordinate().x - newJunc.getCoordinate().x;
        double y = dest.getCoordinate().y - newJunc.getCoordinate().y;
        double distance = Math.sqrt((x*x) + (y*y)); 
        return distance;
    }
    
    //PURPOSE: to find a new path for the agent to take, based on a given destination coordinate
    //PARAMETERS: the current Ship state that we are in, and the destination coordinates for where the agent is going
    //RETURN: nothing
    public void findNewPath(Ship geoTest, Coordinate destination)
    {	
        Node currentJunction = geoTest.network.findNode(getGeomObject().getGeometry().getCoordinate());
        Node destNode = geoTest.network.findNode(destination);
        if(destNode == null)
        {
            System.out.println("NULL DESTINATION");
            return;
        }
        if (currentJunction != null)
        {   
            String[] pathTo;
            pathTo = new String[geoTest.network.getNodes().size()];
            Queue<Node> queue = new LinkedList<Node>();
            //Stack<Node> stack = new Stack<Node>(); FOR USING DFS

            queue.add(currentJunction);
            //stack.push(currentJunction); FOR USING DFS
            pathTo[(int)currentJunction.getData()] = (int)currentJunction.getData() + " ";
            Iterator<Node> it = geoTest.network.getNodes().iterator();
            while(it.hasNext())
                it.next().setVisited(false);//Set them all to not visited before the BFS occurs
            while(!queue.isEmpty()) //!stack.empty() FOR USING DFS
            {
                if(runBFS(queue.remove(), destNode, queue, pathTo) == true) //FOR USING BFS
                    break; //FOR USING BFS
                //if(runDFS(stack.peek(), destNode, stack, pathTo) == true) FOR USING DFS
                    //break; FOR USING DFS
            }

            /*System.out.println();
            System.out.println("Destination Node: " + (int)destNode.getData());
            System.out.println("Full Path: " + pathTo[(int)destNode.getData()]);
            System.out.println();*/

            if(pathTo[(int)destNode.getData()] != null)
            {
                Scanner pathScan = new Scanner(pathTo[(int)destNode.getData()]);
                while(pathScan.hasNextInt())
                    path.add(pathScan.nextInt());
            }
            else
            {	
                System.out.println("Cannot reach next network node");
                return;
            }

            //System.out.println("On the move!!");    
            nextInPath(currentJunction);
        }
    }

    public void setStart(Coordinate startCoord)
    {
        moveTo(startCoord);
    }
    
    //PURPOSE: to set a new route for the agent and set its projected movement direction along the route
    //PARAMETERS: Linestring for the route; boolean true if agent is at start of line, false if at end of line
    //RETURN: nothing
    public void setNewRoute(LineString line, boolean start)
    {
        //System.out.println("Setting new Route for Agent #: " + this.hashCode());
        segment = new LengthIndexedLine(line);
        startIndex = segment.getStartIndex();
        endIndex = segment.getEndIndex();

        Coordinate startCoord = null;

        if (start)
        {
            startCoord = segment.extractPoint(startIndex);
            currentIndex = startIndex;
            moveRate = baseMoveRate; // ensure we move forward along segment
        } else
        {
            startCoord = segment.extractPoint(endIndex);
            currentIndex = endIndex;
            moveRate = -baseMoveRate; // ensure we move backward along segment
        }
        
        moveTo(startCoord);
    }

    //PURPOSE: Move the agent to the given coordinates
    //PARAMETERS: a Coordinate object of where we are moving
    //RETURN: nothing
    public void moveTo(Coordinate c)
    {
        //System.out.println("AGENT PREVIOUSLY MOVED FROM LOCATION: " + this.location.getGeometry());
        pointMoveTo.setCoordinate(c);
        getGeomObject().getGeometry().apply(pointMoveTo);
        getGeometry().geometry.geometryChanged();
        //System.out.println("AGENT SUCCESSFULLY MOVED TO NEW LOCATION: " + this.location.getGeometry());
    }

    //PURPOSE: to loop the program and iterate it through a single step of the program
    //PARAMETERS: a Simstate object(necessary for step functions using the MASON library)
    //RETURN: nothing
    @Override
    public void step(SimState state)
    {   
        Ship world = (Ship)state;
        
        if(world.getTime()/4.0 == 0)
        {
            resetVisited();
            initializeNodes(world);//new preferred areas each day (at 4AM when people are asleep)
        }
        
        //Work is done in extended classes
        
        move(world);
    }

    //PURPOSE: Determines whether the agent will stay where it is, find a new path, or continue moving based on
    //certain conditions such as the time of day, and who the agent is
    //PARAMETERS: the Ship object of this program
    //RETURN: nothing
    public void move(Ship geoTest)
    {	       
    	Node currentJunction = geoTest.network.findNode(getGeomObject().getGeometry().getCoordinate());

//        if(geoTest.getRunCode() == 2 && this.getLocation().getIntegerAttribute("INFECTED") > 0)// if zombies, slow down
//                this.setMoveRate(foot/10);
        
        if(pathMoving == false)
        {
            destination = getProjectedDestination(geoTest);//get new destination if its not already moving
            
            if(destination.equals(safeNode))//in case of zombies
                this.getGeomObject().addIntegerAttribute("SAFE", 1);
        }
        
        if(currentJunction != null && destination != null && currentJunction.equals(destination))
        {
            if(pathMoving == true)
            {
                pathMoving = false;
            //System.out.println("Arrived at destination!");
                if(destination.equals(homeNode))
                    atHome = true;
                else if(destination.equals(diningNode))
                    atDining = true;
                else if(destination.equals(freeNode))
                    atFree = true;
                else if(destination.equals(workNode))
                    atWork = true;
            }
            
            /*if((timeStationary % (60*5) == 0) && (getLocation().getIntegerAttribute("INFECTED") > 0)) 
            {
                MAKE VIRAL PARTICLES
                ViralParticle virus = new ViralParticle(geoTest, currentJunction.getCoordinate());
                geoTest.agents.addGeometry(virus.getGeometry());
                geoTest.schedule.scheduleRepeating(virus);
            }*/
            if((timeStationary >= timeToSpend) && atWork != true) //if they've been somewhere except their room for a while, leave (unless it's work)
            {
                hasVisited(geoTest);
                return;
            }
            
            //numPosition = 1;
            timeStationary++;
        }
        else if (pathMoving == true && !arrived())
            moveAlongPath();
        else if(pathMoving == false && destination != null)//If not moving and the schedule says they
        {                                                //need to be somewhere, move (took out randomness for now)
            timeStationary = 0;
            atHome = false;
            atDining = false;
            atFree = false;
            atWork = false;
            pathMoving = true;
            numPosition = 1;
            path.clear();
            timeToSpend = geoTest.random.nextInt(3600)+1800;//Set a random time to spend at that node
            if(currentJunction != null && currentJunction.equals(homeNode))
                resetVisited();//If leaving home, reset visited nodes
            
            findNewPath(geoTest, destination.getCoordinate());
        }
        else if(pathMoving == true && currentJunction!=null)
            nextInPath(currentJunction);
    }

    //PURPOSE: Determines what node is the next node in the path and sets up the variables necessary to move to the next node
    //PARAMETERS: the Node that the agent is currently at
    //RETURN: nothing
    public void nextInPath(Node currentJunction)
    {
        GeomPlanarGraphEdge finEdge = null;
        //calculate the line segment like in previous version of findNewRoute
        DirectedEdgeStar directedEdgeStar = currentJunction.getOutEdges();
        Object[] edges = directedEdgeStar.getEdges().toArray();
        //System.out.println("NUM EDGES: " + edges.length);
        //System.out.println("JUNC COORD: " + currentJunction.getCoordinate());
        for(int i = 0; i < edges.length; i++)
        {
            GeomPlanarGraphDirectedEdge directedEdge = (GeomPlanarGraphDirectedEdge) edges[i];
            GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge) directedEdge.getEdge();
            //System.out.println("Checking equivalency to scanned: " + (int)edge.getOppositeNode(currentJunction).getData());
            //System.out.println("Scanned: " + path.get(numPosition));
            if((int)edge.getOppositeNode(currentJunction).getData() == path.get(numPosition))
            {
                //System.out.println("finEdge Found!");
                finEdge = edge;
                numPosition++;
                break;
            }
        }
        if(finEdge == null) {
            System.out.println("NUM EDGES: " + edges.length);
            System.out.println("JUNC COORD: " + currentJunction.getCoordinate());
            for(int i = 0; i < edges.length; i++)
            {
                GeomPlanarGraphDirectedEdge directedEdge = (GeomPlanarGraphDirectedEdge) edges[i];
                GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge) directedEdge.getEdge();
                System.out.println("Checking equivalency to scanned: " + (int)edge.getOppositeNode(currentJunction).getData());
                System.out.println("Scanned: " + path.get(numPosition));
            }
            System.out.println("finEdge not Found!");
        }
        LineString newRoute = finEdge.getLine();
        Point startPoint = newRoute.getStartPoint();
        Point endPoint = newRoute.getEndPoint();
        if (startPoint.equals(getGeomObject().geometry))
        {
            setNewRoute(newRoute, true);
        } else
        {
            if (endPoint.equals(getGeomObject().geometry))
            {
                setNewRoute(newRoute, false);
            } else
            {
                System.err.println("Lost Agent...");
            }
        }   
    }
    
    //PURPOSE: Move this agent along the current line path
    //PARAMETERS: nothing
    //RETURN: nothing
    public void moveAlongPath()
    {
        currentIndex = currentIndex + moveRate;

        // Truncate movement to end of line segment
        if (moveRate < 0)
        { // moving from endIndex to startIndex
            if (currentIndex < startIndex)
            {
                currentIndex = startIndex;
            } 
        } else
        { // moving from startIndex to endIndex
            if (currentIndex > endIndex)
            {
                currentIndex = endIndex;
            }
        }
        Coordinate currentPos = segment.extractPoint(currentIndex);
        moveTo(currentPos);
    }
    
    //PURPOSE: Determines the destination of this agent based on time and place
    //PARAMETERS: Ship state to find time
    //RETURN: a Node of where the agent needs to go now based on time and place
    public Node getProjectedDestination(Ship state)
    {
        // ISOLATION CODE

        double time = (((state.schedule.getTime()) / 3600) + 6) % 24;
        int wholeTime = (int)time;
//        if ((wholeTime >= 8) && (wholeTime <= 9)) {     // Between 8 and 9 AM everyday
            if( (vspIsolation && (totalIll >= isolationThreshold)) &&
                    (getGeomObject().getIntegerAttribute("ILL") == 1) ) {		// Isolate ill passengers if 3% passengers reported with gastroenteritis
//                System.out.println("Sent home");
                return this.homeNode;
            }
//        }
    	
        if(selfIsolation && ( getGeomObject().getIntegerAttribute("ILL" ) == 1) ) {
        	int dpi = ( (int) state.schedule.getTime() - getGeomObject().getIntegerAttribute( "TIME_INFECTED")) / Person.dayInSteps;
        	
            if( dpi > 0 )
                return this.homeNode;
        }

        
        //DO THIS STUFF IF YOU ARE RUNNING THE NORMAL PROGRAM WITH RUNCODE == 1
        double timeFrame = (state.getTime() + randomness + 24.0) % 24.0;
        switch(behavior[(int)timeFrame])//passengers have randomness
        {
            case "Meal:Breakfast":
                if(hadBreakfast == true)
                    return this.homeNode;
                else
                    return this.diningNode;
            case "Meal:Lunch":
                if(hadLunch == true)
                    return this.homeNode;
                else
                    return this.diningNode;
            case "Meal:Dinner":
                if(hadDinner == true)
                    return this.homeNode;
                else
                    return this.diningNode;
            case "Sleep":
                return this.homeNode;
            case "Free":
                if(hadFun == true)
                    return this.homeNode;
                else
                    return this.freeNode;
            case "Work":
                if("Crew: Unstructured".equals(getAgentType()))
                {
                    int freeOrDine = state.random.nextInt(2) + 3; 
                    int rando = state.random.nextInt(state.nodeData.get(freeOrDine).size());
                    int setWork = state.nodeData.get(freeOrDine).get(rando); 
            
                    workNode = (Node)(state.getNodeArr()[setWork]);
                }      
                return this.workNode;//Will stay at work consistently until off
            default:
                return this.homeNode;//just in case
        }
    }
    
    public void hasVisited(Ship state)
    {
        double timeFrame = (state.getTime() + randomness + 24.0) % 24.0;
        
        switch(behavior[(int)timeFrame])
        {
            case "Meal:Breakfast":
                hadBreakfast = true;
                break;
            case "Meal:Lunch":
                hadLunch = true;
                break;
            case "Meal:Dinner":
                hadDinner = true;
                break;
            case "Free":
                hadFun = true;
                break;
            case "Work":
                hadWork = true;
                break;
            default:
                break; //do nothing
        }
    }
        
    public void resetVisited()
    {
        hadBreakfast = false;
        hadLunch = false;
        hadDinner = false;
        hadFun = false;
        hadWork = false;
    }
    
    public void resetAt()
    {
        atHome = false;
        atDining = false;
        atFree = false;
        atWork = false;
    }
 
    public void kill(Ship state)
    {
        state.agentList.remove(this);
        state.agents.removeGeometry(this.geomObject);
    }

    public MasonGeometry getGeomObject(){ return geomObject; }
    public void setGeomObject(MasonGeometry loc){ this.geomObject = loc; }

    protected String getAgentType() { return "Agent"; }
    
    protected Node getRandomNode(Ship state)
    {
        int len = state.getNodeArr().length;
        return (Node)(state.getNodeArr()[state.random.nextInt(len)]);    
    }
}
