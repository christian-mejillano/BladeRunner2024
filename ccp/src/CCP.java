import java.net.*;
import java.util.*;

public class CCP {
    //Variables to be used for connection status checking
    private static boolean espStatAck = false;
    private static boolean mcpStatAck = false;
    //Variables for sending a message at set intervals. Hearbeat Variable is 2 seconds
    private static final long HEARTBEAT_INTERVAL = 2000;
    private static Timer heartbeatTimer;
    //10.20.30.11
    private static final String MCP_IP_ADDRESS = "10.20.30.1";
    private static final String ESP_IP_ADDRESS = "10.20.30.124";
    private static final int MCP_PORT = 2000;
    private static final int ESP32_PORT = 3024;
    public static int MCP_Sequence = new Random().nextInt(1000, 30000);
    public static int ESP_Sequence = new Random().nextInt(1000, 30000);
    private static int MCP_Receive_Sequence;

    public static void main(String[] args) {
        try {
            //Create new sockets for ESP and MCP on their respective ports
            DatagramSocket mcpSocket = new DatagramSocket(MCP_PORT);
            DatagramSocket espSocket = new DatagramSocket(ESP32_PORT);

            //Instantiate Receive Objects for MCP and ESP
            ReceiveMCP mcpReceive = new ReceiveMCP(mcpSocket);
            ReceiveESP espReceive = new ReceiveESP(espSocket);
            //Instanstiate Send Object and new Timer 
            Send sendMCP = new Send(mcpSocket);
            Send sendESP = new Send(espSocket);
            heartbeatTimer = new Timer();         
            
            intialiseConnections(sendMCP, sendESP, mcpReceive, espReceive);

            // Call the the function to setup all the timers
            setupTimers(sendMCP, sendESP, mcpReceive, espReceive);

            while (true) {
                //Call the two functions that will run the send and receive logic for ESP and MCP communication
                mcpMessageLogic(sendMCP, sendESP, mcpReceive, espReceive);
                espMessageLogic(sendMCP, sendESP, mcpReceive, espReceive);
            }

            //Basic send test messages to ESP

            // String message = sendESP.send_esp_exec("Start");
            // sendESP.sendMessage(message, ESP_IP_ADDRESS, ESP32_PORT);

            // Basic Send Testing

            // Thread.sleep(5000);

            // message = sendESP.send_esp_exec("Stop");
            // sendESP.sendMessage(message, ESP_IP_ADDRESS, ESP32_PORT);

            // Thread.sleep(5000);

            // message = sendESP.send_esp_exec("Back");
            // sendESP.sendMessage(message, ESP_IP_ADDRESS, ESP32_PORT);

            // Thread.sleep(5000);

            // message = sendESP.send_esp_exec("Stop");
            // sendESP.sendMessage(message, ESP_IP_ADDRESS, ESP32_PORT);
        } 

        //In case there are any errors that haven't been addressed, print out the stack trace
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void intialiseConnections(Send sendMCP, Send sendESP, ReceiveMCP mcpReceive, ReceiveESP espReceive){
        //check if CCIN received from esp
        //send AKIN
        //check if akin/ccin
        //set espStatAck to true
        System.out.println("Checking for init");
        while(!getESP_ACK()){
            System.out.println("Checking for recieved");
            espReceive.run();
            if(espReceive.getMessage() != null){
                System.out.println("here");
                if(espReceive.getMessage().equals( "CCIN")){
                    //Send CCIN to ESP 3 times before waiting for AKIN again
                    for(int i = 0; i <= 3; i++){
                        sendESP.sendMessage(sendESP.send_esp_akin(), ESP_IP_ADDRESS, ESP32_PORT);
                        espReceive.run();
                        if(espReceive.getMessage().equals("AKIN/ACK")){
                            setESP_ACK(true);
                            break;
                        }
                    }
                }
            }
        }

        //Send CCIN to MCP
        //check if ack received
        //set mcpStatAck to true
        while(!getMCP_ACK()){
            
            sendMCP.sendMessage(sendMCP.send_mcp_ccin(), ESP_IP_ADDRESS, ESP32_PORT);
            mcpReceive.run();
            if(mcpReceive.getMessage().equals("AKIN")){
                setMCP_ACK(true);
            }
        } 
    }

    public static void setupTimers(Send sendMCP, Send sendESP, ReceiveMCP mcpReceive, ReceiveESP espReceive){
            //Runs every 2 seconds given the HEARTBEAT_INTERVAL variable
            //Sends STAT message to ESP and CCP every 2 seconds
            heartbeatTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //Send a STAT message to the MCP
                    String message = sendMCP.send_mcp_ccin();
                    sendMCP.sendMessage(message, MCP_IP_ADDRESS, MCP_PORT);
                    System.out.println("Sending MCP: " + message);
                    //Send a STAT message to the ESP
                    message = sendESP.send_esp_strq();
                    sendESP.sendMessage(message, ESP_IP_ADDRESS, ESP32_PORT);
                    System.out.println("Sending ESP: " + message);
                }
            }, 0, HEARTBEAT_INTERVAL); 

            //Runs every 5 seconds to check if the connection with ESP and MCP is maintained
            heartbeatTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //If there is no connection with the ESP, then print it out
                    if(!getESP_ACK() && !getMCP_ACK()){
                        intialiseConnections(sendMCP, sendESP, mcpReceive, espReceive);
                        return;
                    }

                    if(!getESP_ACK()){
                        System.out.println("Lost connection with ESP");
                    }
                    //Set the flag variable to false if there is a connection with the ESP
                    else{
                        setESP_ACK(false);
                    }
                    //If there is no connection with the ESP, then print it out
                    if(!getMCP_ACK()){
                        System.out.println("Lost connection with MCP");
                        //Could put LIMP Mode here where an EXEC message is sent to the ESP with the action being SLOW
                    }
                    //Set the flag variable to false if there is a connection with the ESP
                    else{
                        setMCP_ACK(false);
                    }
                }
            }, 10000, 4999);
    }

    public static void mcpMessageLogic(Send sendMCP, Send sendESP, ReceiveMCP mcpReceive, ReceiveESP espReceive){
        //Call the run method for mcpReceive
        mcpReceive.run();
        //If a message has been received on the MCP port
        if (mcpReceive.hasReceivedMessage()) {
            //If the message was intended for a CCP and in particular our CCP
            // mcpReceive.getIntendedClientID().equals(Receive.client_id) && mcpReceive.getIntendedClientType().equals(Receive.client_type)
            if (true) {
                //Message variable is what is going to be sent depending on the received message
                String message = null;
                //Variable to check the destination of the message
                String destination = "ESP";
                if(mcpReceive.getMessage() != null){
                    //If the message received is AKIN then set the MCP connection variable to true
                    if(mcpReceive.getMessage().equals("AKIN")){
                        setMCP_ACK(true);
                    }
                    //If the command is STAT then create a STAT message using the send object and change the destination to the MCP
                    else if(mcpReceive.getMessage().equals("STAT")){
                        setMCP_ACK(true);
                        message = sendMCP.send_mcp_stat(espReceive.getStatus());
                        destination = "MCP";
                    }
                    //If the command is EXEC then set the lights accordingly
                    else if(mcpReceive.getMessage().equals("EXEC")){
                        setMCP_ACK(true);
                        if(mcpReceive.getAction().equals("STOP")){
                            espReceive.setIntendedLightColour("RED");
                        }

                        else if(mcpReceive.getAction().equals("SLOW")){
                            espReceive.setIntendedLightColour("YELLOW");
                        }

                        else if(mcpReceive.getAction().equals("FAST")){
                            espReceive.setIntendedLightColour("GREEN");
                        }
                        //Create EXEC message
                        message = sendESP.send_esp_exec(mcpReceive.getAction());
                    }
                    //If the command is door close or open then create the according message
                    else if(mcpReceive.getMessage().equals("DOPN")){
                        setMCP_ACK(true);
                        // message = sendESP.send_esp_door("OPEN");
                    }

                    else if(mcpReceive.getMessage().equals("DCLS")){
                        setMCP_ACK(true);
                        // message = sendESP.send_esp_door("CLOSE");
                    }


//new mcp recieve logic
                    if(mcpReceive.getMessage().equals("AKIN")){
                        setMCP_ACK(true);
                    } else if(mcpReceive.getMessage().equals("AKST")){
                        // mcp acknowledged status
                        sendMCP.send_mcp_stat("Filler string");
                    } else if(mcpReceive.getMessage().equals("STRQ")){
                        // status request
                    } else if(mcpReceive.getMessage().equals("EXEC")){
                        // execute command
                    }
                }

                //If the message isn't null then send it to the destination using the specified ip address and port, then set the receivedMessage variable to false in the receive object
                if(message != null){
                    if(destination.equals("ESP")){
                        sendESP.sendMessage(message, ESP_IP_ADDRESS, ESP32_PORT);
                    }
                    else{
                        sendMCP.sendMessage(message, MCP_IP_ADDRESS, MCP_PORT);
                    }
                }
            }
            //Set the flag to false
            mcpReceive.setReceivedMessage(false);
        }
    }

    public static void espMessageLogic(Send sendMCP, Send sendESP, ReceiveMCP mcpReceive, ReceiveESP espReceive){
        //Call the run method for espReceive
        espReceive.run();

        //If a message has been received on the ESP Port
        if(espReceive.hasReceivedMessage()){
            //If the message was intended for a CCP and in particular our CCP
            // espReceive.getIntendedClientID().equals(Receive.client_id) && espReceive.getIntendedClientType().equals(Receive.client_type)
            if (true) {
                //Message variable is what is going to be sent depending on the received message
                String message = null;
                //Variable to check the destination of the message
                String destination = "MCP";
                if(espReceive.getMessage() != null){
                    //If the message received is ACKS then set the ESP connection variable to true
                    if(espReceive.getMessage().equals("CCIN")){
                        setESP_ACK(true);
                    }
                    //If the message received is STAT then set the ESPConnection variable to true and do error checking
                    if(espReceive.getMessage().equals("STAT")){
                        setESP_ACK(true);
                        //If the actual light colour on the BR isn't the intended light colour then resend the previous EXEC message to the ESP
                        if(espReceive.getActualLightColour() != espReceive.getActualLightColour()){
                            message = sendESP.send_esp_exec(mcpReceive.getAction());
                            destination = "ESP";
                        }
                    }
                }
                
                //If the message isn't null then send it to the destination using the specified ip address and port, then set the receivedMessage variable to false in the receive object
                if(message != null){
                    if(destination.equals("MCP")){
                        sendMCP.sendMessage(message, MCP_IP_ADDRESS, MCP_PORT);
                    }
                    else{
                        sendESP.sendMessage(message, ESP_IP_ADDRESS, ESP32_PORT);
                    }
                }
            }
            //Set the flag to false
            espReceive.setReceivedMessage(false);
        }
    }

    //Getters and setters for the private variables
    public static void setESP_ACK(boolean state){
        espStatAck = state;
    }

    public static Boolean getESP_ACK(){
        return espStatAck;
    }

    public static void setMCP_ACK(boolean state){
        mcpStatAck = state;
    }

    public static Boolean getMCP_ACK(){
        return mcpStatAck;
    }
}