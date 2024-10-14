package CruiseShipModel;

public class UnstrucCrew extends Person
{
    public UnstrucCrew(Ship state, String personType)
    {
        super(state, personType);
        
        randomness = state.random.nextDouble()*2 - 1;
        
        int workOrSleep = state.random.nextInt(state.numUnstrucCrew);
        
        this.getGeometry().addIntegerAttribute("AGENT_TYPE", 4);//4 equals crew
        
        for(int i = 0; i < 24; i++) //Behavior patterns for agents
        {
            switch(i)
            {
                case 0:
                    if(workOrSleep <= state.numUnstrucCrew/20)
                        behavior[i] = "Sleep";
                    else
                        behavior[i] = "Work";
                    break;
                case 1:
                    if(workOrSleep <= state.numUnstrucCrew/20)
                    {
                        behavior[i] = "Sleep";
                        break;
                    }
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    if(workOrSleep <= state.numUnstrucCrew/20)
                        behavior[i] = "Work";
                    else
                        behavior[i] = "Sleep";
                    break;
                case 8:  
                    behavior[i] = "Meal:Breakfast";
                    break;
                case 9:
                case 10:      
                case 11:
                case 12:
                    behavior[i] = "Work";
                    break;
                case 13:
                    behavior[i] = "Meal:Lunch";
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                    behavior[i] = "Work";
                    break;
                case 18:
                case 19:
                    behavior[i] = "Meal:Dinner";
                    break;
                case 20:
                case 21:
                case 22:
                case 23:
                    if(workOrSleep <= state.numUnstrucCrew/20)
                        behavior[i] = "Sleep";
                    else        
                        behavior[i] = "Work";
                    break;
                default:
                    behavior[i] = "Work";
            }
        }
    }  
    
    @Override
    protected String getAgentType() { return "Crew: Unstructured"; }
}
