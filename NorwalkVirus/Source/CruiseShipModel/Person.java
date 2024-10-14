package CruiseShipModel;

import java.util.Random;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public abstract class Person extends Agent
{
    // 1 step is equal to 1 second
    public static final int minInSteps = 60;
    public static final int hourInSteps = 3600;
    public static final int dayInSteps = 86400;

    public static int totalPassengers = 0;
    public static int totalCrew = 0;
    
    public static int totalImmune = 0;
    public static int totalInfected = 0;
    public static int totalIll = 0;
    public static int totalDead = 0;
    public static int totalRecovered = 0;
    
    public static int totalPassengersImmune = 0;
    public static int totalCrewImmune = 0;
    
    public static int totalPassengersInfected = 0;
    public static int totalCrewInfected = 0;
    
    public static int totalPassengersIll = 0;
    public static int totalCrewIll = 0;
    
    public static int totalPassengersDead = 0;
    public static int totalCrewDead = 0;
    
    public static int totalPassengersRecovered = 0;
    public static int totalCrewRecovered = 0;
    public static int infectInterval = minInSteps * 20; // 20 minutes. Frequency of touching a surface and their face.
       
    // RT-PCR shedding values from the paper "Norwalk Virus Shedding after Experimental Human Infection - Figure 1 C and E".
    protected static final double[] symptomaticShedding =  {7.75, 9, 11, 11, 11, 10, 10, 9.5, 9, 9, 8, 8, 8, 8, 8 };
    protected static final double[] asymptomaticShedding = {7.75, 9.5, 10.5, 10, 9, 8, 7.75, 7.75, 7.75, 7.75, 7.75, 7.75, 7.75, 7.75, 7.75 };
    
    public static final double doseAdjustment = 4;
    public static final double immuneRatio = 0.2;
    public static int immunePopulation = (int) (immuneRatio * Ship.totalOnboard);
//    private static final int[] initCases = {5, 6};
//    private static int caseRnd = new Random().nextInt(initCases.length);
    public static int initialInfectedPopulation = 0;
	public static final double mortalityRate = 0;		// Ratio between 0 and 1
//    public static double initialCrewInfectionRate = 0.01;
//    public static double initialPassInfectionRate = 0.01;
    public static int infectionCallCounter = 0;
    public static int illnessCallCounter = 0;
    
    /**
     * The below coefficient values are from the paper "Norwalk virus: How infectious is it".
     * Table III. Maximum likelihood estimates.
     */

    // For non-aggregated particles
    private static final double alpha = 0.111;
    private static final double beta = 32.81;
    private static final double eta = 0.508;
    private static final double gamma = 0.095;

    // For aggregated particles
//    private static double alpha = 0.00535;
//    private static double beta = 0.00251;
//    private static double eta = 0.000873;
//    private static double gamma = 0.095;

    private static int peopleCounter = 0;
//    private boolean shortRecovery;

    /** 
     * Constructor 
     * @param state
     */
    
    public Person(Ship state, String personType) {
        super( state );        
        initializeNodes(state);
        setStart(getBoardingNode().getCoordinate());
        initMoveRate(state);
        peopleCounter++;
        if("Passenger".equals(personType)) {
        	totalPassengers++;
        }
        else {
        	totalCrew++;
        }
        
        // Set immune population
        if( (immunePopulation > 0) && ((peopleCounter % 5) == 0)) {
        	getGeomObject().addIntegerAttribute("IMMUNE", 1);  		// Is a person who is naturally immune to the disease. Negative Secretor in literature.
        	totalImmune++;
        	if("Passenger".equals(personType)) {
            	totalPassengersImmune++;
            }
            else {
            	totalCrewImmune++;
            }
            immunePopulation--;
        }
        else {
        	getGeomObject().addIntegerAttribute("IMMUNE", 0);  		// is a person who is not immune to the disease. Positive Secretor in literature.
        }

//        if ((peopleCounter % 10) == 0) {    // Norwalk: 10% of population has long recovery
//            shortRecovery = false;
//        }
//        else {
//            shortRecovery = true;
//        }
                
        // Set initial infected population
        if( (initialInfectedPopulation > 0) && (getGeomObject().getIntegerAttribute("IMMUNE") == 0)) {
            getGeomObject().addIntegerAttribute("INFECTED", 1);
            getGeomObject().addIntegerAttribute("TIME_INFECTED", -dayInSteps);
            getGeomObject().addIntegerAttribute("PORTRAYAL", 2);			// Set the color portrayal. Symptomatic.
            getGeomObject().addIntegerAttribute( "ILL", 1); 				// Person is already ill because incubation is complete.
            getGeomObject().addDoubleAttribute("ACQUIRED_PARTICLES", Math.pow( 10, symptomaticShedding[1]) - doseAdjustment);
//            getGeomObject().addStringAttribute("SHORT_RECOVERY", "yes");    // Default recovery duration

            totalInfected++;
            totalIll++;
            if("Passenger".equals(personType)) {
            	totalPassengersInfected++;
            	totalPassengersIll++;
            }
            else {
            	totalCrewInfected++;
            	totalCrewIll++;
            }
            initialInfectedPopulation--;
        }
        else {
            getGeomObject().addIntegerAttribute("INFECTED", 0);		// 0 means not infected.
            getGeomObject().addIntegerAttribute("ILL", 0);
            getGeomObject().addIntegerAttribute("PORTRAYAL", 0);
            getGeomObject().addDoubleAttribute("ACQUIRED_PARTICLES", 0.0);
//            getGeomObject().addStringAttribute("SHORT_RECOVERY", "yes");    // Default recovery duration
        }
        
        // quick logic check
//        if( (getGeomObject().getIntegerAttribute("INFECTED") > 0) &&
//                (getGeomObject().getIntegerAttribute("IMMUNE") == 1)) {
//           System.out.println( "An immune person became infected, this should not happen, review code");
//           System.exit(1);
//        }

        resetVisited();
        resetAt();
    }

    /**
     * Probabilities depend on the shedding value.
     * For infection the formula used is: P(inf) = 1 - (1 + (dose / beta)) ^ (-alpha)
     * @param sheddingValue
     * @param state
     * @return
     */
    protected static boolean becomesInfected(double sheddingValue, SimState state) {
        if(sheddingValue > 0) {
            double infProb = 1.0 - (Math.pow( 1.0 + ( sheddingValue / beta ), -alpha ) );
//            double rand = state.random.nextDouble();
//            System.out.println("--- Infect ---");
//            System.out.println("ExternalParticles: " + sheddingValue);
//            infectionCallCounter++;
//            System.out.println("Call Counter: " + infectionCallCounter);
//            System.out.println("infProb: "+ infProb);
//            System.out.println("nextDouble: "+ rand);
////    	    return rand <= infProb;
//            System.out.println("------");
            return (infProb > 0.5);
        }
        else {
            return false;
        }
    }

    // Both the below equations and the constant values are found in the paper "Norwalk virus: How infectious is it?".
    /**
     * Probabilities depend on the shedding value.
     * For illness the formula used is: P(ill) = 1 - (1 + (eta * dose)) ^ (-gamma)
     * @param sheddingValue
     * @param state
     * @return
     */
    protected static boolean becomesIll(double sheddingValue, SimState state) {   
    	if(sheddingValue > 0) {
    		double illProb = 1.0 - (Math.pow( 1.0 + ( eta * sheddingValue ), -gamma ) );
//    		double rand = state.random.nextDouble();
//            return rand <= illProb;
//            System.out.println("--- Ill ---");
//            System.out.println("InternalParticles: " + sheddingValue);
//            illnessCallCounter++;
//            System.out.println("Call Counter: " + illnessCallCounter);
//            System.out.println("illProb: "+ illProb);
//            System.out.println("nextDouble: "+ rand);
//            System.out.println("------");
            return (illProb > 0.3);
    	}
    	else {
    		return false;
    	}
    }
    

    
    @Override
    protected String getAgentType() { 
    	return "Person"; 
    }
    
    @Override
    public void step(SimState state)
    {   
    	int daysPostInfection = 0;
        Ship world = (Ship)state;
        
        if( (world.getTime() / 4.0) == 1.0 )
            initializeNodes(world);	// New preferred areas each day (at 4AM when people are asleep for sure).

        if( getGeomObject().getIntegerAttribute("INFECTED") == 1 ) {
//            System.out.println("*** START infection ***");
        	int timeWhenInfected = getGeomObject().getIntegerAttribute("TIME_INFECTED");
        	int stateTime = (int)state.schedule.getTime();
            // the duration in steps that the person has been infected
            daysPostInfection = (stateTime - timeWhenInfected) / dayInSteps;
//            if(daysPostInfection == 0) {
//                System.out.println("time: " + (stateTime - timeWhenInfected));
//                System.out.println("DPI: " + daysPostInfection);
//            }
            boolean halfDay = (stateTime - timeWhenInfected) >= (dayInSteps / 2);
            int illnessAttr = getGeomObject().getIntegerAttribute("ILL");
            String agentType = this.getAgentType();
            double acquiredViralParticles = getGeomObject().getDoubleAttribute("ACQUIRED_PARTICLES");
//            if( (acquiredViralParticles == 0.0) || (acquiredViralParticles == 1.0) ) {
//                System.out.println("illnessAttr: " + illnessAttr);
//                System.out.println("halfDay: " + halfDay);
//                System.out.println("acquiredViralParticles: " + acquiredViralParticles);
//            }
            // If incubation period is complete, then check if person will fall ill.
            if( (illnessAttr == 0) && (halfDay) && becomesIll(acquiredViralParticles, state)) {
            	getGeomObject().addIntegerAttribute("ILL", 1);		// Person is symptomatic. Symptomatic shedding.
                getGeomObject().addIntegerAttribute("PORTRAYAL", 2);	// Person color for symptomatic.
                totalIll++;
                if( agentType.equals("Passenger") ) {
                    totalPassengersIll++;
                }
                else if( agentType.startsWith("Crew")  ) {
                    totalCrewIll++;
                }
            }
            // Illness phase
//            else if( (illnessAttr == 1) && ((daysPostInfection == 1) || (daysPostInfection == 2 ))) {
//                setMoveRate(savedMoveRate / 2.0);
//            }
			// Death and Recovery
//            else if( daysPostInfection >= 3 ) {
                // Death
//            	if( (daysPostInfection <= 7) && (illnessAttr == 1) && (totalDead < (int)(mortalityRate * totalIll)) ) {
//            		getLocation().addIntegerAttribute("INFECTED", 3);		// Person is dead.
//                    getLocation().addIntegerAttribute("ILL", 1);			// Still symptomatic shedding.
//                    getLocation().addIntegerAttribute("PORTRAYAL", 3);		// Person color for deceased.
//                    getLocation().addIntegerAttribute("TIME_DECEASED", (int)state.schedule.getTime());
//                    setMoveRate(0.0);										//does not move
//
//                    totalDead++;
//                    if( this.getAgentType().equals("Passenger") ) {
//                        totalPassengersDead++;
//                    }
//                    else if( this.getAgentType().startsWith("Crew")  ) {
//                        totalCrewDead++;
//                    }
//            	}

				// Recovery
            	if( (daysPostInfection >= 3) && ((illnessAttr == 0) || (illnessAttr == 1))
                        && (getGeomObject().getIntegerAttribute("INFECTED") != 3)) {

                    getGeomObject().addIntegerAttribute("INFECTED", 2);
            	    getGeomObject().addIntegerAttribute("ILL", 2); 			// Both symptomatic and asymptomatic patients recover. No more shedding.
            		getGeomObject().addIntegerAttribute("PORTRAYAL", 0);		// Set this back to uninfected color.
            		setMoveRate(savedMoveRate);
            		totalRecovered++;	
                    if( agentType.equals("Passenger") ) {
                        totalPassengersRecovered++;
                    }
                    else if( agentType.startsWith("Crew")  ) {
                        totalCrewRecovered++;
                    }
                    
            	}

//                if( (daysPostInfection >= 4) && ((illnessAttr == 0) || (illnessAttr == 1))
//                        && (getGeomObject().getIntegerAttribute("INFECTED") != 3)
//                        && getGeomObject().getStringAttribute("SHORT_RECOVERY").equals("no")) {
//
//                    getGeomObject().addIntegerAttribute("INFECTED", 2);
//                    getGeomObject().addIntegerAttribute("ILL", 2); 			// Both symptomatic and asymptomatic patients recover. No more shedding.
//                    getGeomObject().addIntegerAttribute("PORTRAYAL", 0);		// Set this back to uninfected color.
//                    setMoveRate(savedMoveRate);
//                    totalRecovered++;
//                    if( agentType.equals("Passenger") ) {
//                        totalPassengersRecovered++;
//                    }
//                    else if( agentType.startsWith("Crew")  ) {
//                        totalCrewRecovered++;
//                    }
////                    System.out.println("Delayed Recovery");
//                }

//            }

            if( ((int) state.schedule.getTime() % infectInterval == 0) ) {
            	Bag people = world.agents.getObjectsWithinDistance(this.getGeomObject(), (foot/2));//17.794/2 for .5 foot radius
                int newIllnessAttr = getGeomObject().getIntegerAttribute("ILL");
                double sheddingValue = 1.0;  										// the current shedding value for this person.
                try {
                    if( newIllnessAttr == 1 ) {
                        sheddingValue = Math.pow(10, symptomaticShedding[daysPostInfection] - doseAdjustment);
                    }
                    else if ( (newIllnessAttr == 0) && (halfDay) ) { // Completed incubation and is asymptomatic
                        sheddingValue = Math.pow( 10, asymptomaticShedding[ daysPostInfection ] - doseAdjustment );
                        System.out.println("Completed incubation and is asymptomatic");
                    }

                    if (sheddingValue < 1)
                        sheddingValue = 1.0;

                } catch ( ArrayIndexOutOfBoundsException e )
                {
                    // We currently have no data for shedding values after 14 days.
                	// Set a default
                    System.out.println("xxx EXCEPTION xxx");
                    sheddingValue = 1.0;
                }
                
//                System.out.println( "people.numObjs:" + people.numObjs + ", /10: " + Math.ceil ( ( double ) people.numObjs / 10 ) );

                boolean infectionProbability = becomesInfected(sheddingValue, state);
                // Infect people in the vicinity - R0
                int[] avgR = {1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2};
                int rnd = new Random().nextInt(avgR.length);
                int vicinityLimit = avgR[rnd];
                int vicinityObjects = people.numObjs;
                int iterLimit = 0;
                if (vicinityObjects <= vicinityLimit) {
                    iterLimit = vicinityObjects;
                }
                else {
                    iterLimit = vicinityLimit;      // Default value for R0
                }
//                int iterLimit = ThreadLocalRandom.current().nextInt(0, 4);
                for( int i = 0; i < iterLimit ; i++ )
                {
                    // is newly infected
//                    System.out.println( "--- Vicinity ---");
//                    System.out.println( "iterLimit: " + iterLimit);
                    if( ( ( (MasonGeometry)people.objs[ i ] ).getIntegerAttribute("INFECTED") == 0)
                    	&& ( ((MasonGeometry) people.objs[ i ]).getIntegerAttribute("IMMUNE") == 0 )
                       	&&  infectionProbability) {
                        world.addNumInfected();
                        world.addDailyInfect();

//                        System.out.println( "Selected R0: " + iterLimit);
//                        System.out.println( "people.numObjs: " + people.numObjs);
                        System.out.println(((MasonGeometry) people.objs[ i ]).getGeometry().getCoordinate().toString());

                        totalInfected++;
                        if( ((MasonGeometry) people.objs[ i ]).getIntegerAttribute("AGENT_TYPE") == 0 ) {
                            totalPassengersInfected++;
                        }
                        else {
                            totalCrewInfected++;
                        }
                        
                        ((MasonGeometry)people.objs[i]).addIntegerAttribute("TIME_INFECTED", (int)state.schedule.getTime() );
                        ((MasonGeometry)people.objs[i]).addIntegerAttribute("PORTRAYAL", 1);		// Color that person is asymptomatic.
                        ((MasonGeometry)people.objs[i]).addIntegerAttribute("INFECTED", 1);
                        ((MasonGeometry)people.objs[i]).addIntegerAttribute("ILL", 0);				// Indicates asymptomatic shedding.
                        ((MasonGeometry)people.objs[i]).addDoubleAttribute("ACQUIRED_PARTICLES", sheddingValue);
                            
                    }
                    
                }
                people.clear();
            }
//            System.out.println("*** END infection ***");
        }

        // Check if person is dead
        if(mortalityRate > 0) {
            int infectedStatus = getGeomObject().getIntegerAttribute("INFECTED");
            if( infectedStatus == 3 ) {
                int minsPostDeath = 0;
                int timeWhenDeceased = getGeomObject().getIntegerAttribute("TIME_DECEASED");
                int stateTime = (int)state.schedule.getTime();
                minsPostDeath = (stateTime - timeWhenDeceased) / minInSteps;    // the duration in steps that the person has been dead
                if(minsPostDeath == 10) {
                    // Remove the body
//                System.out.println("Dead person removed at - " + state.schedule.getTime());
                    kill(world);
                }
            }
        }
        else{
            move(world);
        }

    }
}
