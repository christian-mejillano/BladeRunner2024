import java.net.*;

import org.json.simple.*;

public class Send {
    //Variables for Send class
    private DatagramSocket socket;
    private String client_type = "ccp";
    private String message;
    private int timestamp;
    private String client_id = "BR";
    private String status;
    private String station_id;
    private String action;

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

    //Function that will update the values given a receive object and will return a JSON object based on the values received
    @SuppressWarnings("unchecked")
    public JSONObject updateValues(Receive receive){
        //Update the necessary variables
        setMessage(receive.getMessage());
        setTimestamp(receive.getTimestamp());
        setStatus(receive.getStatus());
        setStationId(receive.getStationId());
        setAction(receive.getAction());

        //Create a new JSON object with all the private variables from this class
        JSONObject jsonToSend = new JSONObject();
        jsonToSend.put("client_id", getClientId());
        jsonToSend.put("message", getMessage());
        jsonToSend.put("timestamp", getTimestamp());
        jsonToSend.put("status", getStatus());
        jsonToSend.put("staion_id", getStationId());
        jsonToSend.put("action", getAction());

        //Return the JSON object
        return jsonToSend;
    }
    
    //Getters and Setters for all the private variables in this class
    public void setClientType(String client_type) { 
        this.client_type = client_type; 
    }

    public String getClientType(){
        return this.client_type;
    }

    public void setMessage(String message) { 
        this.message = message; 
    }

    public String getMessage(){
        return this.message;
    }
    
    public void setTimestamp(int timestamp) { 
        this.timestamp = timestamp; 
    }

    public int getTimestamp(){
        return this.timestamp;
    }

    public void setClientId(String client_id) { 
        this.client_id = client_id; 
    }

    public String getClientId(){
        return this.client_id;
    }

    public void setStatus(String status) { 
        this.status = status; 
    }

    public String getStatus() { 
        return this.status; 
    }

    public void setStationId(String station_id) { 
        this.station_id = station_id; 
    }

    public String getStationId() { 
        return this.station_id; 
    }

    public void setAction(String action) {
         this.action = action;
    }

    public String getAction() {
        return this.action;
   }
}