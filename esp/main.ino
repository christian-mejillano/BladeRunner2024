#include <WiFi.h>
#include <WiFiUdp.h>
#include <ArduinoJson.h> 

/* WiFi network name and password */
const char* ssid = "abcdefg"; //?? 
const char* password = "password";

// Motor pins
const int motorPin1 = 16;
const int motorPin2 = 17;

// UDP configuration
WiFiUDP udp;
const char* udpAddress = "10.20.30.1"; 
const uint16_t udpPort = 2001;          

unsigned long previousMillis = 0;
const long interval = 10000; // interval to check WiFi connection

void setup() {
  Serial.begin(115200);
  pinMode(motorPin1, OUTPUT);
  pinMode(motorPin2, OUTPUT);

  // Connect to WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
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
  int packetSize = udp.parsePacket();
  if (packetSize) {
    char incomingPacket[512];
    int len = udp.read(incomingPacket, 512);
    if (len > 0) {
      incomingPacket[len] = '\0';
    }
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
}