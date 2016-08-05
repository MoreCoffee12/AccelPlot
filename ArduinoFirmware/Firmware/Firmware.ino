// Code to acquire acceleration data and transmit it wirelessly
// from a piston

// Software serial port for the Bluetooth communications
#include <SoftwareSerial.h>
SoftwareSerial BTSerial(10, 11); // RX | TX

// I2Cdev and MPU6050 must be installed as libraries, or else the .cpp/.h files
// for both classes must be in the include path of your project
#include "I2Cdev.h"
#include "MPU6050.h"

// Arduino Wire library is required if I2Cdev I2CDEV_ARDUINO_WIRE implementation
// is used in I2Cdev.h
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include "Wire.h"
#endif

// class default I2C address is 0x68
// specific I2C addresses may be passed as a parameter here
// AD0 low = 0x68 (default for InvenSense evaluation board)
// AD0 high = 0x69
MPU6050 accelgyro;
//MPU6050 accelgyro(0x69); // <-- use for AD0 high

// uncomment "OUTPUT_READABLE_ACCELGYRO" if you want to see a tab-separated
// list of the accel X/Y/Z and then gyro X/Y/Z values in decimal. Easy to read,
// not so easy to parse, and slow(er) over UART.
//#define OUTPUT_READABLE_ACCELGYRO

// uncomment "OUTPUT_BINARY_ACCELGYRO" to send all 6 axes of data as 16-bit
// binary, one right after the other. This is very fast (as fast as possible
// without compression or data loss), and easy to parse, but impossible to read
// for a human.
#define OUTPUT_BINARY_ACCELGYRO


#define LED_PIN 13

// Globals
bool blinkState = false;
byte btTemp = 0;
unsigned short iX_Accel;
unsigned short iY_Accel;
unsigned char iAddress;
unsigned long timeLast;
unsigned int iBytesReturned;
unsigned int iIdx;

// Setup, runs once
void setup() 
{
    // join I2C bus (I2Cdev library doesn't do this automatically)
    #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
        Wire.begin();
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif
    
    // Initialize bluetooth
    BTSerial.begin(57600);
    delay(50);
  
    // initialize serial communication
    // (38400 chosen because it works as well at 8MHz as it does at 16MHz, but
    // it's really up to you depending on your project)
    Serial.begin(57600);
    delay(50);
    
    // initialize device
    Serial.println("Initializing I2C devices...");
    accelgyro.initialize();

    // verify connection
    Serial.println("Testing device connections...");
    Serial.println(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");
    
    // Configure the gyro
    accelgyro.setDLPFMode(MPU6050_DLPF_BW_256);
    accelgyro.setFullScaleAccelRange(MPU6050_ACCEL_FS_2);
    
    Serial.flush();

    // configure Arduino LED for
    pinMode(LED_PIN, OUTPUT);
    
    // Reset some of the variables associated with the MinSegBus
    iBytesReturned = 0;
    
    // Update thet time
    timeLast = micros();
   
}

void loop() 
{
  // To get this to fit in an Uno the timing will have to be polling, rather
  // than interrupts.
  timeLast = micros();
  
  // Get the value from the MPU-6050 accelerometer
  iX_Accel=accelgyro.getAccelerationX();
  iY_Accel=accelgyro.getAccelerationY();

  // Serial.println("Got new readings");
  
  // X_Accel, address 0x0000
  iX_Accel = (iX_Accel >> 4);
  BTSerial.write((byte)iX_Accel);
  //Serial.print((byte)iX_Accel);
  btTemp = (byte)(iX_Accel>>8);
  BTSerial.write(btTemp);
  //Serial.print(btTemp);
  //Serial.print("\n");
  
  // Y_Accel, address 0x0001
  iY_Accel = (iY_Accel >> 4);
  BTSerial.write((byte)iY_Accel);
  btTemp = (byte)(iY_Accel>>8);
  btTemp |= 1 << 4;
  BTSerial.write(btTemp);
  
  // blink LED to indicate activity
  blinkState = !blinkState;
  digitalWrite(LED_PIN, blinkState);
  
  // Do nothing until the desired time has elapsed
  delayMicroseconds(3000-(micros()-timeLast));
}


