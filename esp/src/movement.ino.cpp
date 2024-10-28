# 1 "/var/folders/0p/1n8x57qd2_7gv3nfsgcq_hs80000gn/T/tmpp_c1hm1j"
#include <Arduino.h>
# 1 "/Users/christianmejillano/Desktop/uni/ENGG3000/BladeRunner2024/esp/src/movement.ino"

const int MOTOR_PIN_1 = 19;
const int MOTOR_PIN_2 = 21;
const int MOTOR_SPEED_PIN = 27;
void setup();
void loop();
void setup();
void loop();
void connect();
void recieve();
void processMovement();
void processLighting();
void processDoor();
String changeStatus();
void collisionDetection();
long getDistance(int trigPin, int echoPin);
#line 6 "/Users/christianmejillano/Desktop/uni/ENGG3000/BladeRunner2024/esp/src/movement.ino"
void setup()
{
  Serial.begin(115200);


  pinMode(MOTOR_PIN_1, OUTPUT);
  pinMode(MOTOR_PIN_2, OUTPUT);
  pinMode(MOTOR_SPEED_PIN, OUTPUT);




  analogWrite(MOTOR_SPEED_PIN, 0);
}

void loop()
{
    Serial.println("Forward fast");
    analogWrite(MOTOR_SPEED_PIN, 255);
    digitalWrite(MOTOR_PIN_1, HIGH);
    digitalWrite(MOTOR_PIN_2, LOW);

    delay(2000);

    Serial.println("Stopped");
    analogWrite(MOTOR_SPEED_PIN, 0);
    digitalWrite(MOTOR_PIN_1, LOW);
    digitalWrite(MOTOR_PIN_2, LOW);

    delay(2000);

    Serial.println("Reverse");
    analogWrite(MOTOR_SPEED_PIN, 255);
    digitalWrite(MOTOR_PIN_1, LOW);
    digitalWrite(MOTOR_PIN_2, HIGH);

    delay(2000);
}
# 1 "/Users/christianmejillano/Desktop/uni/ENGG3000/BladeRunner2024/esp/src/main.ino"







#include <WiFi.h>
#include <WiFiUDP.h>
#include <ArduinoJson.h>
#include <ESP32Servo.h>

Servo myServo;


const String br = "BR24";
const int udpPort = 12000;
const char *udpAddress = "10.20.30.1";


IPAddress local_IP(10, 20, 30, 124);
IPAddress gateway(10, 20, 30, 250);
IPAddress subnet(255, 255, 255, 0);


String currStat = "STOPPPED";
String currColour = "RED";
String currDoor = "CLOSED";

String currBrStat = "STOPC";


String targetStat = currStat;
String targetColour = currColour;
String targetDoor = currDoor;


int SEQ_NUM = 0;


const char *ssid = "ENGG2K3K";


WiFiUDP udp;


unsigned long previousMillis = 0;
const long heartbeatInterval = 500;
unsigned long lastHeartbeatReceived = 0;
const long heartbeatTimeout = 1000;
unsigned long currentMillis = 0;


const int collisionThreshold = 20;
const long collisionDetectionTime = 500;
unsigned long frontCollisionStartTime = 0;
unsigned long backCollisionStartTime = 0;




const int MOTOR_PIN_1 = 19;
const int MOTOR_PIN_2 = 21;
const int MOTOR_SPEED_PIN = 27;


const int SERVO_PIN = 5;


const int FRONT_TRIG_PIN = 25;
const int FRONT_ECHO_PIN = 26;


const int BACK_TRIG_PIN = 32;
const int BACK_ECHO_PIN = 33;


const int PHOTO_PIN = 18;


const int RED_PIN = 4;
const int YELLOW_PIN = 16;
const int GREEN_PIN = 17;

void setup()
{
  Serial.begin(115200);


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




  analogWrite(MOTOR_SPEED_PIN, 0);


  WiFi.config(local_IP, gateway, subnet);
  WiFi.begin(ssid);


  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }


  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());


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


   connect();
}

void loop()
{

  if (WiFi.status() != WL_CONNECTED)
  {
    Serial.println("WiFi lost, attempting to reconnect...");
    WiFi.reconnect();
  }


  recieve();





  processMovement();
  processLighting();
  processDoor();


  if (currentMillis - lastHeartbeatReceived > heartbeatTimeout)
  {
    Serial.println("Heartbeat lost! Connection to Java program is down.");
    connect();
  }


  delay(100);
}

void connect()
{
  boolean ACKED = false;
  while (!ACKED)
  {

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


    int packetSize = udp.parsePacket();
    JsonDocument recieved;
    char incomingPacket[255];
    int len = udp.read(incomingPacket, 255);

    if (len > 0)
    {
      incomingPacket[len] = '\0';
    }
    deserializeJson(recieved, incomingPacket);


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
    delay(2000);
  }
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


    {
      Serial.print("Received from server: ");
      Serial.println(message);
      Serial.print("Action from server: ");
      Serial.println(action);
    }

    if (message == "STRQ")
    {
      JsonDocument doc;
      doc["client_type"] = "CCP";
      doc["message"] = "STAT";
      doc["client_id"] = BR;

      doc["status"] = changeStatus();
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





        targetStat = "STATION";
        targetDoor = "CLOSED";
        targetColour = "YELLOW";
      }
      else if (action == "FFASTC")
      {

        targetStat = "FAST";
        targetDoor = "CLOSED";
        targetColour = "GREEN";
      }
      else if (action == "RSLOWC")
      {





        targetStat = "RSTATION";
        targetDoor = "CLOSED";
        targetColour = "FLASHING_YELLOW";
      }
      else if (action == "DISCONNECT")
      {


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
    udp.flush();
  }
}

