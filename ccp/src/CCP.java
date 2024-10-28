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

        // Runs forever
        while(true){
            mcpMessageLogic();
            espMessageLogic();
        }
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

            //If the message or sequence values (the values that will be checked later on) are null then do nothing
            if(espThread.getValueFromMessage("message") == null || espThread.messageJSON.get("sequence") == null){}

            //If the message is STAT then set espConnection to true and check if the actual status matches the expectedStatus
            else if(espThread.getValueFromMessage("message").equals("STAT")){
                if(espThread.getValueFromMessage("status") != null){
                    espThread.actualStatus = espThread.getValueFromMessage("status");
                }
            }

            //If the message is ACK (EXEC has been executed) then updated actualStatus and send AKEX to MCP
            else if(espThread.getValueFromMessage("message").equals("AKEX")){
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