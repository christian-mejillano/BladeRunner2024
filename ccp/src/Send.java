import java.net.*;

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
}