// ESP32 WiFi connection to Arduino

#include <WiFi.h>
#include <WiFiUdp.h>

/* WiFi network name and password */
const char * ssid = "dd-wrt";
const char * pwd = "password";

// IP address and port to send UDP data to
const char * udpAddress = "10.20.30.1";
const uint16_t udpPort = 2001;

//Create UDP instance
WiFiUDP udp;

unsigned long previousMillis = 0;
const long interval = 10000; // interval to check WiFi connection

void setup() {
  Serial.begin(115200);

  // Connect to the WiFi network
  WiFi.begin(ssid, pwd);

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  // Initialize UDP
  udp.begin(udpPort);
}

void loop() {
  // Send data to the server
  String message = "hello world";
  udp.beginPacket(udpAddress, udpPort);
  udp.write((const uint8_t*)message.c_str(), message.length());
  udp.endPacket();

  // Wait for a response from the server
  int packetSize = udp.parsePacket();
  if (packetSize) {
    char incomingPacket[255];
    int len = udp.read(incomingPacket, 255);
    if (len > 0) {
      incomingPacket[len] = '\0';
    }
    Serial.print("Received from server: ");
    Serial.println(incomingPacket);
  }

  // Check WiFi connection and reconnect if needed
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

  // Wait for 1 second before next loop
  delay(1000);
}
