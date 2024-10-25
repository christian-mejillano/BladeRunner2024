import java.net.*;

public class CCP {
    //Variables to be used for connection status checking
    public static boolean espConnection = false;
    public static boolean mcpConnection = false;
    //Network variables
    public static final String MCP_IP_ADDRESS = "10.20.30.1";
    public static final String ESP_IP_ADDRESS = "10.20.30.124";
    public static final int ME_PORT = 3024;
    public static final int MCP_PORT = 2000;
    public static final int ESP32_PORT = 12000;

    //Sending objects
    public static SendToMCP mcpSender;
    public static SendToESP espSender;

    //Thread objects for receiving from MCP/ESP and timers
    public static ThreadMCP mcpThread;
    public static ThreadESP espThread;
    public static TimerThread mainTimer;

    public static void main(String[] args) {
        //Create new sockets for ESP and MCP on their respective ports
        DatagramSocket mcpSendSocket = null;
        DatagramSocket espSocket = null;
        try{
            mcpSendSocket = new DatagramSocket(ME_PORT);
            espSocket = new DatagramSocket(ESP32_PORT);
        }
        catch(Exception e){
            System.out.println("Exception line 32 CCP.java");
        }

        //Create new threads and pass through the sockets
        espThread = new ThreadESP(espSocket);
        mcpThread = new ThreadMCP(mcpSendSocket);
        mainTimer = new TimerThread();
        
        //Create new sending objects
        mcpSender = new SendToMCP(mcpSendSocket);
        espSender = new SendToESP(espSocket);

        //Run the ESP and MCP Threads which listen for messages
        espThread.start();
        mcpThread.start();

        connectToESP();
        connectToMCP();

        // Runs forever
        while(true){
            mcpMessageLogic();
            espMessageLogic();
        }
    }

    //Function that is used to connect to the ESP
    public static void connectToESP(){
        //While the ESP isn't connected, run the esp and mcp logic functions. espConnection will only be true if STAT or AKIN/ACK is received from the ESP
        while(!espConnection){
            //Both functions are called here, in the case that the ESP is disconnected and MCP is connected
            espMessageLogic();
            mcpMessageLogic();
        }
        //Once espConnection is true, meaning that connection with the ESP has been established, (re)create the timers for ESP connection check and sending RQSTAT
        // mainTimer.setupESPConnectionCheck();
        mainTimer.setupESPStat();
    }

