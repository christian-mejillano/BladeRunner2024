#include <SoftwareSerial.h>

// Set up SoftwareSerial on pins 2 (RX) and 3 (TX)
SoftwareSerial espSerial(2, 3); // RX, TX

void setup() {
  // Start the Serial Monitor
  Serial.begin(115200);

  // Start the ESP Serial
  espSerial.begin(115200);

  // Print a message to indicate the start of communication
  Serial.println("Waiting for messages from ESP...");
}

void loop() {
  // Check if the ESP serial has data available
  if (espSerial.available()) {
    // Read the incoming data from ESP
    String message = espSerial.readString();

    // Print the received message to the Serial Monitor
    Serial.print("Received message: ");
    Serial.println(message);
  }
}
