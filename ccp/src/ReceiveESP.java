import java.net.*;
import java.util.Date;
import org.json.simple.*;
import org.json.simple.parser.*;

public class ReceiveESP extends Receive{
    //Unique information sent by the ESP
    //status if OFF by default
    private String status = "OFF";
    private String station_id;
    private String intended_light_colour;
    private String actual_light_colour;

    //Constructor
    public ReceiveESP(DatagramSocket socket){
        super(socket);
    }

    @Override
    public void run() {
        //Call super.run so that the updateCommonValues() can be run and all the common variables are upadted
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
            //Update all local variables
            if(jsonMessage.get("status") != null){
                setStatus((String) jsonMessage.get("status"));
            }
            if(jsonMessage.get("station_id") != null){
                setStationID((String) jsonMessage.get("station_id"));
            }
            if(jsonMessage.get("light_colour") != null){
                setActualLightColour((String) jsonMessage.get("light_colour"));
            }
            // Set the flag to false
            setReceivedMessage(false);
        }
    }

    //Getters and Setters
    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getStationID(){
        return station_id;
    }

    public void setStationID(String station_id){
        this.station_id = station_id;
    }

    public String getIntendedLightColour(){
        return intended_light_colour;
    }

    public void setIntendedLightColour(String light_colour){
        this.intended_light_colour = light_colour;
    }

    public String getActualLightColour(){
        return actual_light_colour;
    }

    public void setActualLightColour(String actual_light_colour){
        this.actual_light_colour = actual_light_colour;
    }
}
