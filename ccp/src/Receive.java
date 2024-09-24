import java.io.IOException;
import java.net.*;
import java.util.Date;
import org.json.simple.*;
import org.json.simple.parser.*;

public class Receive implements Runnable {
    //Variables for Receive class that are shared by the MCP and ESP
    private DatagramSocket socket;
    public static String client_type = "ccp";
    public static String client_id = "BRXX";
    private String intended_client_type;
    private String intended_client_id;
    private String message;
    private String rawMessage;
    private boolean hasReceivedMessage = false;

    //Constructor
    public Receive(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //Create empty buffer
            byte[] buffer = new byte[1024];
            try{
                //Only "receive" the packet for 500ms
                socket.setSoTimeout(500);
            }
            //No need for printing any errors since this is intended and we want the program to stop after 500ms
            catch(SocketException e){}
            //Create a new packet using the buffer
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try{
                //Receive the packet
                socket.receive(packet);
            }
            //Just need to catch the exception to satisfy the socket.receive class requirements
            catch(IOException e){}
            
            //Convert recieved message to String
            String fullMessage = new String(packet.getData(), 0, packet.getLength());
            int messageByteSum = 0;
            for(int i = 0; i <= fullMessage.getBytes().length - 1; i++){
                messageByteSum += fullMessage.getBytes()[i];
            }

            //Set flag to true
            if(fullMessage != null && messageByteSum != 0){
                hasReceivedMessage = true;
                //Call updateCommonValues, update the rawMessage variable and print out the message
                setRawMessage(fullMessage);
                updateCommonValues();
                System.out.println("Received: " + fullMessage);

            } 
        } 
        //In case there are any errors print out the stack trace
        catch (Exception e) {
            System.out.println("There was some error when running the try method in the receive class.");
            e.printStackTrace();
        }
    }

    //Function that returns a JSON Object using the objects' rawMessage variable
    public JSONObject stringToJSON(){
        JSONObject jsonMessage = null;
        try{
            //Parse the message and store it in jsonMessage
            JSONParser parser = new JSONParser();
            jsonMessage = (JSONObject) parser.parse(getRawMessage());
        }
        //Stop running if there is a Parse Exception
        catch (ParseException e){
            e.printStackTrace();
            return new JSONObject();
        }
        return jsonMessage;
    }

    //Function that will update all the private variables given the local variable rawMessage
    public void updateCommonValues(){
        //Create a jsonObject using the rawMessage
        JSONObject jsonMessage = stringToJSON();
        //If the message is null then exit the function
        if(jsonMessage == null){
            return;
        }
        //Set all the private variables according to the data recieved from the packet
        if(jsonMessage.get("message") != null){
            setMessage((String) jsonMessage.get("message"));
        }
        if(jsonMessage.get("client_type") != null){
            setIntendedClientType((String) jsonMessage.get("client_type"));
        }
        if(jsonMessage.get("client_id") != null){
            setIntendedClientID((String) jsonMessage.get("client_id"));
        }
    }

    //Getters and Setters for all the private variables in this class
    public String getIntendedClientType() { 
        return intended_client_type; 
    }

    public String getIntendedClientID() { 
        return intended_client_id; 
    }

    public void setIntendedClientType(String intended_client_type){
        this.intended_client_type = intended_client_type;
    }

    public void setIntendedClientID(String intended_client_id){
        this.intended_client_id = intended_client_id;
    }

    public void setRawMessage(String rawMessage){
        this.rawMessage = rawMessage;
    }

    public String getRawMessage(){
        return rawMessage;
    }

    public String getMessage() { 
        return message; 
    }

    public void setMessage(String message){
        this.message = message;
    }

    public boolean hasReceivedMessage() { 
        return hasReceivedMessage;
    }

    public void setReceivedMessage(boolean state){
        this.hasReceivedMessage = state;
    }
}