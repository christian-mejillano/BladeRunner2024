import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class ThreadMCP extends Thread{
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

                //Create a new packet using the buffer
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try{
                    //Receive the packet
                    socket.receive(packet);
                }
                //Just need to catch the exception to satisfy the socket.receive class requirements
                catch(IOException e){
                    System.out.println("MCP thread issue line 35");
                }

                String messageInSocket = new String(packet.getData(), 0, packet.getLength());
                int messageByteSum = 0;
                for(int i = 0; i <= messageInSocket.getBytes().length - 1; i++){
                    messageByteSum += messageInSocket.getBytes()[i];
                }
    
                //Set flag to true
                if(messageInSocket != null && messageByteSum != 0){
                    hasReceivedMessage = true;
                    messageJSON = stringToJSON(messageInSocket);

                    //Keep track of last 5 messages sent. If length is 5 remove the last (oldest) JSONObject in the ArrayList
                    if(lastFiveMessages.size() == 5){
                        lastFiveMessages.remove(4);
                    }
                    //Add messageJSON to start of the ArrayList
                    lastFiveMessages.add(0, messageJSON);

                    System.out.println("Received from MCP: " + messageInSocket);
    
                } 
            }
            
            catch(Exception e){
                System.out.println("MCP thread issue line 62");
            }

            try {
                Thread.sleep(100);
            } 
            
            catch (InterruptedException e) {
                System.out.println("MCP thread issue line 70");
            }
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
        if(messageJSON != null && (String) messageJSON.get(key) != null){
            return (String) messageJSON.get(key);
        }
        return null;
    }

}
