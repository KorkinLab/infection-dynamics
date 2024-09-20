# infection-dynamics
Contains source code, GIS files and libraries for the Infection Dynamics (Cruise Ship) Project.

## Project Setup
### 1. Java Development Kit
This is a Java project and requires a Java development kit (JDK).   
The project was developed using JDK 8 and 11, therefore JDK 11 is recommended for reproducibility.  
JDK 11 for your operating system and architecture can be downloaded from [Azul](https://www.azul.com/downloads/?version=java-11-lts&package=jdk#zulu).  
**NOTE**: If you have an Apple Silicon Mac, choose the `ARM 64-bit` architecture (more on this below).

### 2. Integrated Development Environment
The project needs to be configured through an integrated development environment (IDE) for development and simulation.  
The project was developed using IntelliJ IDEA and the Community Edition (free) for your OS and architecture can be downloaded from [here](https://www.jetbrains.com/idea/download/).  

### 3. IDE Project Configuration
Create an `IdeaProjects` directory in your Home directory.  
Download the GitHub repository from [here](https://github.com/KorkinLab/infection-dynamics/archive/refs/heads/main.zip).  
Unzip the repository and place the `NorwalkVirus` and `NorwalkSim` directories in `IdeaProjects`.  
In a terminal navigate to the `NorwalkSim` directory and rename the `idea` directory.
```
mv idea .idea
```
Launch IntelliJ and on first launch the Azul JDK 11 should be automatically detected.  
Then select the "Open" option, from which select the `~/IdeaProjects/NorwalkSim`. For the security prompt, you can select to trust `~/IdeaProjects`.  
The project should now be open and in the left-side project panel, right-click on `NorwalkSim` and select "Open Module Settings".  
As shown below, in the "Dependencies" tab, for Module SDK, select "zulu-11".  
![ModuleDependencySDK](https://github.com/user-attachments/assets/514b891d-3b2e-40f5-a79b-64c3d3103601)
