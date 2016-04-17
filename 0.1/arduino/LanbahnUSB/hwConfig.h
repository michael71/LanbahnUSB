/* 

hwConfig.h

Hardware configuration file for LANBAHN-USB-0.1 Hardware

 *  Created on: 07,03.2016  
 *  Changed on: see below
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
 
#ifndef _HW_CONFIG_H
#define _HW_CONFIG_H

//---------------- begin configuration ---------------------------
#define HARDWARE   "LU0.1"    // =lanbahn-usb-0.1 hardware
                              // 2 signals and 4 turnouts
#define REV  "16APR2016"      // software revision date

// define two 4-aspect signals
#define SIG_ADDR    "71"   // first two digits of signals addresses
#define N_SIGNALS     2   // maximum 2 signals
// define which Arduino PINs are used for the signals
Signal sigs[N_SIGNALS] = { Signal(13,11,10,9), Signal(6,5,3) };

// define turnouts
#define TURNO_ADDR "81"   // first two digits of turnouts addresses
// define which Arduino PINs are used for the turnouts
Turnouts turnouts(A0, A1, A2, A3);

#define TOTI_ADDR  "910"       // address of (8 channel) train-on-track-indicator

uint8_t enablePin = 2;  // enable pin for the LB1909MC
uint8_t greenLed = A5;  // visual indicator for received commands

//---------------- end of configuration -------------------------



const String sig_cmd = "SET " SIG_ADDR;
const String sig_read = "READ " SIG_ADDR;
const String sig_feedback ="FB " SIG_ADDR;

const String turnouts_cmd = "SET " TURNO_ADDR ;     
const String turnouts_read = "READ " TURNO_ADDR ;   
const String turnouts_feedback = "FB " TURNO_ADDR ;  

const String toti_read = "READ " TOTI_ADDR ;
const String toti_feedback = "FS " TOTI_ADDR ;

const String start_of_day = "SOD" ;  // resets all outputs

// build hard- and software identification string
// 2 signals and 4 turnouts controlled by this hardware
// hardware = LU0.1 (=lanbahn-usb-0.1)
// REV = compilation date

const String ident = "A " SIG_ADDR "0:S " SIG_ADDR "1:S "
TURNO_ADDR "0:T " TURNO_ADDR "1:T " TURNO_ADDR "2:T " TURNO_ADDR "3:T "
TOTI_ADDR ":I "
HARDWARE " " REV;

#endif
