package CruiseShipModel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.media.MediaLocator;
import org.jfree.data.xy.XYSeries;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.SparseGrid2D;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.MasonGeometry;

//Acts as the state for the entire program. Sets and gets data for the state of the program, and helps to loop through the program's steps
public class Ship extends SimState
{
    private static final long serialVersionUID = 4554882816749973618L;

    //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    ShipUI shipGUI = null;
    
    public static final int WIDTH = 1600;
    public static final int HEIGHT = 900;

    // TODO
    // Set number of agents in simulation
    public static final int numPrinters = 1; // should be 1888, scaled down by ~10 for performance purposes
    private int printerCounter = numPrinters;    // see above
    public static final int numPassengers = 1888; // should be 1888, scaled down by ~10 for performance purposes
    private int passengerCounter = numPassengers;    // see above
    public static final int numStrucCrew = 814;   // should be 700, scaled down by ~10 for performance purposes
    private int strucCrewCounter = numStrucCrew;    // see above
    public static final int numUnstrucCrew = 0;// should be 114, scaled down by ~10 for performance purposes
    private int unstrucCrewCounter = numUnstrucCrew;  // see above
    private int numInfectedPersons = 0;
    public static final int totalOnboard = numPassengers + numStrucCrew + numUnstrucCrew;

    // Set days in simulation
    public static final int simEndDay = 15;

    // Set containment
    public static boolean diningRestricted = false;  // Certain dining halls closed
    public static boolean diningClosed = false;      // diningNode set to room
    public static boolean vspIsolation = true;      // CDC VSP
    public static boolean selfIsolation = false;     // Symptomatic remain in rooms
    public static boolean improvedHygiene = false;   // 50% reduction in becoming infected
    public static boolean improvedCleaning = false;  // Viral particles are wiped more often
    public static boolean facemasks = false;
    
    private ArrayList<Integer> infectionsPerDay;
    private double[] infectionsPerDayDoub;

    //GIS fields for shapefile representation
    public GeomVectorField net = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField room = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField dining = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField casino = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField bar = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField theater = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField shopping = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField gym = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField lounge = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField pool = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField boardingZone = new GeomVectorField(WIDTH, HEIGHT);

    public GeomGridField gridField = new GeomGridField();
    public GeomPlanarGraph network = new GeomPlanarGraph();
    public GeomVectorField junctions = new GeomVectorField(WIDTH, HEIGHT); // nodes for intersections

    Bag netAttributes = new Bag();
    Bag roomAttributes = new Bag();
    ArrayList<Agent> agentList = new ArrayList<Agent>();
    public SparseGrid2D deck = new SparseGrid2D(WIDTH, HEIGHT);  
    
    MasonGeometry elevatorConnectors;

    public Vector<Vector<Integer>> nodeData;
    public Vector<Integer> passHomeData; 
    public Vector<Integer> crewHomeData;
    public Vector<Integer> diningData;
    public Vector<Integer> freeData;
    public Vector<Integer> basicData;
    public Vector<Integer> boardingData;
    
    public Vector<Integer> safeNodes;
    
    public Vector<String> screenList;
    public String filePath = "CruiseMovie";
    private boolean record = false;//set to true or false if you want to record or not
    private boolean recordSwap = false;
    private int runCode = 1; //1 for normal infections, 2 for zombies 
    
    public boolean boarding = true;
    private static Object[] nodeArr;//Array of the nodes on the ship
    
    XYSeries series;
    public agentAdder aa;
    
