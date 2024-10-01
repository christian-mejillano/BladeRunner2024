#include <WiFi.h>
#include <WiFiUDP.h>
#include <ArduinoJson.h>
#include <ESP32Servo.h>

Servo myServo;

// WiFi network name and password
const char *ssid = "ENGG2K3K";

// IP address and port to send UDP data to (Java program)
const char *udpAddress = "10.20.30.124"; // Java program IP address
const int udpPort = 3024;
IPAddress local_IP(10, 20, 30, 1);
IPAddress gateway(10, 20, 30, 1);
IPAddress subnet(255, 255, 255, 0);

// Create UDP instance
WiFiUDP udp;

unsigned long previousMillis = 0;
const long heartbeatInterval = 2000; // 2 seconds
unsigned long lastHeartbeatReceived = 0;
const long heartbeatTimeout = 5000; // 5 seconds timeout

// pinning
const int motor1 = 16;
const int motor2 = 17;
const int servoPin = 15;

const int forwardAngle = 180;  // Forward (full movement)
const int stopAngle = 90; 

void setup()
{
  Serial.begin(115200);

    myServo.attach(servoPin);

  // Connect to the WiFi network
  WiFi.config(local_IP, gateway, subnet);
  WiFi.begin(ssid);

  // Wait for connection
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
   myServo.write(forwardAngle);
   delay(1000);
   myServo.write(stopAngle);

}
void loop()
{
  unsigned long currentMillis = millis();


  if (WiFi.status() != WL_CONNECTED)
  {
    Serial.println("WiFi lost, attempting to reconnect...");
    WiFi.reconnect();
  }

  // Send heartbeat acknowledgment if heartbeat received
  // Serial.println("checking for message");
  int packetSize = udp.parsePacket();
  // Serial.print(packetSize);
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

    Serial.print("Received from server: ");
    String message = recieved["message"];
    String action = recieved["action"];
    Serial.println(message);
    Serial.print("Action from server: ");
    Serial.println(action);
    lastHeartbeatReceived = currentMillis;
    if (action == "START")
    {
      JsonDocument doc;
      doc["client_id"] = "ESP";
      doc["message"] = "FORWARD";
      doc["timestamp"] = millis() / 1000;
      doc["status"] = "OK";
      doc["station_id"] = "ST01"; //??
      doc["action"] = "move";
      String reply;
      serializeJson(doc, reply);
      udp.beginPacket(udpAddress, udpPort);
      udp.write((const uint8_t *)reply.c_str(), reply.length());
      udp.endPacket();

      Serial.println("Sent: " + reply);
      Serial.println("Sent message acknowledgment to Java program");
      myServo.write(forwardAngle);
    }
    if (action == "STOP")
    {
      JsonDocument doc;
      doc["client_id"] = "ESP";
      doc["message"] = "STOPPING";
      doc["timestamp"] = millis() / 1000;
      doc["status"] = "OK";
      doc["station_id"] = "ST01"; //??
      doc["action"] = "move";
      String reply;
      serializeJson(doc, reply);
      udp.beginPacket(udpAddress, udpPort);
      udp.write((const uint8_t *)reply.c_str(), reply.length());
      udp.endPacket();

      Serial.println("Sent: " + reply);
      Serial.println("Sent message acknowledgment to Java program");
      myServo.write(stopAngle);
    }

    if (message == "HEARTBEAT")
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

  // Check for heartbeat timeout
  if (currentMillis - lastHeartbeatReceived > heartbeatTimeout)
  {
    Serial.println("Heartbeat lost! Connection to Java program is down.");
   
    // Implement reconnection logic or alerts if needed
    lastHeartbeatReceived = currentMillis; // Reset to avoid continuous alerts
        JsonDocument doc;
    doc["client_id"] = "ESP";
    doc["message"] = "REQ_HEARTBEAT";
    doc["timestamp"] = millis() / 1000;
    doc["status"] = "OK";
    doc["station_id"] = "ST01"; //??
    doc["action"] = "STAT";
    String beat;
    serializeJson(doc, beat);
    udp.beginPacket(udpAddress, udpPort);
    udp.write((const uint8_t *)beat.c_str(), beat.length());
    udp.endPacket();

    Serial.println("Sent: " + beat);
    Serial.println("Sent check up heartbeat to Java program");
  }

  // Send heartbeat to Java program every 2 seconds
  // if (currentMillis - previousMillis >= heartbeatInterval)
  // {
  //   previousMillis = currentMillis;
    // JsonDocument doc;
    // doc["client_id"] = "ESP";
    // doc["message"] = "REQ_HEARTBEAT";
    // doc["timestamp"] = millis() / 1000;
    // doc["status"] = "OK";
    // doc["station_id"] = "ST01"; //??
    // doc["action"] = "STAT";
    // String beat;
    // serializeJson(doc, beat);
    // udp.beginPacket(udpAddress, udpPort);
    // udp.write((const uint8_t *)beat.c_str(), beat.length());
    // udp.endPacket();

    // Serial.println("Sent: " + beat);
    // Serial.println("Sent heartbeat to Java program");
  // }

  // Wait before next iteration
  delay(10);
}