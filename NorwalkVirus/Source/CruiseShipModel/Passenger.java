package CruiseShipModel;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Passenger extends Person
{
    public Passenger(Ship state, String personType) 
    {
        super(state, personType);
        
        this.getGeometry().addIntegerAttribute("AGENT_TYPE", 0);//0 equals passenger
        randomness = state.random.nextDouble()*4 - 2;
                
        for(int i = 0; i < 24; i++) //Behavior patterns for agents
        {
            switch(i)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    behavior[i] = "Sleep";
                    break;
                case 9:
                case 10:
                    behavior[i] = "Meal:Breakfast";
                    break;
                case 11:
                    behavior[i] = "Free";
                    break;
                case 12:
                case 13:
                    behavior[i] = "Meal:Lunch";
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                    behavior[i] = "Free";
                    break;
                case 18:
                case 19:
                    behavior[i] = "Meal:Dinner";
                    break;
                case 20:
                case 21:
                case 22:
                case 23:
                    behavior[i] = "Free";
                    break;
                default:
                    behavior[i] = "Free";//just in case
            }
        }
        
        resetVisited();
        resetAt();
    }   
    
    @Override
    protected String getAgentType(){ return "Passenger"; }
}
