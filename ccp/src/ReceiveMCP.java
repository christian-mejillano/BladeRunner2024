import java.net.*;
import java.util.Date;
import org.json.simple.*;
import org.json.simple.parser.*;

public class ReceiveMCP extends Receive{
    //Unique information sent by the MCP
    private String action;

    //Constructor
    public ReceiveMCP(DatagramSocket socket){
        super(socket);
    }

    @Override
    public void run() {
        //Call super.run so that the updateCommonValues() can be run and all the common variables are updated
        super.run();
        //Update the unique variables
        updateUniqueValues();
    }

    public void updateUniqueValues(){
        //If there is a message received
        if(hasReceivedMessage()){
            JSONObject jsonMessage = stringToJSON();
            //If the message is null then return
            if(jsonMessage == null){
                return;
            }
            //Update all the local variables
            if(jsonMessage.get("action") != null){
                setAction((String) jsonMessage.get("action"));
            }
            //Set the flag to false
            setReceivedMessage(false);
        }
    }

    //Getters and Setters
    public String getAction() { 
        return action; 
    }

    public void setAction(String action){
        this.action = action;
    }
}
