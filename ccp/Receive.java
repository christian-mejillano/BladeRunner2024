import java.net.DatagramPacket;
import java.net.DatagramSocket;
public class Receive implements Runnable{

    private DatagramSocket socket;

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
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + receivedMessage);
           
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
