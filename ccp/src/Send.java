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

    @SuppressWarnings("unchecked")
    public JSONObject messageTemplate(){
        JSONObject message = new JSONObject();
        message.put("client_type", Receive.client_type);
        message.put("client_id", Receive.client_id);
        message.put("timestamp", new Date().getTime() / 1000);
        return message;
    }

    @SuppressWarnings("unchecked")
    public String send_esp_akin(){
        JSONObject message = messageTemplate();
        message.put("message", "AKIN");
        return message.toString();
    }

    @SuppressWarnings("unchecked")
    public String send_esp_stat(){
        JSONObject message = messageTemplate();
        message.put("message", "STAT");
        return message.toString();
    }

    @SuppressWarnings("unchecked")
    public String send_esp_exec(String light_colour, String action){
        JSONObject message = messageTemplate();
        message.put("message", "EXEC");
        message.put("action", action);
        message.put("light_colour", light_colour);
        return message.toString();
    }

    @SuppressWarnings("unchecked")
    public String send_esp_door(String action){
        JSONObject message = messageTemplate();
        message.put("message", "DOOR");
        message.put("action", action);
        return message.toString();
    }

    @SuppressWarnings("unchecked")
    public String send_mcp_ccin(){
        JSONObject message = messageTemplate();
        message.put("message", "CCIN");
        return message.toString();
    }

    @SuppressWarnings("unchecked")
    public String send_mcp_stat(String status){
        JSONObject message = messageTemplate();
        message.put("message", "STAT");
        message.put("status", status);
        return message.toString();
    }

    @SuppressWarnings("unchecked")
    public String send_mcp_stan(String status, String station_id){
        JSONObject message = messageTemplate();
        message.put("message", "STAT");
        message.put("status", status);
        return message.toString();
    }
}