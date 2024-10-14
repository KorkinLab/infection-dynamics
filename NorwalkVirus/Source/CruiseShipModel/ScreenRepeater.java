package CruiseShipModel;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import sim.engine.SimState;
import sim.engine.Steppable;

public class ScreenRepeater implements Steppable
{

    @Override
    public void step(SimState state) 
    {
        Ship world = (Ship)state;
        if(world.getRecord() == true)
        {
            Rectangle rect = world.getDedGUI().getProgFrame().getBounds();
            BufferedImage cap;
            try {
                cap = new Robot().createScreenCapture(rect);
                String path = world.filePath + "/Screens/Screen" + world.schedule.getSteps();
                ImageIO.write(cap, "jpg", new File(path));
                world.screenList.add(path);
            } catch (AWTException | IOException ex) {
                Logger.getLogger(TimeRepeater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
