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
        }, 0, 1000); 
    }
}
