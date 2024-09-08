import java.net.DatagramSocket;
import org.json.simple.*;

public class CCP {
    String client_type;
    String client_id;
    int timestamp;
    String status;

    public static void main(String[] args) {
        final String MCP_IP_ADDRESS = "10.20.30.1";
        final int MCP_PORT = 2001;
        final int ESP32_PORT = 4500; //CHANGE IF NEEDED 
        //Set up MCP and ESP32 connection 
        //boolean connected 
        try {
            //Create new socket
            DatagramSocket socket = new DatagramSocket();
            
            // Instantiate send and receive objects
            Send send = new Send(socket);
            Receive receive = new Receive(socket);
            // Send initalisation message to MCP
            send.sendInitalisation(MCP_IP_ADDRESS, MCP_PORT);

            // Thread.sleep(500);
            //Runs forevery
            while (true) {
                //If a message has been received by the receive object
                if (receive.hasReceivedMessage()) {
                    //If the message has been sent by the MCP forward it onto the ESP
                    if (receive.getClientType().equals("MCP")) {
                        send.sendMessage(receive, "ESP32_IP_Address", ESP32_PORT);
                    } 
                    //If the message has been sent by the ESP forward it onto the MCP
                    else if (receive.getClientType().equals("ESP")) {
                        send.sendMessage(receive, MCP_IP_ADDRESS, MCP_PORT);
                    }
                }
                
            }
        } 
        //In case there are any errors print out the stack trace
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}