package CruiseShipModel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;

public class AlertLabel extends JLabel
{
    int xG, yG;
    
    public AlertLabel(int x, int y)
    {
        super();
        xG = x-40;
        yG = y-40;
        setOpaque(false);
        setVerticalAlignment(JLabel.CENTER);
        setHorizontalAlignment(JLabel.CENTER);
        setForeground(Color.BLACK);
        setBounds(xG, yG, 60, 60);
        updateUI();
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setClip(0, 0, 60, 60);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(0, 0, 60, 60);
        g2d.setColor(new Color(200, 25, 25, 200));
        g2d.fillOval(0, 0, 60, 60);

        ui.update(g2d, this);
    }
}
