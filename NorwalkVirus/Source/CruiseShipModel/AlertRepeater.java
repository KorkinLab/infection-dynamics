package CruiseShipModel;

import sim.engine.SimState;
import sim.engine.Steppable;

public class AlertRepeater implements Steppable
{
    @Override
    public void step(SimState state) 
    {
        Ship world = (Ship)state;
        world.shipGUI.updateAlert(world);
    }
}
