import java.io.IOException;
import java.net.*;
import java.util.*;
import org.json.simple.*;

public class SendToESP {
    //Variables for Send class
    private DatagramSocket socket;
    public int sendingSequenceNumber;
    public static String clientType = "ccp";
    public static String clientID = "BR24";
    public String ipAddress = CCP.ESP_IP_ADDRESS;
    public int port = CCP.ESP32_PORT;

    //Constructor
    public SendToESP(DatagramSocket socket) {
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
            System.out.println("Sending to ESP: " + jsonToSend);
            //Increment the sequence number
            sendingSequenceNumber++;
        }
        catch(IOException e){
            System.out.println("There was an error with sending the message. Line 30 Send.java");
        }
    }

    //Create a message template (just all the values that are common across all message)
    //All other message creation methods in this class call this method since it contains variables that don't change such as
    //clientType, clientID and sequence number
    @SuppressWarnings("unchecked")
    private JSONObject messageTemplate(){
        JSONObject message = new JSONObject();
        message.put("client_type", clientType);
        message.put("client_id", clientID);
        message.put("sequence", sendingSequenceNumber);
        return message;
    }

    //Send AKIN to ESP
    @SuppressWarnings("unchecked")
    public void send_esp_akin(){
        JSONObject message = messageTemplate();
        message.put("message", "AKIN");
        sendMessage(message.toString());
    }

    //Send STRQ to ESP
    @SuppressWarnings("unchecked")
    public void send_esp_strq(){
        JSONObject message = messageTemplate();
        message.put("message", "STRQ");
        sendMessage(message.toString());
    }

    //Send EXEC to ESP
    @SuppressWarnings("unchecked")
    public void send_esp_exec(String action){
        JSONObject message = messageTemplate();
        message.put("message", "EXEC");
        message.put("action", action);
        sendMessage(message.toString());
    }
}
