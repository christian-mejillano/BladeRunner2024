#include <WiFi.h>
#include <WiFiUdp.h>

// WiFi network name and password
const char* ssid = "ENGG2K3K";

// IP address and port to send UDP data to (Java program)
const char* udpAddress = "10.20.30.124"; // Java program IP address
const uint16_t udpPort = 3024;
IPAddress local_IP(10,20,30,1);
IPAddress gateway(10,20,30,1);
IPAddress subnet(255,255,255,0);

// Create UDP instance
WiFiUDP udp;
const char* udpAddress = "10.20.30.1"; 
const uint16_t udpPort = 2001;          

unsigned long previousMillis = 0;
const long heartbeatInterval = 2000; // 2 seconds
unsigned long lastHeartbeatReceived = 0;
const long heartbeatTimeout = 5000; // 5 seconds timeout

void setup() {
  Serial.begin(115200);
  pinMode(motorPin1, OUTPUT);
  pinMode(motorPin2, OUTPUT);

  // Connect to the WiFi network
  WiFi.config(local_IP,gateway,subnet);
  WiFi.begin(ssid);

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println();
  Serial.println("Connected to WiFi");
  Serial.println(WiFi.localIP());

  // Initialse UDP
  udp.begin(udpPort);
  Serial.println("UDP client started");
}


void loop() {
  unsigned long currentMillis = millis();

  // Send heartbeat acknowledgment if heartbeat received
  int packetSize = udp.parsePacket();
  if (packetSize) {
    char incomingPacket[512];
    int len = udp.read(incomingPacket, 512);
    if (len > 0) {
      incomingPacket[len] = '\0';
    }
    String receivedMessage = String(incomingPacket);
    Serial.print("Received from server: ");
    Serial.println(receivedMessage);

    if (receivedMessage == "HEARTBEAT") {
      // Send acknowledgment back to Java program
      String ackMessage = "HEARTBEAT_ACK";
      udp.beginPacket(udp.remoteIP(), udp.remotePort());
      udp.write((const uint8_t*)ackMessage.c_str(), ackMessage.length());
      udp.endPacket();
      Serial.println("Sent heartbeat acknowledgment to Java program");

    // handle the action from the CCP JSON packet
    const char* action = doc["action"];
    if (strcmp(action, "FORWARD") == 0) {
      handleForward();
    } else if (strcmp(action, "BACKWARD") == 0) {
      handleBackward();
    } else if (strcmp(action, "STOP") == 0) {
      handleStop();
    }
  }
}

  // Check for heartbeat timeout
  if (currentMillis - lastHeartbeatReceived > heartbeatTimeout) {
    Serial.println("Heartbeat lost! Connection to Java program is down.");
    // Implement reconnection logic or alerts if needed
    lastHeartbeatReceived = currentMillis; // Reset to avoid continuous alerts
  }

  // Send heartbeat to Java program every 2 seconds
  if (currentMillis - previousMillis >= heartbeatInterval) {
    previousMillis = currentMillis;
    String heartbeatMessage = "HEARTBEAT";
    udp.beginPacket(udpAddress, udpPort);
    udp.write((const uint8_t*)heartbeatMessage.c_str(), heartbeatMessage.length());

    udp.endPacket();
    Serial.println("Sent heartbeat to Java program");
  }

  // Wait before next iteration
  delay(10);
}