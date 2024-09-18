#include <WiFi.h>
#include <WiFiUdp.h>
<<<<<<< HEAD

// WiFi network name and password
const char *ssid = "ENGG2K3K";

// IP address and port to send UDP data to (Java program)
const char *udpAddress = "10.20.30.124"; // Java program IP address
const uint16_t udpPort = 3024;
IPAddress local_IP(10, 20, 30, 1);
IPAddress gateway(10, 20, 30, 1);
IPAddress subnet(255, 255, 255, 0);

// Create UDP instance
=======
#include <ArduinoJson.h> 

/* WiFi network name and password */
const char* ssid = "abcdefg"; //?? 
const char* password = "password";

// Motor pins
const int motorPin1 = 16;
const int motorPin2 = 17;

// UDP configuration
>>>>>>> d280b38ad5f23c90efd573aad57ada4c9a474aa4
WiFiUDP udp;
const char *udpAddress = "10.20.30.1";
const uint16_t udpPort = 2001;

unsigned long previousMillis = 0;
const long heartbeatInterval = 2000; // 2 seconds
unsigned long lastHeartbeatReceived = 0;
const long heartbeatTimeout = 5000; // 5 seconds timeout

// adding sensor pins
const int triggerPin = 5;
const int echoPin = 6;

void setup()
{
  Serial.begin(115200);
  pinMode(motorPin1, OUTPUT);
  pinMode(motorPin2, OUTPUT);

<<<<<<< HEAD
  // Connect to the WiFi network
  WiFi.config(local_IP, gateway, subnet);
  WiFi.begin(ssid);

  // Wait for connection
<<<<<<< HEAD
=======
  // Connect to WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
>>>>>>> d280b38ad5f23c90efd573aad57ada4c9a474aa4
  while (WiFi.status() != WL_CONNECTED) {
=======
  while (WiFi.status() != WL_CONNECTED)
  {
>>>>>>> 98352da62039183d1c706feb82711aed14303236
    delay(1000);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("Connected to WiFi");
  Serial.println(WiFi.localIP());

  // Initialse UDP
  udp.begin(udpPort);
  Serial.println("UDP client started");

  // initialse sensors
  pinMode(triggerPin, OUTPUT);
  pinMode(echoPin, INPUT);
}

<<<<<<< HEAD
<<<<<<< HEAD

void loop() {
=======
void loop()
{
>>>>>>> 98352da62039183d1c706feb82711aed14303236
  unsigned long currentMillis = millis();

  // Send heartbeat acknowledgment if heartbeat received
=======
void loop() {
  // send a JSON status message to CCP
  sendJsonStatus();

  // check for a response from CCP and handle it
  receiveJsonFromCCP();

  // check WiFi connection and reconnect if needed
  checkWiFiConnection();

  delay(1000);
}

// sends a JSON packet to CCP
void sendJsonStatus() {
  StaticJsonDocument<256> doc;
  doc["client_id"] = "ESP";
  doc["message"] = "status_update";
  doc["timestamp"] = millis() / 1000;
  doc["status"] = "OK";
  doc["station_id"] = "ST01"; //?? 
  doc["action"] = "STAT"; 

  String jsonString;
  serializeJson(doc, jsonString);

  // send JSON data to the server
  udp.beginPacket(udpAddress, udpPort);
  udp.write((const uint8_t*)jsonString.c_str(), jsonString.length());
  udp.endPacket();

  Serial.println("Sent JSON to CCP: ");
  Serial.println(jsonString);
}

// receives a JSON packet from CCP and handle it
void receiveJsonFromCCP() {
>>>>>>> d280b38ad5f23c90efd573aad57ada4c9a474aa4
  int packetSize = udp.parsePacket();
  if (packetSize)
  {
    char incomingPacket[512];
    int len = udp.read(incomingPacket, 512);
    if (len > 0)
    {
      incomingPacket[len] = '\0';
    }
<<<<<<< HEAD
    String receivedMessage = String(incomingPacket);
    Serial.print("Received from server: ");
    Serial.println(receivedMessage);

    if (receivedMessage == "HEARTBEAT")
    {
      // Send acknowledgment back to Java program
      String ackMessage = "HEARTBEAT_ACK";
      udp.beginPacket(udp.remoteIP(), udp.remotePort());
      udp.write((const uint8_t *)ackMessage.c_str(), ackMessage.length());
      udp.endPacket();
      Serial.println("Sent heartbeat acknowledgment to Java program");
=======
    Serial.print("Received JSON from CCP: ");
    Serial.println(incomingPacket);

    // parse the incoming JSON
    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, incomingPacket);
    if (error) {
      Serial.print("Failed to parse incoming JSON: ");
      Serial.println(error.c_str());
      return;
    }
>>>>>>> d280b38ad5f23c90efd573aad57ada4c9a474aa4

      // handle the action from the CCP JSON packet
      const char *action = doc["action"];
      if (strcmp(action, "FORWARD") == 0)
      {
        handleForward();
      }
      else if (strcmp(action, "BACKWARD") == 0)
      {
        handleBackward();
      }
      else if (strcmp(action, "STOP") == 0)
      {
        handleStop();
      }
    }
  }

<<<<<<< HEAD
  // Check for heartbeat timeout
  if (currentMillis - lastHeartbeatReceived > heartbeatTimeout)
  {
    Serial.println("Heartbeat lost! Connection to Java program is down.");
    // Implement reconnection logic or alerts if needed
    lastHeartbeatReceived = currentMillis; // Reset to avoid continuous alerts
  }

  // Send heartbeat to Java program every 2 seconds
  if (currentMillis - previousMillis >= heartbeatInterval)
  {
    previousMillis = currentMillis;
    String heartbeatMessage = "HEARTBEAT";
    udp.beginPacket(udpAddress, udpPort);
    udp.write((const uint8_t *)heartbeatMessage.c_str(), heartbeatMessage.length());

    udp.endPacket();
    Serial.println("Sent heartbeat to Java program");
  }

  // send sensor distance over udp
  float distance = getDistance();

  Serial.print("Distance: ");
  Serial.print(distance);
  Serial.println(" cm");
  
  String distanceMsg = "DISTANCE:" + String(distance);
  udp.beginPacket(udpAddress, udpPort);
  udp.write((const uint8_t *)distanceMsg.c_str(), distanceMsg.length());
  udp.endPacket();
  Serial.println("Sent distance data to Java program");

  // Wait before next iteration
  delay(10);
<<<<<<< HEAD
=======
void handleForward() {
  digitalWrite(motorPin1, HIGH);
  digitalWrite(motorPin2, LOW);
  Serial.println("Motor moving forward");
}

void handleBackward() {
  digitalWrite(motorPin1, LOW);
  digitalWrite(motorPin2, HIGH);
  Serial.println("Motor moving backward");
}

void handleStop() {
  digitalWrite(motorPin1, LOW);
  digitalWrite(motorPin2, LOW);
  Serial.println("Motor stopped");
}

// check and reconnect WiFi
void checkWiFiConnection() {
  unsigned long currentMillis = millis();
  if ((WiFi.status() != WL_CONNECTED) && (currentMillis - previousMillis >= interval)) {
    Serial.println("Reconnecting to WiFi...");
    WiFi.disconnect();
    WiFi.reconnect();
    previousMillis = currentMillis;
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("Reconnected to WiFi.");
    }
  }
>>>>>>> d280b38ad5f23c90efd573aad57ada4c9a474aa4
}
=======
}

// measures the distance to an object in cm
float getDistance()
{
  digitalWrite(triggerPin, LOW);
  delayMicroseconds(2);

  // send a pulse
  digitalWrite(triggerPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(triggerPin, LOW);

  // measure the echo time in microseconds
  long duration = pulseIn(echoPin, HIGH);

  // calculate the distance in cm
  float distance = (duration * 0.0343) / 2; // speed of sound = 343 m/s
  return distance;
}
>>>>>>> 98352da62039183d1c706feb82711aed14303236
