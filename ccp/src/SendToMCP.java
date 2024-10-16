import java.io.IOException;
import java.net.*;
import java.util.*;
import org.json.simple.*;

public class SendToMCP {
    //Variables for Send class
    private DatagramSocket socket;
    public int sendingSequenceNumber;
    public static String clientType = "ccp";
    public static String clientID = "BR24";
    public String ipAddress = CCP.MCP_IP_ADDRESS;
    public int port = CCP.MCP_PORT;

    //Constructor
    public SendToMCP(DatagramSocket socket) {
        this.socket = socket;
        Random random = new Random();
        sendingSequenceNumber = random.nextInt(1000, 30000);
    }

    //Function to send the message given a String (should be JSON)
    public void sendMessage(String jsonToSend){
        //Create a new packet and send it to the given ip address and port
        byte[] buffer = jsonToSend.getBytes();
        InetAddress address = null;
        try{
            address = InetAddress.getByName(ipAddress);
        }
        catch(UnknownHostException e){
            System.out.println("There was an error with the address. Line 21 Send.java");
            System.exit(0);
        }
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        try{
            //Send packet
            socket.send(packet);
            System.out.println("Sending to MCP: " + jsonToSend);
            //Increment the sequence number
            sendingSequenceNumber++;
        }
        catch(IOException e){
            System.out.println("There was an error with sending the message. Line 30 Send.java");
        }
    }


    //Create a message template (just all the values that are common across all message)
    //All other message creation methods in this class call this method since it contains variables that don't change such as
    //client_type, client_id and sequence number
    @SuppressWarnings("unchecked")
    private JSONObject messageTemplate(){
        JSONObject message = new JSONObject();
        message.put("client_type", clientType);
        message.put("client_id", clientID);
        message.put("client_id", clientID);
        message.put("sequence_number", sendingSequenceNumber);
        return message;
    }

    //Send CCIN to ESP
    @SuppressWarnings("unchecked")
    public void send_mcp_ccin(){
        JSONObject message = messageTemplate();
        message.put("message", "CCIN");
        sendMessage(message.toString());
    }

    //Send STAT to MCP
    @SuppressWarnings("unchecked")
    public void send_mcp_stat(String status){
        JSONObject message = messageTemplate();
        message.put("message", "STAT");
        message.put("status", status);
        sendMessage(message.toString());
    }

    //Send AKEX to MCP
    @SuppressWarnings("unchecked")
    public void send_mcp_akex(){
        JSONObject message = messageTemplate();
        message.put("message", "AKEX");
        sendMessage(message.toString());
    }

    //Send NOIP to MCP
    @SuppressWarnings("unchecked")
    public void send_mcp_noip(){
        JSONObject message = messageTemplate();
        message.put("message", "NOIP");
        sendMessage(message.toString());
    }
}
