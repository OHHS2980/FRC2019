
//
// begin license header
//
// This file is part of Pixy CMUcam5 or "Pixy" for short
//
// All Pixy source code is provided under the terms of the
// GNU General Public License v2 (http://www.gnu.org/licenses/gpl-2.0.html).
// Those wishing to use Pixy source code, software and/or
// technologies under different licensing terms should contact us at
// cmucam@cs.cmu.edu. Such licensing terms are available for
// all portions of the Pixy codebase presented here.
//
// end license header
//
// This sketch is a good place to start if you're just getting started with
// Pixy and Arduino.  This program simply prints the detected object blocks
// (including color codes) through the serial console.  It uses the Arduino's
// ICSP port.  For more information go here:
//
// http://cmucam.org/projects/cmucam5/wiki/Hooking_up_Pixy_to_a_Microcontroller_(like_an_Arduino)
//
// It prints the detected blocks once per second because printing all of the
// blocks for all 50 frames per second would overwhelm the Arduino's serial port.
//

#include <SPI.h>
#include <Pixy.h>
#include <Wire.h>
#include <PixyI2C.h>
#include <PixySPI_SS.h>

//#include <iostream>
//#include <string>


//PixyI2C pixyI2C;
PixyI2C pixyI2C(0x54); // You can set the I2C address through PixyI2C object 

// This is the main Pixy object
Pixy pixy;

const int numReadings = 10;

int readings[numReadings];      // the readings from the analog input
int readIndex = 0;              // the index of the current reading
int total = 0;                  // the running total
int average = 0;                // the average

int inputPin = A0;

void setup()
{
  Serial.begin(115200);
  Serial.print("Starting...\n");

  pixy.init();
  
  pixyI2C.init();
  
  pinMode(2, OUTPUT); // Set pin 2 as trigger pin
  digitalWrite(2, LOW); // Set trigger LOW for continuous read

  pinMode(3, INPUT); // Set pin 3 as monitor pin

  for (int thisReading = 0; thisReading < numReadings; thisReading++) {
    readings[thisReading] = 0;
  }
}

void loop()
{
  unsigned long pulseWidth = 0;
  static int i = 0;
  uint16_t blocks;
  uint16_t blocksI2C;
  //char buf[32]; //32 bits to send info
  //char buf2[64] = "64x"; //64 bits to send values
  //int h = 0;
  // grab blocks!
  blocks = pixy.getBlocks();
  blocksI2C = pixyI2C.getBlocks();
  String hold = "";

  pulseWidth = pulseIn(3, HIGH); // Count how long the pulse is high in microseconds
  if(pulseWidth != 0)
  {
    pulseWidth = pulseWidth / 10; // 10usec = 1 cm of distance
  }

  total = total - readings[readIndex];
  // read from the sensor:
  readings[readIndex] = pulseWidth;
  // add the reading to the total:
  total = total + readings[readIndex];
  // advance to the next position in the array:
  readIndex = readIndex + 1;

  // if we're at the end of the array...
  if (readIndex >= numReadings) {
    // ...wrap around to the beginning:
    readIndex = 0;
  }

  // calculate the average:
  average = total / numReadings;

  if(blocks||blocksI2C) 
  {
    i++;
    if(i%20==0) 
    { 
      for (int j = 0; j < blocks; j++)
      {
         //char[] size 23
         char buffer2[] = "P1S0X000Y000W000H000N0\n";
         
         sprintf(buffer2,"P1S%01dX%03dY%03dW%03dH%03dN%01d\n", pixy.blocks[j].signature, pixy.blocks[j].x, pixy.blocks[j].y, pixy.blocks[j].width, pixy.blocks[j].height,j);

         hold+=buffer2;
      }

      for (int j = 0; j < blocksI2C; j++)
      {
         //char[] size 22
         char buffer3[] = "P2S0X000Y000W000H000N0\n";
         
         sprintf(buffer3,"P2S%01dX%03dY%03dW%03dH%03dN%01d\n", pixyI2C.blocks[j].signature, pixyI2C.blocks[j].x, pixyI2C.blocks[j].y, pixyI2C.blocks[j].width, pixyI2C.blocks[j].height,j);

         hold+=buffer3;
      }
      
      char pulse[] = "D:000\n";
      sprintf(pulse, "D:%03d\n", average);
      //Serial.print("here3");
      
      String output;
      output = pulse+hold;
      Serial.print(output);
    }
  }
  else {
    char pulse[] = "D:000\n";
    sprintf(pulse, "D:%03d\n", average);
    Serial.print(pulse);
  }
  delay(1);
}
