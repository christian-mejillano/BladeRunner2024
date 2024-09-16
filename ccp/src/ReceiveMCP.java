import java.net.*;
import java.util.Date;

import org.json.simple.*;
import org.json.simple.parser.*;

public class ReceiveMCP extends Receive{
    private String action;

    public ReceiveMCP(DatagramSocket socket){
        super(socket);
    }

    @Override
    public void run() {
        super.run();
        updateUniqueValues();
    }

    public void updateUniqueValues(){
        if(hasReceivedMessage()){
            JSONObject jsonMessage = stringToJSON();
        
            if(jsonMessage == null){
                return;
            }

            if(jsonMessage.get("action") != null){
                setAction((String) jsonMessage.get("action"));
            }
        }
    }

    public String getAction() { 
        return action; 
    }

    public void setAction(String action){
        this.action = action;
    }
}
