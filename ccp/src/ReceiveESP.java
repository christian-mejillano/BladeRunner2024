import java.net.*;
import java.util.Date;

import org.json.simple.*;
import org.json.simple.parser.*;

public class ReceiveESP extends Receive{
    private String status;
    private String station_id;
    private String light_colour;

    public ReceiveESP(DatagramSocket socket){
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

            if(jsonMessage.get("status") != null){
                setStatus((String) jsonMessage.get("status"));
            }
            if(jsonMessage.get("station_id") != null){
                setStationID((String) jsonMessage.get("station_id"));
            }
        }
    }

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

    public String getLightColour(){
        return light_colour;
    }

    public void setLightColour(String light_colour){
        this.light_colour = light_colour;
    }
}
