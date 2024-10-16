import java.net.*;
import java.util.*;

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
    private static int mcpReceiveSequence;

    public static void main(String[] args) {
        try {
            //Create new sockets for ESP and MCP on their respective ports
            DatagramSocket mcpSocket = new DatagramSocket(MCP_PORT);
            DatagramSocket espSocket = new DatagramSocket(ESP32_PORT);

            Send sendMCP = new Send(mcpSocket);
            Send sendESP = new Send(espSocket);

            ThreadESP espThread = new ThreadESP(espSocket);
            ThreadMCP mcpThread = new ThreadMCP(mcpSocket);
            TimerThread mainTimer = new TimerThread();      

            Thread mcpThreadObject = new Thread(mcpThread);
            Thread espThreadObject = new Thread(mcpThread);
            Thread timerThreadObject = new Thread(mainTimer);

            espThreadObject.start();
            mcpThreadObject.start();
            timerThreadObject.start();
            
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

    public static void mcpMessageLogic(Send sendMCP, Send sendESP, ReceiveMCP mcpReceive, ReceiveESP espReceive){
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
                    } 
                    else if(mcpReceive.getMessage().equals("AKST")){
                        // mcp acknowledged status
                        sendMCP.send_mcp_stat("Filler string");
                    } 
                    else if(mcpReceive.getMessage().equals("STRQ")){
                        // status request
                    } 
                    else if(mcpReceive.getMessage().equals("EXEC")){
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