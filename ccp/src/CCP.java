import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class CCP {
    //Variables to be used for connection status checking
    private static boolean espConnection = false;
    private static boolean mcpConnection = false;
    //Variables for sending a message at set intervals. Hearbeat Variable is 2 seconds

    //10.20.30.11
    public static final String MCP_IP_ADDRESS = "10.20.30.1";
    public static final String ESP_IP_ADDRESS = "10.20.30.124";
    public static final int MCP_PORT = 2000;
    public static final int ESP32_PORT = 3024;
    public static int mcpSequence = new Random().nextInt(1000, 30000);
    public static int espSequence = new Random().nextInt(1000, 30000);

    public static void main(String[] args) {
        try {
            //Create new sockets for ESP and MCP on their respective ports
            DatagramSocket mcpSocket = new DatagramSocket(MCP_PORT);
            DatagramSocket espSocket = new DatagramSocket(ESP32_PORT);

            ThreadESP espThread = new ThreadESP(espSocket);
            ThreadMCP mcpThread = new ThreadMCP(mcpSocket);
            TimerThread mainTimer = new TimerThread();
            
            SendToMCP mcpSender = new SendToMCP(mcpSocket);
            SendToMCP espSender = new SendToMCP(espSocket);

            Thread mcpThreadObject = new Thread(mcpThread);
            Thread espThreadObject = new Thread(espThread);
            Thread timerThreadObject = new Thread(mainTimer);

            espThreadObject.start();
            mcpThreadObject.start();
            intialiseConnections();
            timerThreadObject.start();
        }

        //In case there are any errors that haven't been addressed, print out the stack trace
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void intialiseConnections(){
        //check if CCIN received from esp
        //send AKIN
        //check if akin/ccin
        //set espStatAck to true
        // System.out.println("Checking for init");
        // while(!getESP_ACK()){
        //     System.out.println("Checking for recieved");
        //     if(espReceive.getMessage() != null){
        //         System.out.println("here");
        //         if(espReceive.getMessage().equals( "CCIN")){
        //             //Send CCIN to ESP 3 times before waiting for AKIN again
        //             for(int i = 0; i <= 3; i++){
        //                 sendESP.sendMessage(sendESP.send_esp_akin(), ESP_IP_ADDRESS, ESP32_PORT);
        //                 espReceive.run();
        //                 if(espReceive.getMessage().equals("AKIN/ACK")){
        //                     setESP_ACK(true);
        //                     break;
        //                 }
        //             }
        //         }
        //     }
        // }

        // //Send CCIN to MCP
        // //check if ack received
        // //set mcpStatAck to true
        // while(!getMCP_ACK()){
            
        //     sendMCP.sendMessage(sendMCP.send_mcp_ccin(), ESP_IP_ADDRESS, ESP32_PORT);
        //     mcpReceive.run();
        //     if(mcpReceive.getMessage().equals("AKIN")){
        //         setMCP_ACK(true);
        //     }
        // } 
    }

    public static void mcpMessageLogic(ThreadMCP mcpThreadObject){
        if(mcpThreadObject.hasReceivedMessage){
            if(mcpThreadObject.getValueFromMessage("message") == null){
                //Send noip
            }
            else if(mcpThreadObject.getValueFromMessage("message").equals("AKIN")){

            }
            
            else if(mcpThreadObject.getValueFromMessage("message").equals("AKST")){

            }

            else if(mcpThreadObject.getValueFromMessage("message").equals("STRQ")){
                
            }

            else if(mcpThreadObject.getValueFromMessage("message").equals("EXEC")){
                if(mcpThreadObject.getValueFromMessage("action") == null){
                    //Send noip
                }

                else if(mcpThreadObject.getValueFromMessage("action").equals("STOPC")){

                }

                else if(mcpThreadObject.getValueFromMessage("action").equals("STOPO")){

                }

                else if(mcpThreadObject.getValueFromMessage("action").equals("FSLOWC")){

                }

                else if(mcpThreadObject.getValueFromMessage("action").equals("FFASTC")){

                }

                else if(mcpThreadObject.getValueFromMessage("action").equals("RSLOWC")){

                }

                else if(mcpThreadObject.getValueFromMessage("action").equals("DISCONNECT")){

                }

                else{
                    //noip
                }
            }

            else{
                //Send noip
            }

        }
    }

    public static void espMessageLogic(ThreadESP espThreadObject){
        if(espThreadObject.hasReceivedMessage){
            if(espThreadObject.getValueFromMessage("message") == null || espThreadObject.getValueFromMessage("sequence") == null){
                return;
            }

            else if(espThreadObject.getValueFromMessage("sequence").equals("yomama")){

            }

            else if(espThreadObject.getValueFromMessage("message").equals("CCIN")){

            }
            
            else if(espThreadObject.getValueFromMessage("message").equals("AKIN/ACK")){

            }

            else if(espThreadObject.getValueFromMessage("message").equals("STAT")){

            }
        }
    }
}