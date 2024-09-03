// ESP32 WiFi connection to Arduino

#include "WiFi.h"

const char* ssid = "networkName";
const char* password = "networkPassword";
 
void setup() {
	
	Serial.begin(115200);
	WiFi.begin(ssid, password);

	
}

void loop() {

}
