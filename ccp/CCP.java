import java.net.DatagramSocket;

public class CCP {
    String client_type;
    String client_id;
    int timestamp;
    String status;

    public static void main(String[] args) {
        final String MCP_IP_ADDRESS = "10.20.30.1";
        final int MCP_PORT = 2001;
        final int ESP32_PORT = 4500; //CHANGE IF NEEDED 
//Set up MCP and ESP32 connection 
//boolean connected
        try {
            DatagramSocket socket = new DatagramSocket();

            Send send = new Send(socket);
            Receive receive = new Receive(socket);

            send.sendMessage("Initial message from CCP", MCP_IP_ADDRESS, MCP_PORT);

            Thread receiveThread = new Thread(receive);
            Thread.sleep(500);

            while (true) {
                if (receive.hasReceivedMessage()) {
                    String message = receive.getMessage();
                    if (receive.getClientType().equals("MCP")) {
                        send.sendMessage(message, "ESP32_IP_Address", ESP32_PORT);
                    } else if (receive.getClientType().equals("ESP")) {
                        send.sendMessage(message, MCP_IP_ADDRESS, MCP_PORT);
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}