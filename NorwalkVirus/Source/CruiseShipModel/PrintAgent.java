/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CruiseShipModel;

import static CruiseShipModel.Person.*;
import static CruiseShipModel.Ship.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import sim.engine.SimState;

public class PrintAgent extends Agent {

    private static final long serialVersionUID = 3705661848016825633L;

    String totalFile = "Norwalk_Virus-All_Members-";
    String passengerFile = "Norwalk_Virus-Passengers-";
    String crewFile = "Norwalk_Virus-Crew-";

    String notContained = "No_Containment.csv";
    String dineRestricted = "Dining_Restricted.csv";
    String dineClosed = "Dining_Closed.csv";
    String selfIsolated = "Self_Isolation.csv";
    String vspIsolated = "VSP_Isolation.csv";


    static File csvFileTotals;
    static BufferedWriter csvWriterTotals;

    static File csvFilePassengers;
    static BufferedWriter csvWriterPassengers;

    static File csvFileCrew;
    static BufferedWriter csvWriterCrew;

    private int printInterval = 900; 		// 15 minutes in steps.

//    static File txtFile = new File("CruiseLog.txt");
//    static BufferedWriter txtWriter;

    //CONSTRUCTOR
    public PrintAgent(Ship state)
    {
        super(state);

        if(diningRestricted) {
            totalFile = totalFile + dineRestricted;
            passengerFile = passengerFile + dineRestricted;
            crewFile = crewFile + dineRestricted;
        }
        else if(diningClosed) {
            totalFile = totalFile + dineClosed;
            passengerFile = passengerFile + dineClosed;
            crewFile = crewFile + dineClosed;
        }
        else if(selfIsolation) {
            totalFile = totalFile + selfIsolated;
            passengerFile = passengerFile + selfIsolated;
            crewFile = crewFile + selfIsolated;
        }
        else if(vspIsolation) {
            totalFile = totalFile + vspIsolated;
            passengerFile = passengerFile + vspIsolated;
            crewFile = crewFile + vspIsolated;
        }
        else {
            totalFile = totalFile + notContained;
            passengerFile = passengerFile + notContained;
            crewFile = crewFile + notContained;
        }

        try {
            Date date = new Date() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss") ;
            String dirName = "Log_" + dateFormat.format(date);
            File dir = new File(dirName);
            dir.mkdir();

            csvFileTotals = new File(dirName + File.separator + totalFile);
            csvFilePassengers = new File(dirName + File.separator +passengerFile);
            csvFileCrew = new File(dirName + File.separator +crewFile);

            csvWriterTotals = new BufferedWriter(new FileWriter(csvFileTotals));
            csvWriterTotals.write("DAY,TIME,TOTAL_IMMUNE,TOTAL_UNINFECTED,TOTAL_INFECTED,TOTAL_SYMPTOMATIC,TOTAL_ASYMPTOMATIC,TOTAL_DEAD,TOTAL_RECOVERED");
            csvWriterTotals.newLine();
            csvWriterTotals.flush();

            csvWriterPassengers = new BufferedWriter(new FileWriter(csvFilePassengers));
            csvWriterPassengers.write("DAY,TIME,PASSENGERS_IMMUNE,PASSENGERS_UNINFECTED,PASSENGERS_INFECTED,PASSENGERS_SYMPTOMATIC,PASSENGERS_ASYMPTOMATIC,PASSENGERS_DEAD,PASSENGERS_RECOVERED");
            csvWriterPassengers.newLine();
            csvWriterPassengers.flush();

            csvWriterCrew = new BufferedWriter(new FileWriter(csvFileCrew));
            csvWriterCrew.write("DAY,TIME,CREW_IMMUNE,CREW_UNINFECTED,CREW_INFECTED,CREW_SYMPTOMATIC,CREW_ASYMPTOMATIC,CREW_DEAD,CREW_RECOVERED");
            csvWriterCrew.newLine();
            csvWriterCrew.flush();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void step(SimState state)
    {
        // print info to console
        if((int) state.schedule.getTime() % printInterval == 0)
        {
//           System.out.println( "At step - " + state.schedule.getTime() );
//           System.out.println( "Total infections: " + totalInfections );
//           System.out.println( "Total ill: " + totalIll );
//           System.out.println( "Total infected passengers: " + totalPassengerInfections);
//           System.out.println( "Total ill passengers: " + totalPassengerIll);
//           System.out.println( "Total infected crew: " + totalCrewInfections);
//           System.out.println( "Total ill crew: " + totalCrewIll);

            int day = ((int)state.schedule.getTime() / dayInSteps) + 1;
            double time = (((state.schedule.getTime()) / 3600) + 6) % 24;
            int wholeTime = (int)time;
            double fracTime = (time-wholeTime) * 60;
            try {
//        	   txtWriter.write("At step - " + state.schedule.getTime() + ":\n");
//        	   txtWriter.write("Total infected passengers: " + totalPassengersInfected + "\n");
//        	   txtWriter.write("Total ill passengers: " + totalPassengersIll + "\n");
//        	   txtWriter.write("Total dead passengers: " + totalPassengersDead + "\n");
//        	   txtWriter.write("Total recovered passengers: " + totalPassengersRecovered + "\n");
//        	   txtWriter.write("Total infected crew: " + totalCrewInfected + "\n");
//        	   txtWriter.write("Total ill crew: " + totalCrewIll + "\n");
//        	   txtWriter.write("Total dead crew: " + totalCrewDead + "\n");
//        	   txtWriter.write("Total recovered crew: " + totalCrewRecovered + "\n");
//        	   txtWriter.write("-------------------------------------------\n");
//        	   txtWriter.flush();

                csvWriterTotals.write(day + "," + (wholeTime + ":" + String.format("%02d", (int)fracTime)) + ","
                        + totalImmune + "," + (totalOnboard - totalInfected) + "," + totalInfected + "," + totalIll + "," + (totalInfected-totalIll) + ","
                        + totalDead + "," + totalRecovered);
                csvWriterTotals.newLine();
                csvWriterTotals.flush();

                csvWriterPassengers.write(day + "," + (wholeTime + ":" + String.format("%02d", (int)fracTime)) + ","
                        + totalPassengersImmune + "," + (totalPassengers - totalPassengersInfected) + "," + totalPassengersInfected + ","
                        + totalPassengersIll + "," + (totalPassengersInfected - totalPassengersIll) + "," + totalPassengersDead + "," + totalPassengersRecovered);
                csvWriterPassengers.newLine();
                csvWriterPassengers.flush();

                csvWriterCrew.write(day + "," + (wholeTime + ":" + String.format("%02d", (int)fracTime)) + ","
                        + totalCrewImmune + "," + (totalCrew - totalCrewInfected) + "," + totalCrewInfected + "," + totalCrewIll + ","
                        + (totalCrewInfected - totalCrewIll) + "," + totalCrewDead + "," + totalCrewRecovered);
                csvWriterCrew.newLine();
                csvWriterCrew.flush();

                if(day == simEndDay) {		// Terminate the simulation
                    System.exit(0);
                }

            } catch (IOException ex) {
                System.out.println("HELP");
                Logger.getLogger(Person.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
