import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class ThreadMCP implements Runnable{
    private DatagramSocket socket;
    public boolean hasReceivedMessage;

    public ArrayList<JSONObject> lastFiveMessages;
    public JSONObject messageJSON; 
    

    public ThreadMCP(DatagramSocket socket){
        this.socket = socket;
        this.hasReceivedMessage = false;
        this.lastFiveMessages = new ArrayList<>();
    }

    @Override
    public void run(){
        while(true){
            try{
                byte[] buffer = new byte[1024];
                try{
                    //Only "receive" the packet for 500ms
                    socket.setSoTimeout(500);
                }
                catch(SocketException e){}

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
    
                //Set flag to true
                if(messageInSocket != null && messageByteSum != 0){
                    hasReceivedMessage = true;
                    messageJSON = stringToJSON(messageInSocket);

                    if(lastFiveMessages.size() == 5){
                        lastFiveMessages.remove(4);
                    }
                    lastFiveMessages.add(0, messageJSON);

                    System.out.println("Received: " + messageInSocket);
    
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

    public String getValueFromMessage(String key){
        if(messageJSON != null && (String) messageJSON.get(key) != null){
            return (String) messageJSON.get(key);
        }
        return null;
    }

}
