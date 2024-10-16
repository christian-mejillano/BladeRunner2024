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
    public String ipAddress = "";
    public int port = 0;

    //Constructor
    public SendToESP(DatagramSocket socket) {
        this.socket = socket;
        Random random = new Random();
        sendingSequenceNumber = random.nextInt(1000, 30000);
    }

    //Function to send the message given a receive object, ip address and port
    //The receive parameter will need to change to a JSON object as right now this is just forwarding whatever is being received
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
            socket.send(packet);
            System.out.println("Sending: " + jsonToSend);
            sendingSequenceNumber++;
        }
        catch(IOException e){
            System.out.println("There was an error with sending the message. Line 30 Send.java");
        }
    }

        //Create a message template (just all the values that are common across all message)
    //All other message creation methods in this class call this method since it contains variables that don't change such as
    //clientType, clientID and timestamp
    @SuppressWarnings("unchecked")
    private JSONObject messageTemplate(){
        JSONObject message = new JSONObject();
        message.put("client_type", clientType);
        message.put("client_id", clientID);
        return message;
    }

    //Create AKIN message to send to the ESP
    @SuppressWarnings("unchecked")
    public void send_esp_akin(){
        JSONObject message = messageTemplate();
        message.put("message", "AKIN");
        message.put("sequence", sendingSequenceNumber);
        sendMessage(message.toString());
    }

    //Create Status Request Message to ESP
    @SuppressWarnings("unchecked")
    public void send_esp_strq(){
        JSONObject message = messageTemplate();
        message.put("message", "STRQ");
        message.put("sequence", sendingSequenceNumber);
        sendMessage(message.toString());
    }

    //Create EXEC message to send to the ESP
    @SuppressWarnings("unchecked")
    public void send_esp_exec(String action){
        JSONObject message = messageTemplate();
        message.put("message", "EXEC");
        message.put("action", action);
        message.put("sequence", sendingSequenceNumber);
        sendMessage(message.toString());
    }
}
