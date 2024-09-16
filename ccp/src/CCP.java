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
        //Set up MCP and ESP32 connection 
        final String MCP_IP_ADDRESS = "192.168.0.89";
        final String ESP_IP_ADDRESS = "10.20.30.1";
        final int MCP_PORT = 2000;
        final int ESP32_PORT = 3024;

        try {
            //Create new socket
            DatagramSocket mcpSocket = new DatagramSocket(MCP_PORT);
            DatagramSocket espSocket = new DatagramSocket(ESP32_PORT);
            // Instantiate send and receive objects
            ReceiveMCP mcpReceive = new ReceiveMCP(mcpSocket);
            ReceiveESP espReceive = new ReceiveESP(espSocket);
            Send send = new Send(mcpSocket);
            heartbeatTimer = new Timer();

            // Need to implement connection error checking for ESP and MCP
            // Need to implement heartbeat every 2 seconds to/from ESP and CCP
            heartbeatTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                    // String heartbeatMessage = "HEARTBEAT";
                    // send.sendMessage(heartbeatMessage, ESP_IP_ADDRESS, ESP32_PORT);

                    String message = send.send_mcp_ccin();
                    setMCPConnection(true);
                    send.sendMessage(message, MCP_IP_ADDRESS, MCP_PORT);
                    } 
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, HEARTBEAT_INTERVAL); 

            while (true) {
                // Call the run method from the receive object
                mcpReceive.run();
                //If a message has been received by the receive object
                if (mcpReceive.hasReceivedMessage()) {
                    //If the message has been sent by the MCP
                    if (mcpReceive.getIntendedClientID().equals(Receive.client_id) && mcpReceive.getIntendedClientType().equals(Receive.client_type)) {
                        //Message variable is what is going to be sent
                        String message = null;
                        String ip_address = ESP_IP_ADDRESS;
                        int port = ESP32_PORT;
                        //If the command is AKIN then call the akin method and set the ipAddress and port to the MCP
                        if(mcpReceive.getMessage() != null){
                            if(mcpReceive.getMessage().equals("AKIN")){
                                setMCPConnection(true);
                            }
                            //If the command is STAT then call the status method and set the ipAddress and port to the MCP
                            else if(mcpReceive.getMessage().equals("STAT")){
                                setMCPConnection(true);
                                send.send_mcp_stat(espReceive.getStatus());
                                ip_address = MCP_IP_ADDRESS;
                                port = MCP_PORT;
                            }
                            //If the command is exec then call the exec method
                            else if(mcpReceive.getMessage().equals("EXEC")){
                                if(mcpReceive.getAction().equals("STOP")){
                                    espReceive.setLightColour("RED");
                                }

                                else if(mcpReceive.getAction().equals("SLOW")){
                                    espReceive.setLightColour("YELLOW");
                                }

                                else if(mcpReceive.getAction().equals("FAST")){
                                    espReceive.setLightColour("GREEN");
                                }
                                message = send.send_esp_exec(espReceive.getLightColour(), mcpReceive.getAction());
                            }
                            //If the command is door close or open then call the doors method
                            else if(mcpReceive.getMessage().equals("DOPN")){
                                message = send.send_esp_door("OPEN");
                            }

                            else if(mcpReceive.getMessage().equals("DCLS")){
                                message = send.send_esp_door("CLOSE");
                            }
                        }
                        //If the message isn't null then send it to the specified ip address and port, then set the receivedMessage variable to false in the receive object
                        if(message != null){
                            send.sendMessage(message, ip_address, port);
                        }
                    }
                }
                
                espReceive.run();

                if(espReceive.hasReceivedMessage()){
                    if (espReceive.getIntendedClientID().equals(Receive.client_id) && espReceive.getIntendedClientType().equals(Receive.client_type)) {
                        String message = null;
                        String ip_address = MCP_IP_ADDRESS;
                        int port = MCP_PORT;
                        
                        if(espReceive.getMessage() != null){
                            if(espReceive.getMessage().equals("ACKS")){
                                setESPConnection(true);
                            }

                            if(espReceive.getMessage().equals("STAT")){
                                setESPConnection(true);
                            }

                            if(espReceive.getMessage().equals("STAN")){
                                message = send.send_mcp_stan(espReceive.getStatus(), espReceive.getStationID());
                            }
                        }

                        if(message != null){
                            send.sendMessage(message, ip_address, port);
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