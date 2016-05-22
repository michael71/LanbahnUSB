/*
 *  Copyright:  Michael Blank - 2016 - lanbahn.net

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
package net.lanbahn.lanbahnusbcontrol;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import java.util.ArrayList;

import static net.lanbahn.lanbahnusbcontrol.LanbahnUSBControl.*;

/**
 * Test program for lanbahn-usb-0.1 control using pi4j lib
 *
 * An accessory decoder is attached to an USB port. Lanbahn "SET" or "READ"
 * messages are forwarded to the USB port, if the address matches
 *
 * Feedback messages ("F ..." or "FB ...." or "FS ...." ) are received at the
 * USB port and forwarded to the LANBAHN network (added to sendQueue)
 *
 * @author Michael Blank
 */
public class AccessoryDecoder {

    public Serial usbport = SerialFactory.createInstance();
    private ArrayList<Integer> addresses = new ArrayList();
    private boolean ready;  
    // = ready when successfully parsed arduino announce string, i.e. when 
    //   we have stored all the addresses of the decoder

    AccessoryDecoder(String dev) {
        usbport.addListener(new SerialDataListener() {
            // print out the data received to the console
            @Override
            public void dataReceived(SerialDataEvent event) {
                String input = event.getData().toUpperCase();
                String[] lines = input.split("\n");  // in case the arduino has
                // sent multiple feedbacks
                for (String line : lines) {
                    parseReceivedCommand(line);
                }
            }
        });

        ready = false;
        // try to open the serial port and try to get arduino ID string
        try {
            usbport.open(dev, 57600);
            int count = 0;
            usbport.writeln("ID");  // request to send ID of decoder

        } catch (SerialPortException ex) {
            //System.out.println("Error: " + ex.getMessage());
            // ignore - we will identify a connection by reading announce string
        }

    }

    /**
     * interpret commands received from the arduino (via USB)
     *
     * @param cmd
     */
    private void parseReceivedCommand(String cmd) {
        // accept only strings starting with a character A..Z
        // and ending with "\n"

        if (DEBUG) {
            System.out.println("rec:  " + cmd);
        }
        if (cmd.length() < 3) {
            return;
        }
        if ((ready == false) && (cmd.charAt(0) == 'A')) {  // startup behaviour
            ready = parseAnnounceString(cmd);
        }
        if (cmd.charAt(0) == 'F') {  // feedback message received
            // send all feeback messages back to lanbahn UDP multicast
            txMessageQueue.offer(cmd);  // 
        }
    }

    public boolean isReady() {
        return ready;
    }

    public boolean set(int a, int val) {
        if (this.hasAddress(a)) {  // "doublecheck"
            usbport.writeln("SET " + a + " " + val);
            if (DEBUG_USB) {
                System.out.println("sent to usb: SET " + a + " " + val);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean sendMessage(String s) {
        // send only valid and matching messages to usbport
        String msgParts[] = s.toUpperCase().split(" ");
        if (msgParts.length < 2) return false;
        
        if ((msgParts[0].equals("SET") && (msgParts.length == 3))
                || (msgParts[0].equals("READ") && (msgParts.length == 2))) {
            int a = Integer.parseInt(msgParts[1]);
            if (this.hasAddress(a)) {  // check if we have an address match
                usbport.writeln(s);
                if (DEBUG_USB) {
                     System.out.println("sent to usb: " + s);
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean hasAddress(int a) {
        if (addresses.contains(a)) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Integer> getAllAddresses() {
        return addresses;
    }

    private boolean parseAnnounceString(String s) {

        boolean success = false;
        String[] parsed = s.split(" ");
        if (DEBUG) System.out.println("parsing announce string");
        for (int i = 1; i < parsed.length; i++) {
            String[] p2 = parsed[i].split(":");
            if (p2.length == 2) {
                // address:type pair received
                int addr;
                try {
                    addr = Integer.parseInt(p2[0]);
                    if (!addresses.contains(addr)) {
                        //if (DEBUG) {
                        //    System.out.println("adding " + addr);
                        //}
                        addresses.add(addr);
                        success = true;
                    }
                } catch (NumberFormatException e) {
                    ;
                }
            }
        }
        return success;
    }
}