    //Function that is used to connect to the MCP
    public static void connectToMCP(){
        //While the MCP isn't connected, run the esp and mcp logic functions. mcpConnection will only be true if STAT or AKIn is received from the MCP
        while(!mcpConnection){
            //Both functions are called here, in the case that the MCP is disconnected and ESP is connected
            espMessageLogic();
            mcpMessageLogic();
            mcpSender.send_mcp_ccin();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Once mcpConnection is true, meaning that connection with the MCP has been established, (re)crate the timer for MCP connection check
        
        /* 
        * mainTimer.setupMCPConnectionCheck(); 
        */
    }

    //Function that checks the contents of the message received from the MCP and sends a message (to the MCP and/or ESP) accordingly
    public static void mcpMessageLogic(){
        //Check if a message has actually been received and isn't null
        if(mcpThread.hasReceivedMessage && mcpThread.getValueFromMessage("message") != null){
            mcpConnection = true;
            //If the message is AKIN then set MCP Connection to true as a connection has successfully been established
            if(mcpThread.getValueFromMessage("message").equals("AKIN")){}
            
            //If the message is AKST, meaning that the MCP acknowledges a STAT then do nothing. Still needs to be checked so that the final "else" statement doesn't include AKST.
            else if(mcpThread.getValueFromMessage("message").equals("AKST")){}

            //If the MCP is requesting a STAT message then set mcpConnection to true and send the actual status
            else if(mcpThread.getValueFromMessage("message").equals("STRQ")){
                mcpSender.send_mcp_stat(espThread.actualStatus);
            }

            //If the message is EXEC
            else if(mcpThread.getValueFromMessage("message").equals("EXEC")){
                //Send back NOIP is the action value is null
                if(mcpThread.getValueFromMessage("action") == null){
                    mcpSender.send_mcp_noip();
                }

                //If the action value matches with any of the following, then forward it to the ESP
                else if(mcpThread.getValueFromMessage("action").equals("STOPC")){
                    espSender.send_esp_exec("STOPC");
                    espThread.expectedStatus = "STOPC";
                }

                else if(mcpThread.getValueFromMessage("action").equals("STOPO")){
                    espSender.send_esp_exec("STOPO");
                    espThread.expectedStatus = "STOPO";
                }

                else if(mcpThread.getValueFromMessage("action").equals("FSLOWC")){
                    espSender.send_esp_exec("FSLOWC");
                    espThread.expectedStatus = "FSLOWC";
                }

                else if(mcpThread.getValueFromMessage("action").equals("FFASTC")){
                    espSender.send_esp_exec("FFASTC");
                    espThread.expectedStatus = "FFASTC";
                }

                else if(mcpThread.getValueFromMessage("action").equals("RSLOWC")){
                    espSender.send_esp_exec("RSLOWC");
                    espThread.expectedStatus = "RSLOWC";
                }

                else if(mcpThread.getValueFromMessage("action").equals("NOIP")){}

                //If the action value is disconnect, stop the mcpConnectionCheck timer, stop the mcpThread and send disconnect to the ESP
                else if(mcpThread.getValueFromMessage("action").equals("DISCONNECT")){
                    espSender.send_esp_exec("DISCONNECT");
                    mainTimer.mcpConnectionCheck.cancel();
                    mcpThread.interrupt();
                }
                
                //If the action value isn't recognised then send NOIP to the MCP
                else{
                    mcpSender.send_mcp_noip();
                }
            }

            //If the message value isn't recognised then send NOIP to the MCP
            else{
                mcpSender.send_mcp_noip();
            }

            //Reset the mcpThread.hasReceivedMessage so that the program can check the next time a message is received
            mcpThread.hasReceivedMessage = false;
        }
    }

    //Function that checks the contents of the message received from the ESP and sends a message (to the MCP and/or ESP) accordingly
    public static void espMessageLogic(){
        //Check if a message has actually been received
        if(espThread.hasReceivedMessage && espThread.getValueFromMessage("message") != null){
            espConnection = true;
            //If the message is CCIN then send AKIN
            if(espThread.getValueFromMessage("message").equals("CCIN")){
                espSender.send_esp_akin();
            }
            //If the message or sequence values (the values that will be checked later on) are null then do nothing
            else if(espThread.getValueFromMessage("message") == null || espThread.messageJSON.get("sequence") == null){}
            
            //If the message is AKIN/ACk then a connection (through the 3-way handshake) has been established so set espConnection to true
            else if(espThread.getValueFromMessage("message").equals("AKIN")){
                espThread.expectedStatus = "STOPC";
                espThread.actualStatus = "STOPC";
            }

            //If the message is STAT then set espConnection to true and check if the actual status matches the expectedStatus
            else if(espThread.getValueFromMessage("message").equals("STAT")){
                if(espThread.getValueFromMessage("status") == null){}
                
                //If the actual status doesn't match the expted status then update the actualStatus variable, send a STAT to MCP and send an EXEC with the
                //expected status to the ESP
                else if(!espThread.getValueFromMessage("status").equals(espThread.expectedStatus)){
                    espThread.actualStatus = espThread.getValueFromMessage("status");
                    espSender.send_esp_exec(espThread.expectedStatus);
                    mcpSender.send_mcp_stat(espThread.actualStatus);
                }
                //If actualStatus and expectedStatus match then set actualStatus to expectedStatus
                else{
                    espThread.actualStatus = espThread.expectedStatus;
                }
            }

            //If the message is ACK (EXEC has been executed) then updated actualStatus and send AKEX to MCP
            else if(espThread.getValueFromMessage("message").equals("AKEX")){
                espThread.actualStatus = espThread.expectedStatus;
                mcpSender.send_mcp_akex();
            }

            //If the message is disconnect (which should only ever be sent as a reply to a disconnect message being sent from CCP first)
            //Then close everything
            else if(espThread.getValueFromMessage("message").equals("DISCONNECT")){
                System.exit(0);
            }

            //Reset the espThread.hasReceivedMessage so that the program can check the next time a message is received
            espThread.hasReceivedMessage = false;
        }
    }
}