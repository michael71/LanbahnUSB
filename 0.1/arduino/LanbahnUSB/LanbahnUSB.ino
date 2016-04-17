/*
 Lanbahn-USB

 This program sends its ID as long as there was no command
 from the PC, then responds to commands like "SET 710 2" (set
 address 710 to a value of 2)

 *  Hardware = Arduino Micro (with USB)

 *  Created on: 06 Mar 2016
 *  Changed on: see hwConfig.h file !
 *  Copyright:  Michael Blank
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */
#include <Arduino.h> 
#include "Signal.h"
#include "Turnouts.h"
#include "hwConfig.h"

#define BUF_LEN    80


//======= take configuration from hwConfig. h ================================


//================== config end ===============================================

long timer = 0;
long greenTime = 0;
uint8_t count = 0;   // for chars in buffer
char buf[BUF_LEN];
String last_gbm = "S--------";

/* initialize */
void setup() {
  sigs[0].init();
  sigs[1].init();
  turnouts.init(enablePin);
  pinMode(greenLed, OUTPUT);

  Serial1.begin(57600);   // TOTI input
  
  // start USB serial port and wait for port to open:
  Serial.begin(57600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  Serial.println(ident);
}

uint8_t sendFeedbackTurnout(uint8_t n) {
  if (n >= N_TURNOUTS) return INVALID;
  Serial.print(turnouts_feedback);
  Serial.print((char)(n+'0')); 
  Serial.print(" "); 
  uint8_t ret = turnouts.get(n);
  Serial.println(ret);
  return ret;
}

uint8_t sendFeedbackSignal(uint8_t n) {
  if (n >= N_SIGNALS) return INVALID;
  Serial.print(sig_feedback);
  Serial.print((char)(n+'0'));
  Serial.print(" "); 
  uint8_t ret = sigs[n].get();
  Serial.println(ret);
  return ret;
}

uint8_t sendFeedbackToti() {
  Serial.print(toti_feedback);
  Serial.print(" ");
  Serial.println(last_gbm);
  return 0;  // always valid
}

void loop() {
  sigs[0].process();   // for led fading
  sigs[1].process();

  if (Serial1.available() > 0) {  // gleisbelegtmelder input
    String gbm = Serial1.readStringUntil('\n');
    if (gbm != "") {
       Serial.print(toti_feedback);
       Serial.print(" ");
       Serial.println(gbm);
       last_gbm = gbm;   // store for later reading
    }
  }
  // if we get a valid byte, interpret command:
  if (Serial.available() > 0) {
    // switch on green LED for 1 second
    digitalWrite(greenLed, HIGH);
    greenTime = millis();
    // get incoming byte:
    char c = Serial.read();
    if ( c != '\n') {
      buf[count] = c;
      if (count < BUF_LEN -1) count++;
      
    } else { //command completed
       buf[count] = 0;
       String s(buf);
       uint8_t ret = INVALID;  // set to current value when we understand the command
                               // and can execute it on this hardware
       if ( s.startsWith("ID") ) {
         Serial.println(ident);
         timer = millis();
         ret = 0;
       } else if (s.startsWith(sig_cmd)) {
          // matching address, get value
          if (s.length() > (sig_cmd.length() +2) ) {
            uint8_t n = s.charAt(sig_cmd.length())-'0';
            uint8_t i = s.charAt(sig_cmd.length()+2)-'0';
            if (n < N_SIGNALS) {  // to avoid pointer error 
               ret = sigs[n].set(i);
               sendFeedbackSignal(n);
            }
          }  
        } else if (s.startsWith(turnouts_cmd)) {
          // matching address, get turnout number and value
          if (s.length() > (turnouts_cmd.length() +2) ) {
            uint8_t n = s.charAt(turnouts_cmd.length())-'0';
            uint8_t i = s.charAt(turnouts_cmd.length()+2)-'0';         
            ret = turnouts.set(n,i);
            sendFeedbackTurnout(n);
          }  
       }  else if (s.startsWith(sig_read)) {
            // read last state of signal and send to serial
            if (s.length() > sig_read.length()) {         
              uint8_t n = s.charAt(sig_read.length())-'0';
              ret = sendFeedbackSignal(n);
            }      
       } else if (s.startsWith(turnouts_read)) {
           if (s.length() > turnouts_read.length() ) {         
              uint8_t n = s.charAt(turnouts_read.length())-'0';
              ret = sendFeedbackTurnout(n);
          }  
       } else if (s.startsWith(toti_read)) {
              ret = sendFeedbackToti(); 
       }
       if (ret == INVALID) {
          // we cannot interpret this command !
          Serial.print("Error: ");
          Serial.println(s);
       }
       count = 0;  // reset buf
       buf[0] = 0;
    }
    
  }
  if ((millis() - timer) >30000) {
    Serial.println(ident);
    timer = millis();
  }
  if ((millis() - greenTime) > 300) {
    digitalWrite(greenLed, LOW);
  }
}


