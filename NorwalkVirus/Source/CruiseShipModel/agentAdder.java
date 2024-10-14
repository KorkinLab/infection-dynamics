package CruiseShipModel;

import sim.engine.SimState;
import sim.engine.Steppable;

public class agentAdder implements Steppable
{   
    @Override
    public void step(SimState state) 
    {
        Ship world = (Ship)state;
        
        
        if(world.getPrinterCount() > 0)
        {
            PrintAgent print = new PrintAgent(world);
            world.agentList.add(print);
            world.schedule.scheduleRepeating(print, 1.0);
            world.decrementPrinter();
            return;
        }
        else if(world.getStrucCount() > 0 || world.getUnstrucCount() > 0)
        {
            if(world.getStrucCount() > 0)
            {
                StrucCrew c = new StrucCrew(world, "Crew");
                c.setStart(c.getBoardingNode().getCoordinate());
                world.agentList.add(c);
                world.agents.addGeometry(c.getGeometry());
                world.schedule.scheduleRepeating(c, 1.0);
                world.decrementStruc();
            }
            if(world.getUnstrucCount() > 0)
            {
                UnstrucCrew u = new UnstrucCrew(world, "Crew");
                u.setStart(u.getBoardingNode().getCoordinate());
                world.agentList.add(u);
                world.agents.addGeometry(u.getGeometry());
                world.schedule.scheduleRepeating(u, 1.0);
                world.decrementUnstruc();
            }
            //world.schedule.scheduleOnce(this);
            return;
        }
        else if(world.getPassCount() > 0)
        {
            Passenger p = new Passenger(world, "Passenger");
            p.setStart(p.getBoardingNode().getCoordinate());
            world.agentList.add(p);
            world.agents.addGeometry(p.getGeometry());
            world.schedule.scheduleRepeating(p, 1.0);
            world.decrementPass();
            //world.schedule.scheduleOnce(this);
            return;
        }
        
    }
}