    //CONSTRUCTOR
    public Ship(long seed)
    {
//    	super(seed);     
//      desiredAttributes.add("elevConec");
//      desiredAttributes.add("RoomNumber");
//      desiredAttributes.add("FID");
    	super(seed);

    	Person.initialInfectedPopulation = 2;

        netAttributes.add("elevConec");
        netAttributes.add("RoomNumber");
        netAttributes.add("FID");
        netAttributes.add("BoardingZo");
        roomAttributes.add("Type");
        try
        {
//            System.out.println("reading buildings layer");
//            System.out.println("reading halls and rooms layer");

            URL netGeometry = Ship.class.getResource("/CruiseNet.shp");
            URL roomGeometry = Ship.class.getResource("/roomAreas.shp");
            URL diningGeometry = Ship.class.getResource("/diningAreas.shp");
            URL casinoGeometry = Ship.class.getResource("/casinoAreas.shp");
            URL barGeometry = Ship.class.getResource("/barAreas.shp");
            URL theaterGeometry = Ship.class.getResource("/theaterAreas.shp");
            URL shoppingGeometry = Ship.class.getResource("/shoppingAreas.shp");
            URL gymGeometry = Ship.class.getResource("/gymAreas.shp");
            URL loungeGeometry = Ship.class.getResource("/loungeAreas.shp");
            URL poolGeometry = Ship.class.getResource("/poolAreas.shp");
            URL boardingGeometry = Ship.class.getResource("/boardingZones.shp");
            
            ShapeFileImporter.read(netGeometry, net, netAttributes);
            ShapeFileImporter.read(roomGeometry, room, roomAttributes);
            ShapeFileImporter.read(diningGeometry, dining);
            ShapeFileImporter.read(casinoGeometry, casino);
            ShapeFileImporter.read(barGeometry, bar);
            ShapeFileImporter.read(theaterGeometry, theater);
            ShapeFileImporter.read(shoppingGeometry, shopping);
            ShapeFileImporter.read(gymGeometry, gym);
            ShapeFileImporter.read(loungeGeometry, lounge);
            ShapeFileImporter.read(poolGeometry, pool);
            ShapeFileImporter.read(boardingGeometry, boardingZone);

//            System.out.println("Done reading data");
            
            Envelope MBR = net.getMBR();
            MBR.expandBy(100);
            agents.setMBR(MBR);
            junctions.setMBR(MBR);
            room.setMBR(MBR);
            dining.setMBR(MBR);
            casino.setMBR(MBR);
            bar.setMBR(MBR);
            theater.setMBR(MBR);
            shopping.setMBR(MBR);
            gym.setMBR(MBR);
            lounge.setMBR(MBR);
            pool.setMBR(MBR);
            boardingZone.setMBR(MBR);
            
            elevatorConnectors = net.getGeometry("elevConec", 1);
            
            infectionsPerDay = new ArrayList<Integer>();
            
            nodeData = new Vector<Vector<Integer>>(); //(0:passenger, 1:crew, 2:dining, 3:free, 4:basic)
            passHomeData = new Vector<Integer>(); 
            crewHomeData = new Vector<Integer>(); 
            diningData = new Vector<Integer>(); 
            freeData = new Vector<Integer>();
            basicData = new Vector<Integer>();
            boardingData = new Vector<Integer>();
            
            safeNodes = new Vector<Integer>();
            
            screenList = new Vector<String>();
            
            network.createFromGeomField(net);
            setNodeData(network.nodeIterator());
            
            nodeArr = network.getNodes().toArray();
        } catch (IOException e) {
        } catch (Exception e) {
        }
    }

