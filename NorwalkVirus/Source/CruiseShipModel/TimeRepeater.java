/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CruiseShipModel;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author Jeffrey
 * This class allows for the time at the bottom of the simulation to be updated
 */
public class TimeRepeater implements Steppable
{
    @Override
    public void step(SimState state) 
    {
        Ship world = (Ship)state;
        world.shipGUI.updateTime(world);
    }
}
