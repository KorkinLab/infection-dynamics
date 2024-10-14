package CruiseShipModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

public class GraphButton extends JPanel implements ActionListener
{
    protected JButton b;
    
    public GraphButton()
    {
        b = new JButton("Click for Statistics");
        b.addActionListener(this);
        
        add(b);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        
    }
    
}
