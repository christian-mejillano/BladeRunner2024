import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receive implements Runnable {
    private DatagramSocket socket;
    private String client_type;
    private String message;
    private String action;
    private int timestamp;
    private String status;
    private String station_id;
    private boolean hasReceivedMessage = false;

    public Receive(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(packet);
                message = new String(packet.getData(), 0, packet.getLength());
                client_type = extractClientType(message);  
                hasReceivedMessage = true;
                System.out.println("Received: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getClientType() { 
        return client_type; 
    }
    public String getMessage() { 
        return message; 
    }
    public String getAction() { 
        return action; 
    }
    public int getTimestamp() { 
        return timestamp; 
    }
    public String getStatus() { 
        return status; 
    }
    public String getStationId() { 
        return station_id; 
    }
    public boolean hasReceivedMessage() { 
        return hasReceivedMessage;
     }

    private String extractClientType(String message) {
       
        return "MCP"; 
    }
    //MCP-
    //AKIN: receive acknowledge message from MCP
    //STAT: Receive status every 2 seconds from MCP
    //EXEC: receive messgae for break runner to move forward (slow)
    //EXEC: receive messgae for break runner to move forward (fast)
    //EXEC: receive messgae for break runner to stop
    //DOPN: doors open
    //DCLS: doors closed

    //ESP32- 
    
}