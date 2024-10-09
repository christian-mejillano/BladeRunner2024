#include <WiFi.h>
#include <WiFiUDP.h>
#include <ArduinoJson.h>
#include <ESP32Servo.h>

Servo myServo;

// TO CHANGE PER BLADERUNNER
const String br = "BR24";
const int udpPort = 3024;
const char *udpAddress = "10.20.30.124"; // Java program IP address

// IP address and port to send UDP data to (Java program)
IPAddress local_IP(10, 20, 30, 1);
IPAddress gateway(10, 20, 30, 1);
IPAddress subnet(255, 255, 255, 0);

// Bladerunner status
String currStat = "STOPPPED";
String currColour = "RED";
String currDoor = "CLOSED";

// target status
String targetStat = currStat;
String targetColour = currColour;
String targetDoor = currDoor;

// connection stream
int SEQ_NUM = 0;

// WiFi network name and password
const char *ssid = "ENGG2K3K";

// Create UDP instance
WiFiUDP udp;

// heartbeat timmer
unsigned long previousMillis = 0;
const long heartbeatInterval = 500; // 2 seconds
unsigned long lastHeartbeatReceived = 0;
const long heartbeatTimeout = 1000; // 5 seconds timeout
unsigned long currentMillis = 0;

// PINNING

// motor NOT FINALISED
const int MOTOR_PIN_1 = 19;
const int MOTOR_PIN_2 = 21;
const int MOTOR_SPEED_PIN = 27;

// servo
const int SERVO_PIN = 5;

// ultrasonic sensor 1
const int FRONT_TRIG_PIN = 25;
const int FRONT_ECHO_PIN = 26;

// ultrasonic sensor 2
const int BACK_TRIG_PIN = 32;
const int BACK_ECHO_PIN = 33;

// phototransistor
const int PHOTO_PIN = 18;

// lighting
const int RED_PIN = 4;
const int YELLOW_PIN = 16;
const int GREEN_PIN = 17;