void processMovement()
{
  int photoValue = digitalRead(PHOTO_PIN);
  if (currStat != targetStat)
  {
    digitalWrite(MOTOR_PIN_1, LOW);
    digitalWrite(MOTOR_PIN_2, LOW);
    if (targetStat == "STOPPED")
    {
      analogWrite(MOTOR_SPEED_PIN, 0);
      digitalWrite(MOTOR_PIN_1, LOW);
      digitalWrite(MOTOR_PIN_2, LOW);
      currStat = "STOPPED";
    }
    else if (targetStat == "STATION")
    {
      analogWrite(MOTOR_SPEED_PIN, 50);
      digitalWrite(MOTOR_PIN_1, HIGH);
      digitalWrite(MOTOR_PIN_2, LOW);
      if (photoValue == HIGH)
      {
        targetStat = "STOPPED";
        targetDoor = "CLOSED";
      }
    }
    else if (targetStat == "RSTATION")
    {
      analogWrite(MOTOR_SPEED_PIN, 50);
      digitalWrite(MOTOR_PIN_1, LOW);
      digitalWrite(MOTOR_PIN_2, HIGH);
      if (photoValue == HIGH)
      {
        targetStat = "STOPPED";
        targetDoor = "CLOSED";
      }
    }
    else if (targetStat == "FAST")
    {
      analogWrite(MOTOR_SPEED_PIN, 255);
      digitalWrite(MOTOR_PIN_1, HIGH);
      digitalWrite(MOTOR_PIN_2, LOW);
      currStat = "FAST";
    }
  }
}

void processLighting()
{
  if (currColour != targetColour)
  {
    if (targetColour == "YELLOW")
    {
      digitalWrite(YELLOW_PIN, HIGH);
      digitalWrite(RED_PIN, LOW);
      digitalWrite(GREEN_PIN, LOW);
    }
    else if (targetColour == "GREEN")
    {
      digitalWrite(YELLOW_PIN, HIGH);
      digitalWrite(RED_PIN, LOW);
      digitalWrite(GREEN_PIN, LOW);
    }
    else if (targetColour == "FLASHING_RED")
    {
      digitalWrite(RED_PIN, (millis() / 500) % 2 == 0 ? HIGH : LOW);
    }
    else if (targetColour == "FLASHING_YELLOW")
    {
      digitalWrite(YELLOW_PIN, (millis() / 500) % 2 == 0 ? HIGH : LOW);
    }
    else if (targetColour == "FLASHING_GREEN")
    {
      digitalWrite(GREEN_PIN, (millis() / 500) % 2 == 0 ? HIGH : LOW);
    }
    else if (targetColour == "KANYEWEST")
    {
      digitalWrite(RED_PIN, (millis() / 500) % 2 == 0 ? HIGH : LOW);
      digitalWrite(YELLOW_PIN, (millis() / 500) % 2 == 0 ? HIGH : LOW);
      digitalWrite(GREEN_PIN, (millis() / 500) % 2 == 0 ? HIGH : LOW);
    }
  }
}

void processDoor()
{
  if (currDoor != targetDoor)
  {
    if (targetDoor.equals("OPENED"))
    {
      myServo.write(90);
      currDoor = "OPENED";
    }
    else if (targetDoor.equals("CLOSED"))
    {
      myServo.write(0);
      currDoor = "CLOSED";
    }
  }
}

String changeStatus()
{
  if (currStat == "STOPPED") {
    if (currDoor == "OPENED") {
      if(currColour == "FLASHING_GREEN") {
        currBrStat = "STOPO";
      }
    } else if (currDoor == "CLOSED") {
      if(currColour == "FLASHING_RED") {
        currBrStat = "STOPC";
      } else if(currColour == "KANYEWEST") {
        currBrStat = "DISCONNECT";
      }
    }
  } else if (currStat == "FAST" &&currDoor == "CLOSED" && currColour == "GREEN") {
    currBrStat = "FFASTC";
  } else if (currStat == "STATION" && currDoor == "CLOSED" && currColour == "YELLOW") {
    currBrStat = "FSLOWC";
  } else if (currStat == "RSTATION" && currDoor == "CLOSED" && currColour == "FLASHING_YELLOW") {
    currBrStat = "RSLOWC";
  }
}

void collisionDetection()
{
  long frontDistance = getDistance(FRONT_TRIG_PIN, FRONT_ECHO_PIN);
  long backDistance = getDistance(BACK_TRIG_PIN, BACK_ECHO_PIN);


  if (frontDistance <= collisionThreshold)
  {
    if (frontCollisionStartTime == 0)
    {
      frontCollisionStartTime = millis();
    }
    else
    {
      if (millis() - frontCollisionStartTime > collisionDetectionTime)
      {
        Serial.println("Front Collision detected! Stopping motors.");
        targetStat = "STOPPED";
      }
    }
  }
  else
  {
    frontCollisionStartTime = 0;
  }


  if (backDistance <= collisionThreshold)
  {
    if (backCollisionStartTime == 0)
    {
      backCollisionStartTime = millis();
    }
    else
    {
      if (millis() - backCollisionStartTime > collisionDetectionTime)
      {
        Serial.println("Back Collision detected! Stopping motors.");

        targetStat = "STOPPED";
        }
    }
  }
  else
  {
    backCollisionStartTime = 0;
  }
}

long getDistance(int trigPin, int echoPin)
{
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);


  long duration = pulseIn(echoPin, HIGH);


  long distance = duration * 0.034 / 2;
  return distance;
}
# 1 "/Users/christianmejillano/Desktop/uni/ENGG3000/BladeRunner2024/esp/src/receive.ino"
# 1 "/Users/christianmejillano/Desktop/uni/ENGG3000/BladeRunner2024/esp/src/send.ino"