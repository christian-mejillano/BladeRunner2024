import java.net.DatagramSocket;
public class CCP {
    String client_type; 
    String client_id;
    int timestamp;
    String status; 
       public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();

            Send send = new Send(socket);
            Receive receive = new Receive(socket);

            Thread receiveThread = new Thread(receive);
            receiveThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