void setup()
{
  Serial.begin(115200);

  // sensor pinning
  myServo.attach(SERVO_PIN);
  pinMode(FRONT_TRIG_PIN, OUTPUT);
  pinMode(FRONT_ECHO_PIN, INPUT);
  pinMode(BACK_TRIG_PIN, OUTPUT);
  pinMode(BACK_ECHO_PIN, INPUT);
  pinMode(PHOTO_PIN, INPUT_PULLUP);
  pinMode(RED_PIN, OUTPUT);
  pinMode(YELLOW_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(MOTOR_PIN_1, OUTPUT);
  pinMode(MOTOR_PIN_2, OUTPUT);
  pinMode(MOTOR_SPEED_PIN, OUTPUT);

  // stop motors and set status to stopped
  // close doors and set doors to closed
  // set led to red and currLED to red
  analogWrite(MOTOR_SPEED_PIN, 0);

  // Connect to the WiFi network
  WiFi.config(local_IP, gateway, subnet);
  WiFi.begin(ssid);

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  // debugging
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  // Initialize UDP
  Serial.print("UDP Port: ");
  Serial.println(udpPort);
  if (udp.begin(udpPort))
  {
    Serial.println("UDP started successfully.");
  }
  else
  {
    Serial.println("Failed to start UDP.");
  }
  udp.flush();
  Serial.println("End of setup");

  // start communication
  connect();
}

void loop()
{
  currentMillis = millis();

  // Continual wifi connection check
  if (WiFi.status() != WL_CONNECTED)
  {
    Serial.println("WiFi lost, attempting to reconnect...");
    WiFi.reconnect();
  }

  // message recieving
  recieve();

  // processing movement
  processMovement();
  processLighting();
  processDoor();

  // Check for heartbeat timeout
  if (currentMillis - lastHeartbeatReceived > heartbeatTimeout)
  {
    Serial.println("Heartbeat lost! Connection to Java program is down.");
    connect();
  }

  // Wait before next iteration
  delay(10);
}

void recieve()
{
  int packetSize = udp.parsePacket();
  if (packetSize != 0)
  {
    JsonDocument recieved;
    char incomingPacket[255];
    int len = udp.read(incomingPacket, 255);
    if (len > 0)
    {
      incomingPacket[len] = '\0';
    }
    deserializeJson(recieved, incomingPacket);

    String message = recieved["message"];
    String action = recieved["action"];
    lastHeartbeatReceived = currentMillis;
    SEQ_NUM = recieved["sequence"];

    // debugging
    {
      Serial.print("Received from server: ");
      Serial.println(message);
      Serial.print("Action from server: ");
      Serial.println(action);
    }

    if (message == "RQSTAT")
    {
      JsonDocument doc;
      doc["client_type"] = "CCP";
      doc["message"] = "STAT";
      doc["client_id"] = BR;
      doc["status"] = currStat;
      doc["light_color"] = currColour;
      doc["door_status"] = currDoor;
      String reply;
      serializeJson(doc, reply);
      udp.beginPacket(udpAddress, udpPort);
      udp.write((const uint8_t *)reply.c_str(), reply.length());
      udp.endPacket();
    }
    else if (message == "EXEC")
    {
      if (action == "STOPC")
      {
        targetStat = "STOPPED";
        targetDoor = "CLOSED";
        targetColour = "FLASHING_RED";
      }
      else if (action == "STOPO")
      {
        targetStat = "STOPPED";
        targetDoor = "OPENED";
        targetColour = "FLASHING_GREEN";
      }
      else if (action == "FSLOWC")
      {
        // BR should move forward slowly and stop after
        // it has aligned itself with the IR photodiode
        // at a checkpoint or station. BR doors are to
        // remain closed. If the BR is already aligned
        // with an IR photodiode, the BR should not move.
        targetStat = "STATION";
        targetDoor = "CLOSED";
        targetColour = "YELLOW";
      }
      else if (action == "FFASTC")
      {
        // move forward fast and door closed
        targetStat = "FAST";
        targetDoor = "CLOSED";
        targetColour = "GREEN";
      }
      else if (action == "RSLOWC")
      {
        // BR should move backwards slowly and stop after
        //  it has aligned itself with the IR photodiode
        // at a checkpoint or station. BR doors are to
        // remain closed. If the BR is already aligned with
        //  an IR photodiode, the BR should not move.
        targetStat = "RSTATION";
        targetDoor = "CLOSED";
        targetColour = "FLASHING_YELLOW";
      }
      else if (action == "DISCONNECT")
      {
        // BR status LED should flash at a 2 Hz rate to
        // indicate that it is to be removed from the track.
        targetStat = "STOPPED";
        targetDoor = "CLOSED";
        targetColour = "KANYEWEST";
      }
      JsonDocument doc;
      doc["client_type"] = "CCP";
      doc["message"] = "AKEX";
      doc["client_id"] = BR;
      doc["sequence"] = SEQ_NUM;
      String reply;
      serializeJson(doc, reply);
      udp.beginPacket(udpAddress, udpPort);
      udp.write((const uint8_t *)reply.c_str(), reply.length());
      udp.endPacket();
    }
    {
      // Send acknowledgment back to Java program
      JsonDocument doc;
      doc["client_id"] = "ESP";
      doc["message"] = "STAT";
      doc["timestamp"] = millis() / 1000;
      doc["status"] = "OK";
      doc["station_id"] = "ST01"; //??
      doc["action"] = "STAT";
      String reply;
      serializeJson(doc, reply);
      udp.beginPacket(udpAddress, udpPort);
      udp.write((const uint8_t *)reply.c_str(), reply.length());
      udp.endPacket();

      Serial.println("Sent: " + reply);
      Serial.println("Sent heartbeat acknowledgment to Java program");
    }
    udp.flush();
  }
}

void processMovement()
{
  if (currStat != targetStat)
  {
    if (targetStat == "STOPPED")
    {

    }
    else if (targetStat == "STATION")
    {

    }
    else if (targetStat == "FAST")
    {

    }
    else if (targetStat == "RSTATION")
    {

    }
  }
}

void processLighting()
{
  if (currColour != targetColour)
  {
    if (targetColour == "YELLOW")
    {

    }
    else if (targetColour == "GREEN")
    {

    }
    else if (targetColour == "FLASHING_RED")
    {

    }
    else if (targetColour == "FLASHING_YELLOW")
    {

    }
    else if (targetColour == "FLASHING_GREEN")
    {

    }
    else if (targetColour == "KANYEWEST")
    {

    }
  }
}

void processDoor()
{
  if (currDoor != targetDoor)
  {
    if (targetDoor == "OPENED")
    {

    }
    else if (targetDoor == "CLOSED")
    {
      
    }
  }
}

void connect()
{
  boolean ACKED = false;
  if (!ACKED)
  {
    // sending connection
    JsonDocument doc;
    doc["client_type"] = "CCP";
    doc["message"] = "CCIN";
    doc["client_id"] = br;
    String start;
    serializeJson(doc, start);
    udp.beginPacket(udpAddress, udpPort);
    udp.write((const uint8_t *)start.c_str(), start.length());
    udp.endPacket();

    Serial.println("Sent: " + start);
    Serial.println("Sent check up heartbeat to Java program");

    // recieving packet
    int packetSize = udp.parsePacket();
    JsonDocument recieved;
    char incomingPacket[255];
    int len = udp.read(incomingPacket, 255);

    if (len > 0)
    {
      incomingPacket[len] = '\0';
    }
    deserializeJson(recieved, incomingPacket);

    // processing packet
    if (recieved["message"] == "AKIN")
    {
      ACKED = true;
      SEQ_NUM = recieved["sequence"];
      JsonDocument doc;
      doc["client_type"] = "CCP";
      doc["message"] = "AKIN";
      doc["client_id"] = br;
      doc["sequence"] = SEQ_NUM;
      String start;
      serializeJson(doc, start);
      udp.beginPacket(udpAddress, udpPort);
      udp.write((const uint8_t *)start.c_str(), start.length());
      udp.endPacket();

      Serial.println("Sent: " + start);
      Serial.println("Sent 3rd handshake to Java program");
    }
    delay(10);
  }
}