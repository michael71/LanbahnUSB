/*

model 1..4 turnouts (not just a single one!)

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
#include "Turnouts.h"

// create 1 turnouts
Turnouts::Turnouts(uint8_t t0) {
   // pins for output
   _pin[0] = t0;
   _npins = 1;
}

// create 2 turnouts
Turnouts::Turnouts(uint8_t t0, uint8_t t1) {
   // pins for output
   _pin[0] = t0;
   _pin[1] = t1;
   _npins = 2;
}

// create 3 turnouts
Turnouts::Turnouts(uint8_t t0, uint8_t t1, uint8_t t2) {
   // pins for output
   _pin[0] = t0;
   _pin[1] = t1;
   _pin[2] = t2;
   _npins = 3;

}

// create 4 turnouts
Turnouts::Turnouts(uint8_t t0, uint8_t t1, uint8_t t2, uint8_t t3) {
   // pins for output
   _pin[0] = t0;
   _pin[1] = t1;
   _pin[2] = t2;
   _pin[3] = t3;
   _npins = 4;
}

// initialize all turnouts to "CLOSED"
void Turnouts::init(uint8_t en) {
  for (int i =0; i <_npins; i++) {
     pinMode(_pin[i], OUTPUT);
     _value[i]= CLOSED;  
     digitalWrite(_pin[i], _value[i]);
     _state[i]= CLOSED;
  }
  pinMode(en, OUTPUT);
  digitalWrite(en, HIGH);  //enable the LM1909's
}

// return current state of turnout n
uint8_t Turnouts::set(uint8_t n, uint8_t v) {
  if  ((n <_npins) // n is always >=0
      &&
      ((v == CLOSED) || (v == THROWN))
      ) {
      _value[n] = v;
      digitalWrite(_pin[n], _value[n]);
      _state[n] = _value[n];
      return _state[n];  // executed.
  } else {
      // cannot interpret command
      return INVALID;
  }
}

uint8_t Turnouts::get(uint8_t n) {
  return _state[n];
}

