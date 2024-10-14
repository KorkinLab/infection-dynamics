package CruiseShipModel;

import com.vividsolutions.jts.io.ParseException;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import sim.display.ChartUtilities;
import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.gui.SimpleColorMap;
import sim.util.media.chart.HistogramGenerator;
import sim.util.media.chart.HistogramSeriesAttributes;
import sim.util.media.chart.TimeSeriesAttributes;
import sim.util.media.chart.TimeSeriesChartGenerator;

//Acts as the UI for the program. Displays any relevant information and agent movement among the GIS shapefiles in the console window.
public class ShipUI extends GUIState
{
    private CruiseDisplay2D display, displayZoom;
    private JFrame displayFrame;
    private final String title = "INFECTION DYNAMICS SIMULATION - NORWALK VIRUS";
    private final String day = "DAY: ";
    private final String hour = "HOURS: ";
    
    private Color[] areaColors = {new Color(150,150,150), new Color(250,150,200), new Color(175,50,100), new Color(0, 200, 100),
                              new Color(25,25,115), new Color(128,128,0), new Color(0,150,75), Color.BLACK, Color.PINK};
    //PASSENGER COLORS
    Color passUninfected = new Color(0, 204, 204); // 
    Color passAsymptomatic = new Color(0, 0, 204); // 
    Color passSymptomatic = new Color(204, 0, 204); //
    Color passengerDeceased = Color.BLACK; //

    //CREW COLORS
    Color crewUninfected = new Color(0, 204, 0); // 
    Color crewAsymptomatic = new Color(204, 204, 0); // 
    Color crewSymptomatic = new Color(204, 0, 0); // 
    Color crewDeceased = new Color(204, 0, 102); //

    Color viralParticle = new Color(255, 51, 51); // 
    
    private  Color[] agentColors = {passUninfected, 
                                    passAsymptomatic, 
                                    passSymptomatic, 
                                    passengerDeceased, 
                                    crewUninfected, 
                                    crewAsymptomatic, 
                                    crewSymptomatic, 
                                    crewDeceased,
                                    viralParticle};

    private final GeomVectorFieldPortrayal netPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal intersectionPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal agentPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal roomsPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal diningPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal casinoPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal barPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal theaterPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal shoppingPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal gymPortrayal = new GeomVectorFieldPortrayal();
    private final GeomVectorFieldPortrayal loungePortrayal = new GeomVectorFieldPortrayal();
    
    final String SIMPANEL = "SIMULATION";
    final String SIMINFOPANEL = "SIMULATION INFO";
    private JPanel simulationPane, firstSimPane, secondSimPane, zoomPane;
    private LegPanel legendPane;
    private JScrollPane scroller, zoomScroller;
    private JLabel recordLabel;
    private AlertLabel alertLabel;
    private JTextField timeTxt;
    private JTextPane legendTxt, alertTxt;
    private SimpleAttributeSet legendSet;
    private JLayeredPane layDisplay;
    private DecimalFormat timeForm = new DecimalFormat("0.00##");
    
    private int alertCounter = 0;
    private double alertTime = 0;
    
    public TimeSeriesChartGenerator myTimeChart;
    public HistogramGenerator myHistoChart;
    public TimeSeriesAttributes myTimeAttributes;
    public HistogramSeriesAttributes myHistoAttributes;
    
    private String[] eventTimes;
    
    //CONSTRUCTOR
    public ShipUI(SimState state)
    {
        super(state);
    }

    //CONSTRUCTOR
    public ShipUI() throws ParseException
    {
        super(new Ship(System.currentTimeMillis()));
    }

