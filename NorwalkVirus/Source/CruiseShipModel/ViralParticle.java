package CruiseShipModel;

import com.vividsolutions.jts.geom.Coordinate;
import sim.engine.SimState;
import sim.util.Bag;

public class ViralParticle extends Agent
{
    private int deathCounter = 0;
    
    public ViralParticle(Ship state, Coordinate startCoord) 
    {
        super(state);
        
        getGeomObject().addIntegerAttribute("INFECTED", 8);//is a viral particle if INFECTED == 8
        setMoveRate(0.0);//doesn't move
        setStart(startCoord);
    }
    
    @Override
    public void step(SimState state)
    {   
        Ship world = (Ship)state;
        
        if(deathCounter >= 86400)
            this.kill(world);
        
        if(deathCounter % 60 == 0)//reduce computations, only check infectivity ever 60 steps
        {
            if(shedding == 0)
                    shedding = SHED_START * world.random.nextDouble();
            Bag result = world.agents.getObjectsWithinDistance(this.getGeomObject(), (foot/2));//17.794/2 for .5 foot radius
            int numInfections = 0;
            for(int i = 0; i < result.numObjs; i++)
            {
                double itt = Math.log10(this.shedding);
                double prob = world.random.nextDouble();
                /*
                if(prob <= infprob[(int)itt] && ((MasonGeometry)result.objs[i]).getIntegerAttribute("INFECTED") == 0)
                {
                    numInfections=numInfections+1;
                    ((MasonGeometry)result.objs[i]).addIntegerAttribute("INFECTED", 1);
                }
                */
            }
            result.clear();
            //doesn't move
        }
        deathCounter++;
    }

    @Override
    protected String getAgentType() { return "Viral Particle"; }
}
