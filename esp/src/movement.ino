// #include <SoftwareSerial.h>

// #define MAXSPEED 10
// #define SPEEDINC 2

// class Movement  {
//   private:
//     int speed;
//     const int MaxSpeed = 10; //these two values cannot be changed

// public:
//     Movement() {
//       speed = 0;
//     }
 
// void setSpeed(int s) { // doesnt exceed max speed
//       if (s > MAXSPEED) {
//         speed = MAXSPEED;
//       } else {
//         speed = s;
//       }
//  }

// void forward() { // had to assume motorForward exists
//     motor.write(speed);

// }
// void stop() {
//       speed = 0; // had to assume motorForward exists
//       motor.write(speed); 
// }
   
   
// void acceleration() {
//     if (speed < MaxSpeed ){
//         speed = speed + SPEEDINC;
//     }
// }

// };
