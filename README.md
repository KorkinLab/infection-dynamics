# infection-dynamics
Contains source code, GIS files and libraries for the Infection Dynamics (Cruise Ship) Project.

## Project Setup
### 1. Java Development Kit
This is a Java project and requires a Java development kit (JDK).   
The project was developed using JDK 8 and 11, therefore JDK 11 is recommended for reproducibility.  
JDK 11 for your operating system and architecture can be downloaded from [Azul](https://www.azul.com/downloads/?version=java-11-lts&package=jdk#zulu).  
**NOTE**: If you have an Apple Silicon Mac, choose the `ARM 64-bit` architecture (more notes for Mac at the end).

### 2. Integrated Development Environment
The project needs to be configured through an integrated development environment (IDE) for development and simulation.  
The project was developed using IntelliJ IDEA and the Community Edition (free) for your OS and architecture can be downloaded from [here](https://www.jetbrains.com/idea/download/).  

### 3. IDE Project Configuration 
Download the GitHub repository from [here](https://github.com/KorkinLab/infection-dynamics/archive/refs/heads/main.zip).  
Then use the below commands from your Downloads directory to setup the project directories.
```
$ mkdir ~/IdeaProjects
$ unzip infection-dynamics-main.zip
$ mv infection-dynamics-main/Norwalk* ~/IdeaProjects
$ mv ~/IdeaProjects/NorwalkSim/idea ~/IdeaProjects/NorwalkSim/.idea
```
Launch IntelliJ and on first launch the Azul JDK 11 should be automatically detected.  
Then select the "Open" option, from which select the `~/IdeaProjects/NorwalkSim`.  
For the security prompt, you can select to trust `~/IdeaProjects`.  
The project should now be open and in the left-side project panel, right-click on `NorwalkSim` and select "Open Module Settings".  
As shown below, in the "Dependencies" tab, for Module SDK, select "zulu-11".  
<img src="https://github.com/user-attachments/assets/3b7ca212-45ae-419e-95fb-c4894bf0f09a" width="768">  
Then click "Apply" and "OK".  

### 4. Running the Simulation
Back in the project panel, right-click on `NorwalkSim` and select "Rebuild Module".  
After which, in the project panel, expand the `NorwalkSim/Source` directory, right-click on `ShipUI` and select "Run".  
There will be two small windows that popup. As shown below, select the square "ShipUI" window with playback controls and click "Run".  
<img src="https://github.com/user-attachments/assets/41763949-05da-4b69-85cc-ffd61334d7f4" width="256">  
This will then expand the other window and the simulation begins to run.  
This window is mainly for visualizing the agents and the map, and can be minimized.  
**NOTE**: Minimizing the visualization window speeds up the simulation drastically and is STRONGLY recommended.  
The simulation automatically ends after the set amount of time has passed or can be stopped by closing the window or using the "Stop" button.  
The simulation statistics are saved to file in `~/IdeaProjects/NorwalkSim`.  
Each simulation generates a logs directory in the format `Log_YYYY-MM-DD_HH.MM.SS`.  
Multiple simulations from the same project can be run simultaneously by right-clicking on `ShipUI` and select "Run", and following the earlier steps.

### Regarding running simulations on Mac
There is instability in the visualization window when running the simulation on macOS (this has been a long standing issue).  
This can end up crashing the simulation if there is any user interaction with the viz window.  
Therefore it is recommended to run the simulation on a Windows or Linux.  
Additionally, as observed on Apple M1, when running multiple simulations, the run times can vary by a wide margin, e.g., 4 to 8 hours to complete Norwalk outbreak.  
This is likely due to how macOS schedules these minimized windows and processes on performance versus efficiency cores.  
This variation does not occur on Windows.  
