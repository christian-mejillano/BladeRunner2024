import java.net.*;
import java.util.Date;
import org.json.simple.*;

public class Send {
    //Variables for Send class
    private DatagramSocket socket;

    //Constructor
    public Send(DatagramSocket socket) {
        this.socket = socket;
    }

    //Function to send the message given a receive object, ip address and port
    //The receive parameter will need to change to a JSON object as right now this is just forwarding whatever is being received
    public void sendMessage(String jsonToSend, String ipAddress, int port) throws Exception {
        //Create a new packet and send it to the given ip address and port
        byte[] buffer = jsonToSend.getBytes();
        InetAddress address = InetAddress.getByName(ipAddress);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

    //Create a message template (just all the values that are common across all message)
    //All other message creation methods in this class call this method since it contains variables that don't change such as
    //client_type, client_id and timestamp
    @SuppressWarnings("unchecked")
    private JSONObject messageTemplate(){
        JSONObject message = new JSONObject();
        message.put("client_type", Receive.client_type);
        message.put("client_id", Receive.client_id);
        message.put("timestamp", new Date().getTime() / 1000);
        return message;
    }

    //Create AKIN message to send to the ESP
    @SuppressWarnings("unchecked")
    public String send_esp_akin(){
        JSONObject message = messageTemplate();
        message.put("message", "AKIN");
        return message.toString();
    }

    //Create STAT message to send to the ESP
    @SuppressWarnings("unchecked")
    public String send_esp_stat(){
        JSONObject message = messageTemplate();
        message.put("message", "STAT");
        return message.toString();
    }

    //Create EXEC message to send to the ESP
    @SuppressWarnings("unchecked")
    public String send_esp_exec(String light_colour, String action){
        JSONObject message = messageTemplate();
        message.put("message", "EXEC");
        message.put("action", action);
        message.put("light_colour", light_colour);
        return message.toString();
    }

    //Crate DOOR message to send to the ESP
    @SuppressWarnings("unchecked")
    public String send_esp_door(String action){
        JSONObject message = messageTemplate();
        message.put("message", "DOOR");
        message.put("action", action);
        return message.toString();
    }

    //Create CCIN message to send to the MCP
    @SuppressWarnings("unchecked")
    public String send_mcp_ccin(){
        JSONObject message = messageTemplate();
        message.put("message", "CCIN");
        return message.toString();
    }

    //Create STAT message to send to the MCP
    @SuppressWarnings("unchecked")
    public String send_mcp_stat(String status){
        JSONObject message = messageTemplate();
        message.put("message", "STAT");
        message.put("status", status);
        return message.toString();
    }

    //Create STAN message to send to the MCP
    @SuppressWarnings("unchecked")
    public String send_mcp_stan(String status, String station_id){
        JSONObject message = messageTemplate();
        message.put("message", "STAT");
        message.put("status", status);
        return message.toString();
    }
}