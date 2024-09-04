#include <SoftwareSerial.h>
class Movement  {
  private:
    int speed;
    const int MaxSpeed = 10; //these two values cannot be changed

public:
    Movement() {
      speed = 0;
    }
 
void setSpeed(int s) { // doesnt exceed max speed
      if (s > MaxSpeed) {
        speed = MaxSpeed;
      } else {
        speed = s;
      }
 }

void forward() { // had to assume motorForward exists
    motor.write(speed);

}
void stop() {
      speed = 0; // had to assume motorForward exists
      motor.write(speed); 
}
   
   
void acceleration() {
    if (speed < MaxSpeed ){
        speed = speed + 2;
    }
}

};
