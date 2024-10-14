package CruiseShipModel;

public class ParameterList 
{
    
    private int numPassengers;  // the number of passengers in the simulation to be run
    private int numStrucCrew;   // the number of "structured crew" in the simulation to be run
    private int numUnstrucCrew; // the number of "unstructured crew" in the simulation to be run
    
    private double inc_per;     // the average incubation period to be used
                                // (incubation period is the duration between infection and experience of symptoms)
    
    private double lat_per;     // the average latency period to be used
                                // (latency period is the duration between infection and ability to infect others (virus shedding))
    
    private double non_sec;     // decimal representation % of non-secretors in the simulation to be run, 0-1
                                // (non-secretors have high level of genetic-based immunity)
    
    private boolean aggreg;     // if true, the simulation to be run will utilize data from 8fIIa innoculum (aggregated), 
                                // if false, 8fIIb (disaggregated)
    
    private boolean pcr;        // if true, the simulation to be run will utilize data from PCR-recorded shedding values, 
                                // if false, ELISA antigen based
    
    private int doseAdjustment; // shedding value data is based on viral copies/gram, 
                                // it is unlikely anyone would come into direct contact 
                                // with such a large amount of contaminated material.  
                                // This value is the number of log10s that each dose encountered 
                                // will be lowered.  Example:  if this value is 3, it will be 
                                // assumed that the typical dose encountered will be 10^-3 g, or 1 mg.
    
    private double quar_dur;       // the duration (in days) of quarantine for symptomatic agents
    
    private double quar_comp_pass;  // this represents the probability of compliance with quarantine among passengers (0-1)
    
    private double quar_comp_crew;  // this represents the probability of compliance with quarantine among crew (0-1)
    
    
    public ParameterList()
    {
        
    }
    
    
    
    public boolean allParamsEntered()
    {
        return this.numPassengers >= 0 && this.numStrucCrew >= 0 && this.numUnstrucCrew >= 0;
    }
    
    public int getNumPassengers()
    {
        return this.numPassengers;   
    }
    
    public int getNumStrucCrew()
    {
        return this.numStrucCrew;
    }
    
    public int getNumUnStrucCrew()
    {
        return this.numUnstrucCrew;
    }
    
    public double getInc_Per()
    {
        return this.inc_per;
    }
    
    public double getLat_Per()
    {
        return this.lat_per;
    }
    
    public double getNon_Sec()
    {
        return this.non_sec;
    }
    
    public int getDoseAdjustment()
    {
        return this.doseAdjustment;
    }
    
    public boolean getAggreg()
    {
        return this.aggreg;
    }
    
    public boolean getPCR()
    {
        return this.pcr;
    }
    
    public double getQuar_Dur()
    {
        return this.quar_dur;
    }
    
    public double getQuar_Comp_Pass()
    {
        return this.quar_comp_pass;
    }
    
    public double getQuar_Comp_Crew()
    {
        return this.quar_comp_crew;
    }
}
