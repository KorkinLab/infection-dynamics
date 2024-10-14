package CruiseShipModel;

import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.util.geo.MasonGeometry;
import sim.util.gui.SimpleColorMap;

//A customized portrayal for every agent on the simulation's GeomVectorField
public class AgentPortrayal extends GeomPortrayal
{
    private static final long serialVersionUID = 6026649920581400781L;

    SimpleColorMap colorMap = null;
    Ship ship;

    //CONSTRUCTOR
    public AgentPortrayal(SimpleColorMap map)
    {
        super(1.0, true);//size of the portrayal is 10
        colorMap = map;
    }

    //PURPOSE: to draw the portrayal of the agents using previously define colorMap
    //PARAMETERS:  the object to draw, and other necessary draw parameters
    //RETURN: nothing
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
        MasonGeometry ag = (MasonGeometry)object;
        paint = colorMap.getColor(ag.getIntegerAttribute("PORTRAYAL") + ag.getIntegerAttribute("AGENT_TYPE"));
        super.draw(object, graphics, info);
    }
}
