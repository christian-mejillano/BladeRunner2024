import java.io.*;
import java.net.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class ThreadESP extends Thread{
    private DatagramSocket socket;

    public boolean hasReceivedMessage;
    public JSONObject messageJSON; 
    public String expectedStatus;
    public String actualStatus;

    public ThreadESP(DatagramSocket socket){
        this.socket = socket;
        this.hasReceivedMessage = false;
        //Should be ERR by default when starting
        this.expectedStatus = "ERR";
        this.actualStatus = "ERR";
    }

    @Override
    public void run(){
        while(true){
            try{
                byte[] buffer = new byte[1024];
                //Create a new packet using the buffer
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try{
                    //Receive the packet
                    socket.receive(packet);
                }
                //Just need to catch the exception to satisfy the socket.receive class requirements
                catch(IOException e){}

                String messageInSocket = new String(packet.getData(), 0, packet.getLength());
                int messageByteSum = 0;
                for(int i = 0; i <= messageInSocket.getBytes().length - 1; i++){
                    messageByteSum += messageInSocket.getBytes()[i];
                }
                
                if(messageInSocket != null && messageByteSum != 0){
                    hasReceivedMessage = true;
                    messageJSON = stringToJSON(messageInSocket);

                    System.out.println("Received from ESP: " + messageInSocket);
    
                } 
            }
            
            catch(Exception e){}

            try {
                Thread.sleep(100);
            } 
            
            catch (InterruptedException e) {}
        }
    }

    public JSONObject stringToJSON(String messageToParse){
        JSONObject jsonMessage = null;
        try{
            //Parse the message and store it in jsonMessage
            JSONParser parser = new JSONParser();
            jsonMessage = (JSONObject) parser.parse(messageToParse);
        }
        //Stop running if there is a Parse Exception
        catch (ParseException e){
            e.printStackTrace();
            return new JSONObject();
        }
        return jsonMessage;
    }

    //Given a key, either return its value from jsonMessage or null if it doesn't exist
    public String getValueFromMessage(String key){
        if(messageJSON != null && messageJSON.get(key) != null){
            return (String) messageJSON.get(key);
        }
        return null;
    }
}
