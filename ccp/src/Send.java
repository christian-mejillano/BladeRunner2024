import java.io.IOException;
import java.net.*;
import java.util.*;
import org.json.simple.*;

public class Send {
    //Variables for Send class
    private DatagramSocket socket;
    public int sendingSequenceNumber;

    //Constructor
    public Send(DatagramSocket socket) {
        this.socket = socket;
        Random random = new Random();
        sendingSequenceNumber = random.nextInt(1000, 30000);
    }

    //Function to send the message given a receive object, ip address and port
    //The receive parameter will need to change to a JSON object as right now this is just forwarding whatever is being received
    public void sendMessage(String jsonToSend, String ipAddress, int port){
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
            socket.send(packet);
            System.out.println("Sending: " + jsonToSend);
        }
        catch(IOException e){
            System.out.println("There was an error with sending the message. Line 30 Send.java");
        }
    }

    //Create a message template (just all the values that are common across all message)
    //All other message creation methods in this class call this method since it contains variables that don't change such as
    //client_type, client_id and timestamp
    @SuppressWarnings("unchecked")
    private JSONObject messageTemplate(){
        JSONObject message = new JSONObject();
        message.put("client_type", Receive.client_type);
        message.put("client_id", Receive.client_id);
        return message;
    }

    //Create AKIN message to send to the ESP
    @SuppressWarnings("unchecked")
    public String send_esp_akin(){
        JSONObject message = messageTemplate();
        message.put("message", "AKIN");
        message.put("sequence", sendingSequenceNumber);
        return message.toString();
    }

    //Create Status Request Message to ESP
    @SuppressWarnings("unchecked")
    public String send_esp_strq(){
        JSONObject message = messageTemplate();
        message.put("message", "STRQ");
        message.put("sequence", sendingSequenceNumber);
        return message.toString();
    }

    //Create EXEC message to send to the ESP
    @SuppressWarnings("unchecked")
    public String send_esp_exec(String action){
        JSONObject message = messageTemplate();
        message.put("message", "EXEC");
        message.put("action", action);
        message.put("sequence", sendingSequenceNumber);
        return message.toString();
    }

    //Create CCIN message to send to the MCP
    @SuppressWarnings("unchecked")
    public String send_mcp_ccin(){
        JSONObject message = messageTemplate();
        message.put("message", "CCIN");
        message.put("sequence_number", sendingSequenceNumber);
        return message.toString();
    }

    //Create STAT message to send to the MCP
    @SuppressWarnings("unchecked")
    public String send_mcp_stat(String status){
        JSONObject message = messageTemplate();
        message.put("message", "STAT");
        message.put("status", status);
        message.put("sequence_number", sendingSequenceNumber);
        return message.toString();
    }

    //Create AKEX message to send to the MCP
    @SuppressWarnings("unchecked")
    public String send_mcp_akex(){
        JSONObject message = messageTemplate();
        message.put("message", "AKEX");
        message.put("sequence_number", sendingSequenceNumber);
        return message.toString();
    }
}