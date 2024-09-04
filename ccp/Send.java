import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Send {
    private DatagramSocket socket;
    private String client_type = "ccp";
    private String message;
    private int timestamp;
    private String client_id = "BR";
    private String status;
    private String station_id;
    private String action;

    public Send(DatagramSocket socket) {
        this.socket = socket;
    }

    public void sendMessage(String message, String ipAddress, int port) throws Exception {
        byte[] buffer = message.getBytes();
        InetAddress address = InetAddress.getByName(ipAddress);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

  
    public void setClientType(String client_type) { 
        this.client_type = client_type; 
    }
    public void setMessage(String message) { 
        this.message = message; }
    public void setTimestamp(int timestamp) { this.timestamp = timestamp; 
    }
    public void setClientId(String client_id) { 
        this.client_id = client_id; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }
    public void setStationId(String station_id) { 
        this.station_id = station_id; 
    }
    public void setAction(String action) {
         this.action = action;
        }
}