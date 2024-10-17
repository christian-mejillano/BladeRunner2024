import java.net.*;

public class CCP {
    //Variables to be used for connection status checking
    public static boolean espConnection = false;
    public static boolean mcpConnection = false;
    //10.20.30.11
    //Network variables
    public static final String MCP_IP_ADDRESS = "10.20.30.1";
    public static final String ESP_IP_ADDRESS = "10.20.30.124";
    public static final int MCP_PORT = 2000;
    public static final int ESP32_PORT = 3024;

    //Sending objects
    public static SendToMCP mcpSender;
    public static SendToESP espSender;

    //Thread objects for receiving from MCP/ESP and timers
    public static ThreadMCP mcpThread;
    public static ThreadESP espThread;
    public static TimerThread mainTimer;

    public static void main(String[] args) {
        try {
            //Create new sockets for ESP and MCP on their respective ports
            DatagramSocket mcpSocket = new DatagramSocket(MCP_PORT);
            DatagramSocket espSocket = new DatagramSocket(ESP32_PORT);

            //Create new threads and pass through the sockets
            espThread = new ThreadESP(espSocket);
            mcpThread = new ThreadMCP(mcpSocket);
            mainTimer = new TimerThread();
            
            //Create new sending objects
            mcpSender = new SendToMCP(mcpSocket);
            espSender = new SendToESP(espSocket);

            //Run the ESP and MCP Threads which listen for messages
            espThread.start();
            mcpThread.start();
            //Attempt to initalise connection with ESP then MCP
            intialiseConnections();

            //Runs forever
            while(true){
                //If both ESP and MCP are connected then run their respective message logic functions
                if(espConnection && mcpConnection){
                    mcpMessageLogic();
                    espMessageLogic();
                }
                //If the MCP and/or ESP aren't connected
                else{
                    //First run the message logic in the case that the timer has set either of the connection flag variables to false
                    //And it has aligned with this check so it could just be that the flag was set to false and the connection isn't actually offline
                    mcpMessageLogic();
                    espMessageLogic();
                    //If ESP is connected but MCP is disconnected then update the expected BR status to STOPC, send this to the ESP, 
                    //remove the mcpConnectionCheckTimer and attempt to connect to the MCP
                    if(espConnection && !mcpConnection){
                        espThread.expectedStatus = "STOPC";
                        espSender.send_esp_exec(espThread.expectedStatus);
                        mainTimer.mcpConnectionCheck.cancel();
                        connectToMCP();
                    }
                    //If the MCP is connected but ESP is disconnected update the actual status to ERR, send this to the MCP, remove all timers
                    //and wait for 3-way handshake
                    else if(!espConnection && mcpConnection){
                        //set status to ERR
                        espThread.actualStatus = "ERR";
                        mcpSender.send_mcp_stat(espThread.actualStatus);
                        mainTimer.espConnectionCheck.cancel();
                        mainTimer.espSendSTAT.cancel();
                        connectToESP();
                    }
                    //If both MCP and ESP are disconnected then remove all timers and start again by running the initaliseConnections function
                    //This will start the whole process again; waiting for ESP connection, establishing ESP connection, creating ESP timers, establishing MCP connection
                    //creating MCP timer
                    else if(!espConnection && !mcpConnection){
                        mainTimer.espConnectionCheck.cancel();
                        mainTimer.espSendSTAT.cancel();
                        mainTimer.mcpConnectionCheck.cancel();
                        intialiseConnections();
                    }
                }
            }

        }
        //In case there are any errors that haven't been addressed, print out the stack trace
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Function that calls two other functions; one for connecting to ESP and another for MCP
    public static void intialiseConnections(){
        connectToESP();
        connectToMCP();
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
        mainTimer.setupESPConnectionCheck();
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
        }
        //Once mcpConnection is true, meaning that connection with the MCP has been established, (re)crate the timer for MCP connection check
        mainTimer.setupMCPConnectionCheck();
    }

    //Function that checks the contents of the message received from the MCP and sends a message (to the MCP and/or ESP) accordingly
    public static void mcpMessageLogic(){
        //Check if a message has actually been received
        if(mcpThread.hasReceivedMessage){
            //If the message is null, send NOIP to MCP
            if(mcpThread.getValueFromMessage("message") == null){
                mcpSender.send_mcp_noip();
            }
            //If the message is AKIN then set MCP Connection to true as a connection has successfully been established
            else if(mcpThread.getValueFromMessage("message").equals("AKIN")){
                mcpConnection = true;
            }
            
            //If the message is AKST, meaning that the MCP acknowledges a STAT then do nothing. Still needs to be checked so that the final "else" statement doesn't include AKST.
            else if(mcpThread.getValueFromMessage("message").equals("AKST")){}

            //If the MCP is requesting a STAT message then set mcpConnection to true and send the actual status
            else if(mcpThread.getValueFromMessage("message").equals("STRQ")){
                mcpConnection = true;
                espThread.expectedStatus = mcpThread.getValueFromMessage("message");
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
                }

                else if(mcpThread.getValueFromMessage("action").equals("STOPO")){
                    espSender.send_esp_exec("STOPO");
                }

                else if(mcpThread.getValueFromMessage("action").equals("FSLOWC")){
                    espSender.send_esp_exec("FSLOWC");
                }

                else if(mcpThread.getValueFromMessage("action").equals("FFASTC")){
                    espSender.send_esp_exec("FFASTC");
                }

                else if(mcpThread.getValueFromMessage("action").equals("RSLOWC")){
                    espSender.send_esp_exec("RSLOWC");
                }

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
        if(espThread.hasReceivedMessage){
            //If the message or sequence values (the values that will be checked later on) are null then do nothing
            if(espThread.getValueFromMessage("message") == null || espThread.getValueFromMessage("sequence") == null){}

            //If the message is CCIN then send AKIN
            else if(espThread.getValueFromMessage("message").equals("CCIN")){
                espSender.send_esp_akin();
            }

            //CCIN is the only time we don't need to check the sequence number, hence in the order of else ifs, this check comes second.
            //If the message isn't CCIN then check if the sequence number matches with the sequence number sent on the last message
            //If the sequence number doesn't match then send an EXEC message with the expectedStatus
            else if(Integer.valueOf(espThread.getValueFromMessage("sequence")) != espSender.sendingSequenceNumber - 1){
                espSender.send_esp_exec(espThread.expectedStatus);
            }
            
            //If the message is AKIN/ACk then a connection (through the 3-way handshake) has been established so set espConnection to true
            else if(espThread.getValueFromMessage("message").equals("AKIN/ACK")){
                espConnection = true;
            }

            //If the message is STAT then set espConnection to true and check if the actual status matches the expectedStatus
            else if(espThread.getValueFromMessage("message").equals("STAT")){
                espConnection = true;
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
            else if(espThread.getValueFromMessage("message").equals("ACK")){
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