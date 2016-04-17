/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lanbahn.lanbahnusbcontrol;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import java.util.ArrayList;
import java.util.Scanner;
import static net.lanbahn.lanbahnusbcontrol.LanbahnUSBControl.DEBUG;


import static net.lanbahn.lanbahnusbcontrol.LanbahnUSBControl.*;

/**
 * Test program for lanbahn-usb-0.1 control using pi4j lib
 *
 * @author Michael Blank
 */
public class AccessoryDecoder {

    public Serial usbport = SerialFactory.createInstance();
    private ArrayList<Integer> addresses = new ArrayList();
    private boolean ready = false;

    AccessoryDecoder(String dev) {
        usbport.addListener(new SerialDataListener() {
            // print out the data received to the console
            @Override
            public void dataReceived(SerialDataEvent event) {
                String input = event.getData().toUpperCase();
                String[] lines = input.split("\n");  // in case arduino sent
                // multiple feedbacks
                for (String line : lines) {
                    parseCommand(line);
                }
            }
        });

        // try to open the serial port and try to get arduino ID string
        try {
            usbport.open(dev, 57600);
            int count = 0;
            usbport.writeln("ID");  // request to send ID of decoder

        } catch (SerialPortException ex) {
            //System.out.println("Error: " + ex.getMessage());
        }

    }

    private void parseCommand(String cmd) {
        // accept only strings starting with a character A..Z
        // and ending with "\n"

        if (DEBUG) {
            System.out.println("rec:  " + cmd);
        }
        if (cmd.length() < 3) {
            return;
        }
        if ((ready == false) && (cmd.charAt(0) == 'A')) {
            ready = parseAnnounceString(cmd);
        } else if (cmd.charAt(0) == 'F') {
            // send all feeback messages back to lanbahn UDP multicast
            txMessageQueue.offer(cmd);  // 
        }
    }

    public boolean isReady() {
        return ready;
    }

    public boolean set(int a, int val) {
        if (this.hasAddress(a)) {
            usbport.writeln("SET " + a + " " + val);
            if (DEBUG) {
                System.out.println("sent: SET " + a + " " + val);
            }
            return true;
        } else {
            return false;
        }
    }
    
    public boolean sendMessage(String s) {
        // send only "set" messages
        String msgParts[] = s.toUpperCase().split(" ");
        if (msgParts.length != 3) return false;
        if (msgParts[0].equals("SET") || msgParts[0].equals("READ")) {
            int a = Integer.parseInt(msgParts[1]);
            if (this.hasAddress(a)) {
               usbport.writeln(s);
               if (DEBUG) {
                  System.out.println("sent: " + s);
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
        System.out.println("parsing announce string");
        for (int i = 1; i < parsed.length; i++) {
            String[] p2 = parsed[i].split(":");
            if (p2.length == 2) {
                // address:type pair received
                int addr;
                try {
                    addr = Integer.parseInt(p2[0]);
                    if (!addresses.contains(addr)) {
                        if (DEBUG) {
                            System.out.println("adding " + addr);
                        }
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
