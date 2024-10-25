import java.util.*;

public class TimerThread extends Thread{
    //Timer objects
    public Timer espSendSTAT;
    public Timer espConnectionCheck;
    public Timer mcpConnectionCheck;

    //Constructor
    public TimerThread(){
        espSendSTAT = new Timer();
        espConnectionCheck = new Timer();
        mcpConnectionCheck = new Timer();
    }

    //Runs when .start() is used
    @Override
    public void run(){
        setupTimers();
    }

    //Setup all 3 timers
    public void setupTimers(){
        setupESPStat();
        setupESPConnectionCheck();
        setupMCPConnectionCheck();
    }

    //Setup ESP stat timer
    public void setupESPStat(){
        //Sends STATRQ message to ESP every 0.5 seconds
        espSendSTAT.schedule(new TimerTask() {
            @Override
            public void run() {
                //Send a STAT message to the ESP
                CCP.espSender.send_esp_strq();
            }
        }, 0, 500); 
    }

    //Check ESP Connection
    public void setupESPConnectionCheck(){
        //Runs every 1 second to check if the ESP is connected
        espConnectionCheck.schedule(new TimerTask() {
            @Override
            public void run() {
                //If there is no connection with the ESP, then print it out
                if(!CCP.espConnection){
                    System.out.println("No connection with ESP");
                }
                //Set the flag variable to false if there is a connection with the ESP
                else{
                    CCP.espConnection = false;
                }
            }
        }, 0, 2000);
    }

    //Check MCP Connection
    public void setupMCPConnectionCheck(){
        //Runs every 1 second to check if the MCP is connected
        mcpConnectionCheck.schedule(new TimerTask() {
            @Override
            public void run() {
                //If there is no connection with the MCP, then print it out
                if(!CCP.mcpConnection){
                    System.out.println("No connection with MCP");
                }
                //Set the flag variable to false if there is a connection with the MCP
                else{
                    CCP.mcpConnection = false;
                }
            }
        }, 0, 2000);
    }
}
