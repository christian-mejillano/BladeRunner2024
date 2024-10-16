import java.io.IOException;
import java.net.*;
import java.util.*;
import org.json.simple.*;

public class SendToESP {
    //Variables for Send class
    private DatagramSocket socket;
    public int sendingSequenceNumber;
    public static String client_type = "ccp";
    public static String client_id = "BR24";
}