    public static int rwInitialInfectedPopulation() {
        int count = 0;
        String filePath = "";
        try {
            if ( !new File(filePath).exists() )
                System.out.println("No Count file!");
            else {
                BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
                String s = br.readLine();
                count = Integer.parseInt(s);
                br.close();
                System.out.println("Count found: " + count);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (count > 6)
            count = 2;

        int temp = count + 1;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
            bw.write(Integer.toString(temp));
            bw.close();
            System.out.println("Count saved: " + temp);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    //PURPOSE: get the number of agents in the simulation
    //PARAMETERS: nothing
    //RETURN: the number of agents as an int
    public int getNumAgents() { return numPassengers+numUnstrucCrew+numStrucCrew; } 
    
    //PURPOSE: set the number of agents in the simulation
    //PARAMETERS: the number of agents as an int
    //RETURN: nothing
    /*public void setNumAgents(int n) 
    { 
        if (n > 0) 
            numPassengers = n; 
    } */

    //PURPOSE: Get the time (in human readable hours) for the simulation
    //PARAMETERS: nothing
    //RETURN: a double time
    public double getTime()
    {
        double seconds = schedule.getTime();//1 steps per second
        double time = (((seconds/60)/60)+6)%(24.00);
        return time;//in hours out of 24 hours      
    }
    
    public int getDayNum()
    {
        double seconds = schedule.getTime();//1 steps per second
        return (int)(((seconds/60)/60)/(24.00)) + 1;
    }
    
    //PURPOSE: Add agents to the simulation and GeomVectorField
    //PARAMETERS: nothing
    //RETURN: nothing
    void addAgents()
    {
        aa = new agentAdder();
        schedule.scheduleRepeating(aa, 5);
        
        TimeRepeater time = new TimeRepeater();
        schedule.scheduleRepeating(time, 60);//update every (2nd param) steps
        
        AlertRepeater alert = new AlertRepeater();
        schedule.scheduleRepeating(alert);
        
        ScreenRepeater screen = new ScreenRepeater();
        schedule.scheduleRepeating(screen, 30);//Take a screen cap every (2nd param) seconds
    }

    //PURPOSE: Does whatever needs to be done when the program ends
    //PARAMETERS: nothing
    //RETURN: nothing
    @Override
    public void finish()
    {
        super.finish();
        
        if(recordSwap == true)
        {
            String movieLoc = "file:" + filePath + "/FinalMovie.avi";
            File file = new File(movieLoc);
            if(file.isFile())
                file.delete();
            MediaLocator ml;
            if ((ml = JpegImagesToMovie.createMediaLocator(movieLoc)) == null) 
                System.exit(0);

            JpegImagesToMovie j = new JpegImagesToMovie();
            j.doIt(this.getDedGUI().getProgFrame().getBounds().width, 
                   this.getDedGUI().getProgFrame().getBounds().height, 
                   25, screenList, ml); 

            file = new File(filePath + "/Screens");
            deleteDir(file);
            System.out.println("deleted movie directory");
        }
    }

    //PURPOSE: Does whatever needs to be done when the program starts
    //PARAMETERS: nothing
    //RETURN: nothing
    @Override
    public void start()
    {
        super.start();
        
        /*schedule.scheduleRepeating(schedule.EPOCH, new Steppable()
        {
            @Override
            public void step(SimState ship) {
                series.add((schedule.getSteps()), getNumInfected());
            }
        }, 100); //FOR ADDING TO THE SERIES LESS OFTEN*/
        
        agents.clear(); // clear any existing agents from previous runs
        addAgents();
        agents.setMBR(net.getMBR());
        schedule.scheduleRepeating( agents.scheduleSpatialIndexUpdater(), Integer.MAX_VALUE, 1.0);

        File file = new File(filePath);
        File file2 = new File(filePath + "/Screens");
        boolean isDirectoryCreated = file.mkdir();
        if (isDirectoryCreated) {
            System.out.println("successfully made movie directory");
        } else {
            deleteDir(file);
            file.mkdir();
            file2.mkdir();
//            System.out.println("deleted and made new movie directory");
        }
    }

    public static boolean deleteDir(File dir) 
    {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    //PURPOSE: to set data for all nodes on the cruise ship, such as whether it is a room or restaurant node
    //PARAMETERS: an iterator for every node on the Ship, and a GeomVectorField of intersections
    //RETURN: nothing
    private void setNodeData(Iterator<?> nodeIterator)
    {
        GeometryFactory fact = new GeometryFactory();
        Coordinate coord = null;
        Point point = null;
        int counter = 0;

        //Create bags of the various geometries of areas on the ship
        Bag roomBag = room.getGeometries();
        Bag diningBag = dining.getGeometries();
        if(diningRestricted) {
            diningBag.remove(4);
        }

        Bag boardingBag = boardingZone.getGeometries();
        Bag freeBag = new Bag();
        freeBag.addAll(casino.getGeometries());
        freeBag.addAll(bar.getGeometries());
        freeBag.addAll(theater.getGeometries());
        freeBag.addAll(shopping.getGeometries());
        freeBag.addAll(gym.getGeometries());
        freeBag.addAll(lounge.getGeometries());
        freeBag.addAll(pool.getGeometries());

        //TODO
        // REMOVE AREAS HERE
//        diningBag.remove(4);
//        freeBag.remove(7);
//        freeBag.remove(12);
//        freeBag.remove(13);

        while (nodeIterator.hasNext())
        {
            Node node = (Node)nodeIterator.next();
            coord = node.getCoordinate();
            point = fact.createPoint(coord);

            node.setData(counter);

            MasonGeometry junc = new MasonGeometry(point);
            junctions.addGeometry(junc);
            junc.addIntegerAttribute("ID", counter);

            boolean typed = false;
            typed = nodeAssign(roomBag, point, counter, "Room", typed);
            typed = nodeAssign(diningBag, point, counter, "Dining", typed);
            typed = nodeAssign(freeBag, point, counter, "Free", typed);
            typed = nodeAssign(boardingBag, point, counter, "Boarding", typed);
            if(typed == false)
                basicData.add(counter); 
                // System.out.println("Node #" + counter + " is a " + nodeData.get(counter));   
            
            //System.out.println((int)node.getData());
            counter++;
        }
        //Add columns to the nodeData 2 dimension vector
        nodeData.add(crewHomeData);//crew col 0
        nodeData.add(passHomeData);//pass col 1
        nodeData.add(diningData);//dine col 2
        nodeData.add(freeData);//free col 3
        nodeData.add(basicData);//basic col 4
        nodeData.add(boardingData);//boarding col 5
        
        if(runCode == 2)
        {
            for(int i = 0; i<100; i++)//define safe nodes
            {
                int ind = this.random.nextInt(2);
                int range = nodeData.get(ind).size();
                int rando = this.random.nextInt(range);
                safeNodes.add(nodeData.get(ind).get(rando));
            }
        }
    }
    
    public boolean nodeAssign(Bag polyBag, Point point, int counter, String polyType, boolean typed)
    {
        if(typed == true)
            return true;
        
        for(int i = 0; i < polyBag.size(); i++)
        {
            MasonGeometry obj = (MasonGeometry)polyBag.objs[i];
            if(obj.getGeometry().contains(point))
            {
                switch(polyType)//For various different types of rooms add to 2nd dimension of nodeData vector
                {
                    case "Room":
                        if("Crew".equals(obj.getStringAttribute("Type")))
                            crewHomeData.add(counter);
                        else
                            passHomeData.add(counter);
                        break;
                    case "Dining":
                        diningData.add(counter);
                        break;
                    case "Free":
                        freeData.add(counter);
                        break;
                    case "Boarding":
                        boardingData.add(counter);
                        break;
                    default:
                        System.out.println("Something Broke");
                }
                return true;//The value has been given a type
            }
        }
        return false;//The value has not been given a type
    }
    
    public void setDedGUI(ShipUI gui){shipGUI = gui;}
    public ShipUI getDedGUI(){return shipGUI;}
    
    public int getNumInfected(){ return numInfectedPersons; }
    public void addNumInfected(){ numInfectedPersons++; }
    
    public int getPrinterCount(){ return printerCounter; }
    public int getPassCount(){ return passengerCounter; }
    public int getStrucCount(){ return strucCrewCounter; }
    public int getUnstrucCount(){ return unstrucCrewCounter; }
    public void decrementPrinter(){ printerCounter--; }
    public void decrementPass(){ passengerCounter--; }
    public void decrementStruc(){ strucCrewCounter--; }
    public void decrementUnstruc(){ unstrucCrewCounter--; }
    public void setRecord(boolean set)
    {
        getDedGUI().switchRecord();
        record = set;
        recordSwap = true;
    }
    public boolean getRecord(){return record;}
    public int getRunCode(){return runCode;}
    
    public void addDailyInfect(){infectionsPerDay.add(((int)(schedule.getSteps()/86400)) + 1 );}
    public ArrayList<Integer> getDailyInfect()
    {
        if(infectionsPerDay.isEmpty())
        {
            for(int x=0; x<7; x++)
                infectionsPerDay.add(x);
        }

        return infectionsPerDay;
    }
    
    public Object[] getNodeArr(){ return nodeArr; }
    public double[] getInfectionsPerDayDoub()
    {     
        if(infectionsPerDay.isEmpty() == false)
            infectionsPerDayDoub = new double[infectionsPerDay.size()];
        else
            return null;
        
        for(int x=0; x<infectionsPerDay.size(); x++)
        {
            if(infectionsPerDay.get(x) != null)
                infectionsPerDayDoub[x] = infectionsPerDay.get(x).doubleValue();
            else
                infectionsPerDayDoub[x] = 0.0;
        }
            
        return infectionsPerDayDoub;
    }
    
    //PURPOSE: loop through the program
    //PARAMETERS: arguments for main function
    //RETURN: nothing
    public static void main(String[] args)
    {
        doLoop(Ship.class, args);
        System.exit(0);
    }
}
