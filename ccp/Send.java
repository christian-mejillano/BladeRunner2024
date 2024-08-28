import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Send {
    private DatagramSocket socket;

    public Send(DatagramSocket socket) {
        this.socket = socket;
    }

    public void sendMessage(String message, String ipAddress, int port) throws Exception {
        byte[] buffer = message.getBytes();
        InetAddress address = InetAddress.getByName(ipAddress);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }
}
