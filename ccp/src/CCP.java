import java.net.*;
import java.util.TimerTask;
import java.util.Timer;

import org.json.simple.*;

public class CCP {
    private static boolean mcpConnection;
    private static boolean espConnection;
    private static final long HEARTBEAT_INTERVAL = 2000; // 2 seconds
    private static Timer heartbeatTimer;

    public static void main(String[] args) {
         //CHANGE IF NEEDED 
        //Set up MCP and ESP32 connection 
        //boolean connected 
        final String MCP_IP_ADDRESS = "10.20.30.177";
        final String ESP_IP_ADDRESS = "10.20.30.1";
        final int MCP_PORT = 2000;
        final int ESP32_PORT = 3024;

        try {
            //Create new socket
            DatagramSocket socket = new DatagramSocket();
            
            // Instantiate send and receive objects
            Send send = new Send(socket);
            Receive receive = new Receive(socket);
            heartbeatTimer = new Timer();

            // Need to implement connection error checking for ESP and MCP
            // Need to implement heartbeat every 2 seconds to/from ESP and CCP
            heartbeatTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                    String heartbeatMessage = "HEARTBEAT";
                    send.sendMessage(heartbeatMessage, ESP_IP_ADDRESS, ESP32_PORT);

                    // String message = receive.akinCommandMCP();
                    // setMCPConnection(true);
                    // send.sendMessage(message, MCP_IP_ADDRESS, MCP_PORT);
                    } 
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, HEARTBEAT_INTERVAL); 

            // Thread.sleep(500);
            //Runs forever

            while (true) {
                // Call the run method from the receive object
                receive.run();
                //If a message has been received by the receive object
                if (receive.hasReceivedMessage()) {
                    //If the message has been sent by the MCP
                    if (receive.getClientType().equals("MCP")) {
                        //Message variable is what is going to be sent
                        String message = null;
                        //ipAddress and port are initially set to the ESP but may change depending on the command
                        String ipAddress = ESP_IP_ADDRESS;
                        int port = ESP32_PORT;
                        //If the command is AKIN then call the akin method and set the ipAddress and port to the MCP
                        if(receive.getAction().equals("AKIN")){
                            message = receive.akinCommandMCP();
                            setMCPConnection(true);
                            ipAddress = MCP_IP_ADDRESS;
                            port = MCP_PORT;
                        }
                        //If the command is STAT then call the status method and set the ipAddress and port to the MCP
                        else if(receive.getAction().equals("STAT")){
                            message = receive.statusCommandMCP();
                            ipAddress = MCP_IP_ADDRESS;
                            port = MCP_PORT;
                        }
                        //If the command is exec then call the exec method
                        else if(receive.getAction().equals("EXEC")){
                            message = receive.execCommandMCP();
                        }
                        //If the command is door close or open then call the doors method
                        else if(receive.getAction().equals("DOPN") || receive.getAction().equals("DCLS")){
                            message = receive.doorsCommandMCP();
                        }
                        //If the message isn't null then send it to the specified ip address and port, then set the receivedMessage variable to false in the receive object
                        if(message != null){
                            send.sendMessage(message, ipAddress, port);
                            receive.setReceivedMessage(false);
                        }
                    } 
                    //If the message has been sent by the ESP
                    else if (receive.getClientType().equals("ESP")) {
                        String message = null;
                        //If the command is ACKS then set the ESP connection flag to true
                        if (receive.getAction().equals("HEARTBEAT_ACK")) {
                            setESPConnection(true);
                            System.out.println("Received heartbeat acknowledgment from ESP32");
                        }
                        else if(receive.getAction().equals("ACKS")){
                            setESPConnection(true);
                        }
                        //If the command is STAN then call the station method
                        else if(receive.getAction().equals("STAN")){
                            message = receive.stationCommandESP();
                        }
                        //If the action is STAT then call the status method
                        else if(receive.getAction().equals("STAT")){
                            receive.statusCommandESP();
                        }
                        //If the message isn't null then send it to the MCP and set the received message flag to false
                        if(message != null){
                            send.sendMessage(message, MCP_IP_ADDRESS, MCP_PORT);
                            receive.setReceivedMessage(false);
                        } 
                    }
                }
                
            }
        } 
        //In case there are any errors print out the stack trace
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Getters and setters for the private variables
    public static void setMCPConnection(boolean state){
        mcpConnection = state;
    }

    public static void setESPConnection(boolean state){
        espConnection = state;
    }
}