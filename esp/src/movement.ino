#include <ArduinoJson.h>

const int MOTOR_PIN_1 = 19;
const int MOTOR_PIN_2 = 21;
const int MOTOR_SPEED_PIN = 27;

void setup()
{
  Serial.begin(115200);

  // sensor pinning
  pinMode(MOTOR_PIN_1, OUTPUT);
  pinMode(MOTOR_PIN_2, OUTPUT);
  pinMode(MOTOR_SPEED_PIN, OUTPUT);

  // stop motors and set status to stopped
  // close doors and set doors to closed
  // set led to red and currLED to red
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