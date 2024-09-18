/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CruiseShipModel;

import java.awt.Color;
import sim.display.Display2D;

/**
 *
 * @author Jmuking
 */
public class CruiseDisplay2D extends Display2D{
    private final Ship state;
    private boolean movieStarted;
    
    public CruiseDisplay2D(double width, double height, ShipUI simulation) {
        super(width, height, simulation);
        state = (Ship)simulation.state;
    }
    
    @Override
    public void startMovie()
    {
        if(movieStarted == false)
        {
            state.setRecord(true);
            movieStarted = true;
        }
        else
        {
            state.setRecord(false);
            movieStarted = false;
        }
    }
}