    //PURPOSE: to initialize and attach every portrayal to the display2D class that displays on the console window
    //PARAMETERS: a Controller variable
    //RETURN: nothing
    @Override
    public void init(Controller controller)
    {
        super.init(controller);
        
        eventTimes = new String[24];
        for(int i=0; i<24; i++)
        {
            switch(i)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    eventTimes[i] = "MIDDLE OF THE NIGHT";
                    break;
                case 6:
                case 7:
                    eventTimes[i] = "EARLY MORNING";
                    break;
                case 8:
                case 9:
                    eventTimes[i] = "MID MORNING";
                    break;
                case 10:
                case 11:
                    eventTimes[i] = "LATE MORNING";
                    break;
                case 12:
                case 13:
                    eventTimes[i] = "EARLY AFTERNOON";
                    break;
                case 14:
                case 15:
                    eventTimes[i] = "MID AFTERNOON";
                    break;
                case 16:
                case 17:
                    eventTimes[i] = "LATE AFTERNOON";
                    break;
                case 18:
                case 19:
                    eventTimes[i] = "EARLY EVENING";
                    break;
                case 20:
                case 21:
                    eventTimes[i] = "MID EVENING";
                    break;
                case 22:
                case 23:
                    eventTimes[i] = "LATE EVENING";
                    break;                
            }
        }
        
        myTimeChart = ChartUtilities.buildTimeSeriesChartGenerator("REAL TIME INFECTIONS STATISTICS", "NUMBER OF STEPS");
        myTimeAttributes = ChartUtilities.addSeries(myTimeChart, "INFECTIONS PER STEP");
        myTimeChart.setYAxisLabel("NUMBER OF INFECTIONS");
        
        myHistoChart = ChartUtilities.buildHistogramGenerator("DAILY INFECTION STATISTICS", "TEST");
        myHistoAttributes = ChartUtilities.addSeries(myHistoChart, "INFECTIONS PER DAY", 7);
        //myHistoChart.setHistogramType(HistogramType.FREQUENCY);
        myHistoChart.setXAxisRange(0, 8);
        myHistoChart.setYAxisLabel("NUMBER OF INFECTIONS");
        myHistoChart.setXAxisLabel("DAY NUMBER");
        
        simulationPane = new JPanel();
        simulationPane.setLayout(new BoxLayout(simulationPane, BoxLayout.Y_AXIS));
        
        firstSimPane = new JPanel();
        firstSimPane.setLayout(new BoxLayout(firstSimPane, BoxLayout.X_AXIS));
        
        secondSimPane = new JPanel();
        secondSimPane.setLayout(new BoxLayout(secondSimPane, BoxLayout.X_AXIS));
        
        legendPane = new LegPanel();
        legendPane.setLayout(new FlowLayout(FlowLayout.LEFT));
        //legendPane.setLayout(new BoxLayout(legendPane, BoxLayout.Y_AXIS));
        
