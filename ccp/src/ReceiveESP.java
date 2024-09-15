import java.net.*;
import java.util.Date;

import org.json.simple.*;
import org.json.simple.parser.*;

public class ReceiveESP extends Receive{
    public ReceiveESP(DatagramSocket socket){
        super(socket);
    }
}
