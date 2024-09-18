/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CruiseShipModel;

import sim.field.geo.*;
/**
 *
 * @author jmuking
 */
public class ShipVectorField extends GeomVectorField
{
    private boolean buildCondition = false;
    
    public ShipVectorField()
    {
        super();
    }
    
    public void updateSpatialIndex()
    {
        super.updateSpatialIndex();
    }
}
