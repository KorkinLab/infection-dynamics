package CruiseShipModel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class LegPanel extends JPanel
{
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(5, 350, 60, 60);
            g2d.setColor(new Color(200, 25, 25, 200));
            g2d.fillOval(5, 350, 60, 60);
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(60, 60);
        }
}
