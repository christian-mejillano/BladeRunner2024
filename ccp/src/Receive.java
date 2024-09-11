import java.net.*;
import java.util.Date;

import org.json.simple.*;
import org.json.simple.parser.*;

public class Receive implements Runnable {
    //Variables for Receive class
    private DatagramSocket socket;
    private String client_type;
    private String client_id = "BR";
    private String message;
    private String light_colour;
    private String action;
    private int timestamp;
    private String status;
    private String station_id;
    private boolean hasReceivedMessage = false;

    //Constructor
    public Receive(DatagramSocket socket) {
        this.socket = socket;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            //Create empty packet
            byte[] buffer = new byte[1024];
            socket.setSoTimeout(500);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try{
                //Receive the packet
                socket.receive(packet);
            }
            catch(SocketTimeoutException e){
            }
            System.out.println("Received packet: "+ packet);
            
            //Convert it to String
            message = new String(packet.getData(), 0, packet.getLength());
            //Set flag to true
            if(packet != null || message != null){
                hasReceivedMessage = true;
                //Call updateValues, print out the message and set the flag to false
                updateValues(message);
                System.out.println("Received: " + message);
            } 

            System.out.println("Converted message: " + message);
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
        //Also do null checks
        if(jsonMessage.get("client_type") != null){
            setClientType((String) jsonMessage.get("client_type"));
        }
        if(jsonMessage.get("message") != null){
            setMessage((String) jsonMessage.get("message"));
        }
        if(jsonMessage.get("action") != null){
            setAction((String) jsonMessage.get("action"));
        }
        if(jsonMessage.get("timestamp") != null){
            setTimestamp((Integer) jsonMessage.get("timestamp"));
        }
        if(jsonMessage.get("status") != null){
            setStatus((String) jsonMessage.get("status"));
        }
        if(jsonMessage.get("station_id") != null){
            setStationId((String) jsonMessage.get("station_id"));
        }
        
        // if(this.getAction().equals("FAST")){
        //     this.setLightColour("green");
        // }
        // else if(this.getAction().equals("SLOW")){
        //     this.setLightColour("yellow");
        // }
        // else if(this.getAction().equals("STOP")){
        //     this.setLightColour("red");
        // }
    }

    //If the command received is AKIN from the MCP
    @SuppressWarnings("unchecked")
    public String akinCommandMCP(){
        JSONObject message = new JSONObject();
        message.put("client_type", "ccp");
        message.put("message", "CCIN");
        message.put("client_id", this.getClientId());
        message.put("timestamp", new Date().getTime() / 1000);
        return message.toString();
    }

    //If the command received is STAT from the MCP
    @SuppressWarnings("unchecked")
    public String statusCommandMCP(){
        JSONObject message = new JSONObject();
        message.put("client_type", "ccp");
        message.put("message", "STAT");
        message.put("client_id", this.getClientId());
        message.put("timestamp", new Date().getTime() / 1000);
        message.put("status", this.getStatus());
        return message.toString();
    }

    //If the command received is EXEC from the MCP
    @SuppressWarnings("unchecked")
    public String execCommandMCP(){
        JSONObject message = new JSONObject();
        message.put("client_type", "ccp");
        message.put("message", "EXEC");
        message.put("client_id", this.getClientId());
        message.put("timestamp", new Date().getTime() / 1000);
        message.put("action", this.getAction());
        message.put("light_colour", this.getLightColour());
        return message.toString();
    }

    //If the command received is DOOR from the MCP
    @SuppressWarnings("unchecked")
    public String doorsCommandMCP(){
        JSONObject message = new JSONObject();
        message.put("client_type", "ccp");
        message.put("message", "DOOR");
        message.put("client_id", this.getClientId());
        message.put("timestamp", new Date().getTime() / 1000);
        message.put("action", this.getAction());
        return message.toString();
    }

    //If the command received is ACKS from the ESP
    @SuppressWarnings("unchecked")
    public String acksCommandESP(){
        JSONObject message = new JSONObject();
        message.put("client_type", "ccp");
        message.put("message", "ACKS");
        message.put("client_id", this.getClientId());
        message.put("timestamp", new Date().getTime() / 1000);
        return message.toString();
    }

    //If the command received is STAT from the ESP
    @SuppressWarnings("unchecked")
    public void statusCommandESP(){
        
    }

    //If the command received is STAT (station) from the ESP
    @SuppressWarnings("unchecked")
    public String stationCommandESP(){
        JSONObject message = new JSONObject();
        message.put("client_type", "ccp");
        message.put("message", "STAT");
        message.put("client_id", this.getClientId());
        message.put("timestamp", new Date().getTime() / 1000);
        message.put("status", this.getStatus());
        message.put("station_id", this.getStationId());
        return message.toString();
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

    public void setClientId(String client_id) { 
        this.client_id = client_id; 
    }

    public String getClientId(){
        return this.client_id;
    }

    public void setLightColour(String colour) { 
        this.light_colour = colour; 
    }

    public String getLightColour(){
        return this.light_colour;
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

    public void setReceivedMessage(boolean state){
        this.hasReceivedMessage = state;
    }
}