        zoomPane = new JPanel();
        zoomPane.setLayout(new BoxLayout(zoomPane, BoxLayout.Y_AXIS));
        Border bord = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "HIGH ACTIVITY AREA", 
                                                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
                                                       new Font("Verdana", Font.BOLD, 15));
        zoomPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 25));
        
        
        display = new CruiseDisplay2D(Ship.WIDTH * 3/4, Ship.HEIGHT * 3/4, this);
        displayZoom = new CruiseDisplay2D(Ship.WIDTH * 3/4, Ship.HEIGHT * 3/4, this);
        
        displayZoom.setBorder(bord);
        
        display.attach(roomsPortrayal, "Rooms", true);
        display.attach(diningPortrayal, "Dining", true);
        display.attach(casinoPortrayal, "Casino", true);
        display.attach(barPortrayal, "Bars", true);
        display.attach(gymPortrayal, "Gyms", true);
        display.attach(theaterPortrayal, "Theaters", true);
        display.attach(shoppingPortrayal, "Shopping", true);
        display.attach(loungePortrayal, "Lounges", true);
        display.attach(netPortrayal, "Halls", true);//net and intersection must be drawn last but before agents
        display.attach(intersectionPortrayal, "Intersections");//net and intersection must be drawn last but before agents
        display.attach(agentPortrayal, "Agents", true);
        
        displayZoom.attach(roomsPortrayal, "Rooms", true);
        displayZoom.attach(diningPortrayal, "Dining", true);
        displayZoom.attach(casinoPortrayal, "Casino", true);
        displayZoom.attach(barPortrayal, "Bars", true);
        displayZoom.attach(gymPortrayal, "Gyms", true);
        displayZoom.attach(theaterPortrayal, "Theaters", true);
        displayZoom.attach(shoppingPortrayal, "Shopping", true);
        displayZoom.attach(loungePortrayal, "Lounges", true);
        displayZoom.attach(netPortrayal, "Halls", true);//net and intersection must be drawn last but before agents
        displayZoom.attach(intersectionPortrayal, "Intersections");//net and intersection must be drawn last but before agents
        displayZoom.attach(agentPortrayal, "Agents", true);
        
        displayFrame = new JFrame(title);
        displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        displayFrame.setContentPane(simulationPane);
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    //PURPOSE: to start the shipUI
    //PARAMETERS: nothing
    //RETURN: nothing
    @Override
    public void start()
    {
        super.start();

        myTimeChart.clearAllSeries();

        //MinGapDataCuller culler = new MinGapDataCuller(50);
        //myTimeChart.setDataCuller(culler);
        //((Ship)state).series = myTimeAttributes.getSeries();
        //ChartUtilities.scheduleSeries(this, myTimeAttributes, null); //FOR UPDATING LESS OFTEN
        
        ChartUtilities.scheduleSeries(this, myTimeAttributes, new sim.util.Valuable()
        {
            @Override
            public double doubleValue()
            {
                return ((Ship)state).getNumInfected(); // myData is updated every timestep
            }
        });
        
        ChartUtilities.scheduleSeries(this, myHistoAttributes, new sim.display.ChartUtilities.ProvidesDoubles()
        {
            @Override
            public double[] provide() 
            {               
                return ((Ship)state).getInfectionsPerDayDoub();
            }
        });
        
                setupPortrayals();
    }
    

    //PURPOSE: to set up the portrayals (representations) for every part of the cruise ship and assign a GeomVectorField. Namely, any imported GIS shapefiles.
    //PARAMETERS: nothing
    //RETURN: nothing
    private void setupPortrayals()
    {
        Ship world = (Ship)state;
        world.setDedGUI(this);

        display.setBackdrop(Color.WHITE);
        
        roomsPortrayal.setField(world.room);
        roomsPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[0], true));      
        diningPortrayal.setField(world.dining);
        diningPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[1],true));
        casinoPortrayal.setField(world.casino);
        casinoPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[2], true));
        barPortrayal.setField(world.bar);
        barPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[3], true));
        gymPortrayal.setField(world.gym);
        gymPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[4], true));
        theaterPortrayal.setField(world.theater);
        theaterPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[5], true));
        shoppingPortrayal.setField(world.shopping);
        shoppingPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[6], true));
        loungePortrayal.setField(world.lounge);
        loungePortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[0], true));
        intersectionPortrayal.setField(world.junctions);
        intersectionPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[8],.1, true));
        netPortrayal.setField(world.net);
        netPortrayal.setPortrayalForAll(new GeomPortrayal(areaColors[7],true));
        
        agentPortrayal.setField(world.agents);
        
        agentPortrayal.setPortrayalForAll(new AgentPortrayal(new SimpleColorMap(agentColors)));
        
        setupGUI();
    }
    
    public void setupGUI()
    {
        Ship world = (Ship)state;
        
        Font font = new Font("Verdana", Font.PLAIN,20);
        
        StyledDocument legendDoc = new DefaultStyledDocument();
        legendTxt = new JTextPane(legendDoc);  
        legendTxt.setEditable(false);
        legendSet = new SimpleAttributeSet();
        createLegend(areaColors, agentColors, legendDoc);
        
        timeTxt = new JTextField();
        
        displayFrame.setPreferredSize(new Dimension(Ship.WIDTH, Ship.HEIGHT)); 

        layDisplay = new JLayeredPane();
        
        firstSimPane.setBackground(new Color(240,240,240, 100));
        firstSimPane.add(layDisplay, 0);
        firstSimPane.add(legendPane, 1);
        //firstSimPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        firstSimPane.setPreferredSize(new Dimension(displayFrame.getPreferredSize().width, displayFrame.getPreferredSize().height*3/6));
        
        secondSimPane.setBackground(new Color(240,240,240, 0));
        secondSimPane.add(myTimeChart.getChartPanel(), 0);
        secondSimPane.add(myHistoChart.getChartPanel(), 1);
        secondSimPane.add(zoomPane, 2);
        //secondSimPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        secondSimPane.setPreferredSize(new Dimension(displayFrame.getPreferredSize().width, displayFrame.getPreferredSize().height*2/6));
        
        layDisplay.add(display, new Integer(0), 0);
        //layDisplay.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        zoomPane.add(displayZoom, 0);
        
        legendPane.add(legendTxt, 0);
        legendPane.setAlignmentX(Component.LEFT_ALIGNMENT);
       
        legendTxt.setAlignmentX(Component.LEFT_ALIGNMENT);    
        //legendTxt.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        legendTxt.setBackground(new Color(240,240,240, 0));
        
        zoomPane.setPreferredSize(new Dimension(legendPane.getPreferredSize().width, zoomPane.getPreferredSize().height));
        
        alertLabel = new AlertLabel(display.getWidth()/2, display.getHeight()/2);
        alertLabel.setVisible(false);
        layDisplay.add(alertLabel, new Integer(1));
        
        recordLabel = new JLabel("RECORDING", JLabel.CENTER);
        recordLabel.setOpaque(true);
        recordLabel.setForeground(Color.BLACK);
        recordLabel.setBackground(Color.RED);
        recordLabel.setHorizontalTextPosition(JLabel.CENTER);
        recordLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        recordLabel.setVisible(false);
        layDisplay.add(recordLabel, new Integer(2));
        
        myTimeChart.getChart().setBackgroundPaint(new Color(240,240,240));
        myTimeChart.getChart().getPlot().setBackgroundPaint(new Color(240,240,240));
        myTimeChart.getChartPanel().setBackground(new Color(240,240,240, 100));
        //myTimeChart.getChartPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
        myTimeChart.getChartPanel().setPreferredSize(new Dimension(layDisplay.getWidth() / 2, displayFrame.getHeight() * 2/6));
        myTimeChart.setFont(font);
        myTimeChart.setXAxisRange(0, 15000);
        myTimeChart.setYAxisRange(0, world.getNumAgents());

        myHistoChart.getChart().setBackgroundPaint(new Color(240,240,240));
        myHistoChart.getChart().getPlot().setBackgroundPaint(new Color(240,240,240));
        //myHistoChart.getChartPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
        myHistoChart.getChartPanel().setPreferredSize(new Dimension(layDisplay.getWidth() / 2, displayFrame.getHeight() * 2/6));
        myHistoChart.setFont(font);
        myHistoChart.getChartPanel().setBackground(new Color(240,240,240, 100));   
        
        displayFrame.getContentPane().add(firstSimPane, 0);
        displayFrame.getContentPane().add(secondSimPane, 1);
        
        font = new Font("Verdana", Font.BOLD, 15);
        timeTxt = new JTextField(day + world.getDayNum() + ", " + hour + 6 + ":" + String.format("%02d", (int)0) + ", " + eventTimes[(int)6]);
        timeTxt.setFont(font);
        timeTxt.setHorizontalAlignment(JTextField.CENTER);
        timeTxt.setBorder(BorderFactory.createEmptyBorder());
        timeTxt.setEditable(false);
        timeTxt.setPreferredSize(new Dimension(displayFrame.getWidth(), timeTxt.getPreferredSize().height));
        
        displayFrame.getContentPane().add(timeTxt, 2);
        
        
        display.addHierarchyBoundsListener(new HierarchyBoundsListener() 
        {
            @Override
            public void ancestorMoved(HierarchyEvent e) {
                firstSimPane.setPreferredSize(new Dimension(displayFrame.getPreferredSize().width, displayFrame.getPreferredSize().height*3/6));
                secondSimPane.setPreferredSize(new Dimension(displayFrame.getPreferredSize().width, displayFrame.getPreferredSize().height*2/6));
                layDisplay.setPreferredSize(new Dimension(displayFrame.getWidth() * 4/6, displayFrame.getHeight() * 3/6));
                int width=Ship.WIDTH * 4/6, height=Ship.HEIGHT/2;
                if(layDisplay.getWidth() < Ship.WIDTH * 4/6 )
                    width = layDisplay.getWidth();
                if(layDisplay.getHeight() < Ship.HEIGHT/2 )
                    height = layDisplay.getHeight();
                display.setBounds((layDisplay.getWidth()-width) / 2, 0, width, height);
                recordLabel.setBounds((layDisplay.getWidth()-width)/2 + 5, 30, 100, 25);
                myTimeChart.getChartPanel().setPreferredSize(new Dimension(layDisplay.getWidth() / 2, displayFrame.getHeight() * 2/6));
                myHistoChart.getChartPanel().setPreferredSize(new Dimension(layDisplay.getWidth() / 2, displayFrame.getHeight() * 2/6));
                //display.setBounds(0, 0, 
                //                 (int)(Ship.WIDTH/(1.5))-(int)((JScrollPane)display.getComponent(1)).getVerticalScrollBar().getPreferredSize().getWidth(), 
                //                  displayFrame.getHeight()/2-(int)((JScrollPane)display.getComponent(1)).getVerticalScrollBar().getPreferredSize().getWidth()*4);
            }

            @Override
            public void ancestorResized(HierarchyEvent e) {
                firstSimPane.setPreferredSize(new Dimension(displayFrame.getPreferredSize().width, displayFrame.getPreferredSize().height*3/6));
                secondSimPane.setPreferredSize(new Dimension(displayFrame.getPreferredSize().width, displayFrame.getPreferredSize().height*2/6));
                layDisplay.setPreferredSize(new Dimension(displayFrame.getWidth() * 4/6, displayFrame.getHeight() * 3/6));
                int width=Ship.WIDTH * 4/6, height=Ship.HEIGHT/2;
                if(layDisplay.getWidth() < Ship.WIDTH * 4/6 )
                    width = layDisplay.getWidth();
                if(layDisplay.getHeight() < Ship.HEIGHT/2 )
                    height = layDisplay.getHeight();
                display.setBounds((layDisplay.getWidth()-width) / 2, 0, width, height);
                recordLabel.setBounds((layDisplay.getWidth()-width)/2 + 5, 30, 100, 25);
                myTimeChart.getChartPanel().setPreferredSize(new Dimension(layDisplay.getWidth() / 2, displayFrame.getHeight() *2/6));
                myHistoChart.getChartPanel().setPreferredSize(new Dimension(layDisplay.getWidth() / 2, displayFrame.getHeight() *2/6));
                //display.setBounds(0, 0, 
                //                  (int)(Ship.WIDTH/(1.5))-(int)((JScrollPane)display.getComponent(1)).getVerticalScrollBar().getPreferredSize().getWidth(), 
                //                  displayFrame.getHeight()/2-(int)((JScrollPane)display.getComponent(1)).getVerticalScrollBar().getPreferredSize().getWidth()*4);
            }
        });
        
        scroller = display.display;
        zoomScroller = displayZoom.display;
        //scroller.setVisible(false);
        
        displayFrame.pack();
        displayFrame.revalidate();
        
        //To center the scroll bars on the side and bottom
        Rectangle bounds = scroller.getViewport().getViewRect();
        Dimension size = scroller.getViewport().getViewSize();
        
        int x = (size.width - bounds.width) / 2;
        int y = (size.height - bounds.height) / 2;
        
        scroller.getViewport().setViewPosition(new Point(x,y));
        
        bounds = zoomScroller.getViewport().getViewRect();
        size = zoomScroller.getViewport().getViewSize();
        
        x = (size.width - bounds.width) / 6;
        y = (size.height - bounds.height) * 7/8;
        
        zoomScroller.getViewport().setViewPosition(new Point(x+5,y));//Zoom for Restaurant
        
        display.setScale(1);
        ((CruiseDisplay2D)zoomPane.getComponent(0)).setScale(2);
        
        display.repaint();
        
        //myTimeChart.getFrame().dispatchEvent(new WindowEvent(myTimeChart.getFrame(), WindowEvent.WINDOW_CLOSING)); NOT NEEDED ANYMORE
    }
    
    public void setStyleConstants(Color color)
    {
        StyleConstants.setUnderline(legendSet, false);
        StyleConstants.setForeground(legendSet, color);
        StyleConstants.setFontSize(legendSet, 14);
        StyleConstants.setFontFamily(legendSet, "Verdana");
        StyleConstants.setBold(legendSet, false);
        StyleConstants.setAlignment(legendSet, StyleConstants.ALIGN_LEFT);
    }
    
    public void createLegend(Color[] area, Color[] agent, StyledDocument doc)
    {
        String area_Legend = "AREA LEGEND\n";
        String room = "ROOM\n";
        String restaurant = "RESTAURANT\n";
        String casino = "CASINO\n";
        String bar = "BAR\n";
        String gym = "GYM\n";
        String theater = "THEATER\n";
        String shop = "SHOP\n";
        String lounge = "LOUNGE\n";
        String agent_Legend = "AGENT LEGEND\n";
        String pass_Sympt = "PASSENGER(symptomatic)\n";
        String pass_Asympt = "PASSENGER(asymptomatic)\n";
        String passengerDeceasedLabel = "PASSENGER(deceased)\n";
        String pass_Uninf = "PASSENGER(uninfected)\n";
        String crew_Sympt = "CREW(symptomatic)\n";
        String crew_Asympt = "CREW(asymptomatic)\n";
        String crew_Uninf = "CREW(uninfected)\n";
        String crewDeceasedLabel = "CREW(deceased)\n";
        String alert = "INFECTION ICON";
        
        String legend = area_Legend 
                      + room + restaurant + casino + bar + gym + theater + shop + lounge
                      + agent_Legend
                      + pass_Uninf + pass_Asympt + pass_Sympt + passengerDeceasedLabel
                      + crew_Uninf + crew_Asympt + crew_Sympt +  crewDeceasedLabel
                      + alert;
                       
        legendTxt.setText(legend);
        
        int posCounter = 0;
        
        setStyleConstants(area[7]);
        StyleConstants.setBold(legendSet, true);
        StyleConstants.setUnderline(legendSet, true);
        StyleConstants.setFontSize(legendSet, 14);
        doc.setParagraphAttributes(posCounter, posCounter+=(area_Legend.length()-1), legendSet, true);//AREA LEGEND
        
        setStyleConstants(area[0]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(room.length()-1), legendSet, true);//ROOM
        
        setStyleConstants(area[1]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(restaurant.length()-1), legendSet, true);//RESTAURANT
        
        setStyleConstants(area[2]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(casino.length()-1), legendSet, true);//CASINO
        
        setStyleConstants(area[3]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(bar.length()-1), legendSet, true);//BAR
        
        setStyleConstants(area[4]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(gym.length()-1), legendSet, true);//GYM
        
        setStyleConstants(area[5]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(theater.length()-1), legendSet, true);//THEATER
        
        setStyleConstants(area[6]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(shop.length()-1), legendSet, true);//SHOP
        
        setStyleConstants(area[7]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(lounge.length()-1), legendSet, true);//LOUNGE
        
        setStyleConstants(area[7]);
        StyleConstants.setBold(legendSet, true);
        StyleConstants.setUnderline(legendSet, true);
        StyleConstants.setFontSize(legendSet, 14);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(agent_Legend.length()-1), legendSet, true);//AGENT LEGEND
        
        setStyleConstants(agent[0]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(pass_Uninf.length()-1), legendSet, true);//PASSENGER(uninfected)

        setStyleConstants(agent[1]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(pass_Asympt.length()-1), legendSet, true);//PASSENGER(asymptomatic)
        
        setStyleConstants(agent[2]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(pass_Sympt.length()-1), legendSet, true);//PASSENGER(symptomatic)
        
        setStyleConstants(agent[3]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(passengerDeceasedLabel.length()-1), legendSet, true);//PASSENGER(deceased)
        
        setStyleConstants(agent[4]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(crew_Uninf.length()-1), legendSet, true);//CREW(uninfected)
        
        setStyleConstants(agent[5]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(crew_Asympt.length()-1), legendSet, true);//CREW(asymptomatic)
        
        setStyleConstants(agent[6]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(crew_Sympt.length()-1), legendSet, true);//CREW(symptomatic) 
        
        setStyleConstants(agent[7]);
        doc.setParagraphAttributes(posCounter+=1, posCounter+=(crewDeceasedLabel.length()-1), legendSet, true);//CREW(deceased) 
        
        setStyleConstants(area[7]);
        StyleConstants.setBold(legendSet, true);
        StyleConstants.setUnderline(legendSet, true);
        StyleConstants.setFontSize(legendSet, 14);
        doc.setParagraphAttributes(posCounter+=2, posCounter+=(alert.length()-1), legendSet, true);
    }

    //PURPOSE: to instantiate a new ShipUI and console to display the UI in
    //PARAMETERS: a potential string of arguments
    //RETURN: nothing
    public static void main(String[] args)
    {
        ShipUI worldGUI = null;

        try
        {
            worldGUI = new ShipUI();
        }
        catch (ParseException ex)
        {
            Logger.getLogger(ShipUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        Console console = new Console(worldGUI);
        console.setVisible(true);
    }

    public JFrame getProgFrame(){ return displayFrame; }
    
    public void switchRecord(){ recordLabel.setVisible(true); }
    
    public void updateTime(Ship world)
    {         
        double time = world.getTime();
        int wholeTime = (int)time;
        double fracTime = (time-wholeTime)*60;
        
        timeTxt = new JTextField(day + world.getDayNum() + ", " + hour + wholeTime + ":" + String.format("%02d", (int)fracTime) + ", " + eventTimes[(int)world.getTime()]);
        Font font = new Font("Verdana", Font.BOLD, 15);
        timeTxt.setFont(font);
        timeTxt.setHorizontalAlignment(JTextField.CENTER);
        timeTxt.setBorder(BorderFactory.createEmptyBorder());
        timeTxt.setEditable(false);
        displayFrame.getContentPane().remove(2);
        displayFrame.getContentPane().add(timeTxt, 2);
        
        displayFrame.getContentPane().revalidate();
    }
    
    public void updateAlert(Ship world)
    {   
        if(alertLabel.isShowing())
        {
            alertLabel.setBounds(display.getWidth()/2 - 40, display.getHeight()/2 - 40, 80, 80);
        }
        
        if(world.getNumInfected() > alertCounter)
        {
            alertLabel.setVisible(true);
            alertTime = world.schedule.getTime();
            alertCounter = world.getNumInfected();
        }
        
        if(alertTime != 0 && (world.schedule.getTime() - alertTime) > 25)
        {
            alertTime = 0;
            alertLabel.setVisible(false);
        }        
    }
}
