import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Receive implements Runnable {
    //Variables for Receive class
    private DatagramSocket socket;
    private String client_type;
    private String message;
    private String action;
    private int timestamp;
    private String status;
    private String station_id;
    private boolean hasReceivedMessage = false;

    //Constructor
    public Receive(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //Create empty packet
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                //Receive the packet
                socket.receive(packet);
                //Convert it to String
                message = new String(packet.getData(), 0, packet.getLength());
                //Update the client -- not sure if this will work -- may need to update client_type using the updateValues() function
                client_type = extractClientType(message);
                //Set flag to true
                hasReceivedMessage = true;
                //Call updateValues, print out the message and set the flag to false
                updateValues(message);
                System.out.println("Received: " + message);
                hasReceivedMessage = false;
            }
        } 
        //In case there are any errors print out the stack trace
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Function that will update all the private variables given a raw message in String from
    public void updateValues(String rawMessage){
        //Declare JSON object which will just set all the values to null if the message isn't in proper form or there is no message
        JSONObject jsonMessage = null;
        try{
            //Parse the message and store it in jsonMessage
            JSONParser parser = new JSONParser();
            jsonMessage = (JSONObject) parser.parse(rawMessage);
        }
        //Stop running if there is a Parse Exception
        catch (ParseException e){
            e.printStackTrace();
            return;
        }
        //Set all the private variables according to the data recieved from the packet
        setClientType((String) jsonMessage.get("client_type"));
        setMessage((String) jsonMessage.get("message"));
        setAction((String) jsonMessage.get("action"));
        setTimestamp((Integer) jsonMessage.get("timestamp"));
        setStatus((String) jsonMessage.get("status"));
        setStationId((String) jsonMessage.get("station_id"));
    }

    // Need to do: 
    //MCP-
    //AKIN: receive acknowledge message from MCP
    //STAT: Receive status every 2 seconds from MCP
    //EXEC: receive messgae for blade runner to move forward (slow)
    //EXEC: receive messgae for blade runner to move forward (fast)
    //EXEC: receive messgae for blade runner to stop
    //DOPN: doors open
    //DCLS: doors closed

    //ESP32- 
    // Ack

    //Getters and Setters for all the private variables in this class
    public String getClientType() { 
        return client_type; 
    }

    public void setClientType(String clientType){
        this.client_type = clientType;
    }

    public String getMessage() { 
        return message; 
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getAction() { 
        return action; 
    }

    public void setAction(String action){
        this.action = action;
    }

    public int getTimestamp() { 
        return timestamp; 
    }

    public void setTimestamp(int timestamp){
        this.timestamp = timestamp;
    }

    public String getStatus() { 
        return status; 
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getStationId() { 
        return station_id; 
    }

    public void setStationId(String station_id){
        this.station_id = station_id;
    }

    public boolean hasReceivedMessage() { 
        return hasReceivedMessage;
    }

    private String extractClientType(String message) {
        return "MCP"; 
    }
}