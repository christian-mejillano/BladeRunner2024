import java.net.*;
import java.util.Date;

import org.json.simple.*;
import org.json.simple.parser.*;

public class ReceiveMCP extends Receive{
    public ReceiveMCP(DatagramSocket socket){
        super(socket);
    }
}
