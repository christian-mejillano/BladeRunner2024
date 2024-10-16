import java.net.*;
import java.util.*;

public class TimerThread implements Runnable{
    private static final long HEARTBEAT_INTERVAL = 2000;
    private static Timer mainTimer;

    public TimerThread(){

    }


    @Override
    public void run(){
        // setupTimers();
    }

    // public static void setupTimers(){
    //     //Runs every 2 seconds given the HEARTBEAT_INTERVAL variable
    //     //Sends STAT message to ESP and CCP every 2 seconds
    //     mainTimer.schedule(new TimerTask() {
    //         @Override
    //         public void run() {
    //             //Send a STAT message to the MCP
    //             String message = sendMCP.send_mcp_ccin();
    //             sendMCP.sendMessage(message, CCP.MCP_IP_ADDRESS, CCP.MCP_PORT);
    //             System.out.println("Sending MCP: " + message);
    //             //Send a STAT message to the ESP
    //             message = sendESP.send_esp_strq();
    //             sendESP.sendMessage(message, CCP.ESP_IP_ADDRESS, CCP.ESP32_PORT);
    //             System.out.println("Sending ESP: " + message);
    //         }
    //     }, 0, HEARTBEAT_INTERVAL); 

    //     //Runs every 5 seconds to check if the connection with ESP and MCP is maintained
    //     mainTimer.schedule(new TimerTask() {
    //         @Override
    //         public void run() {
    //             //If there is no connection with the ESP, then print it out
    //             // if(!CCP.getESP_ACK() && !CCP.getMCP_ACK()){
    //             //     CCP.intialiseConnections(sendMCP, sendESP, mcpReceive, espReceive);
    //             //     return;
    //             // }

    //             if(!CCP.getESP_ACK()){
    //                 System.out.println("Lost connection with ESP");
    //             }
    //             //Set the flag variable to false if there is a connection with the ESP
    //             else{
    //                 CCP.setESP_ACK(false);
    //             }
    //             //If there is no connection with the ESP, then print it out
    //             if(!CCP.getMCP_ACK()){
    //                 System.out.println("Lost connection with MCP");
    //                 //Could put LIMP Mode here where an EXEC message is sent to the ESP with the action being SLOW
    //             }
    //             //Set the flag variable to false if there is a connection with the ESP
    //             else{
    //                 CCP.setMCP_ACK(false);
    //             }
    //         }
    //     }, 10000, 4999);
}
