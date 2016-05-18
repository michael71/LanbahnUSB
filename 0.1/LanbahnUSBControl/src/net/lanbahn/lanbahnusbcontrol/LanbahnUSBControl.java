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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.Timer;

/**
 * Test program for lanbahn-usb-0.1 control
 * using pi4j lib
 * 
 * @author Michael Blank
 */

public class LanbahnUSBControl {
    static ArrayList<AccessoryDecoder> decoders = new ArrayList();
    static ArrayList<Integer> addresses = new ArrayList();
    
    public static Queue<String> rxMessageQueue = new LinkedList<String>();
    public static Queue<String> txMessageQueue = new LinkedList<String>();
    public static final String VERSION = "0.2 - 18 Mai 2016";
    public static final int INVALID_INT = -1;

    private static Timer timer;
    private static Lanbahn lanbahn;
    
    static boolean DEBUG = true;
    
     private static void printAddressList(ArrayList<Integer> a) {
         System.out.print("addresses=");
         for( Integer addr:a) {
             System.out.print(addr+" ");         
         }
         System.out.println();
     }
    @SuppressWarnings("SleepWhileInLoop")
     public static void main(String args[]) throws InterruptedException {
        final int MAX_DEVICES = 10;
        initTimer();
        lanbahn = new Lanbahn();
        try {
            lanbahn.init();
        } catch (Exception ex) {
            System.out.println("ERROR: Could not init lanbahn class.");
            System.exit(1);
        }
        
        String device ="";
        for (int i= 0; i <MAX_DEVICES; i++) {
            device  = "/dev/ttyACM"+i;
            AccessoryDecoder accDec = new AccessoryDecoder(device);
            if (accDec.usbport.isOpen() ) {
               System.out.println("lanbahn-usb at "+device);
               decoders.add(accDec);
            } else {
               System.out.println("NO lanbahn-usb at "+device);
            }
            Thread.sleep(100);
        }
        
        
        if (DEBUG)  System.out.println("waiting for decoders IDs");
        
        long t = System.currentTimeMillis();
        while ((System.currentTimeMillis() -t) < 3000) {
            // wait 3 secs for decoders 'A' response
            Thread.sleep(100);
        }
        
        Iterator<AccessoryDecoder> i = decoders.iterator();
        while (i.hasNext()) {
            AccessoryDecoder dec = i.next(); // must be called before you can call i.remove()
            if (!dec.isReady()) i.remove(); 
        }

        System.out.println(decoders.size()+ " accessory decoders found");
        
        // store all valid addresses
        for (AccessoryDecoder dec:decoders) {
            addresses.addAll(dec.getAllAddresses());
        }
        if (DEBUG) printAddressList(addresses);
        
        /* int count = 0;
        int[]  muster = {0, 1, 0, 2, 0, 0};
        
        while(true) {
            // endless loop
            
            // TODO 
            
            // receive network command
            // check in accessory decoder list, if we have a matching decoder
            // for an address a - then send the value to this decoder
            
            count++;
            if (count >= muster.length) count=0;
            
            for (AccessoryDecoder dec:decoders) {
                int addr = 710;
                dec.set(addr, muster[count]);
                addr = 720;
                dec.set(addr, muster[count]);
                addr = 721;
                dec.set(addr, muster[count]);
            }
            Thread.sleep(3000);
        } */
        
        while(true) {
            Thread.sleep(100);
            ; // endless loop
        }
                
     }  
     
     private static void sendToDecoder(String msg) {
         for (AccessoryDecoder dec:decoders) {
             dec.sendMessage(msg);
         }
     }
     
    private static void initTimer() {
        timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!txMessageQueue.isEmpty()) {
                    String msg = txMessageQueue.poll();
                    lanbahn.send(msg);
                }
                while (!rxMessageQueue.isEmpty()) {
                    String msg = rxMessageQueue.poll();
                    sendToDecoder(msg);
                }

            }

        });
        timer.start();
    }

    private static void interpret(String msg) {
        if  (msg.length() > 40) {
            return; // too long to send to XBEE
        }
        // todo remove whitespaces
        String cmd[] = msg.toUpperCase().split(" ");
        if (cmd.length < 2) {
            return; // no command
        }
        switch (cmd[0]) {
            case "SET":
            case "READ":
                /* TODO
                LanbahnXBeeDevice lbDev = getXBeeDeviceFromAddress(cmd[1]);
                if (lbDev == null) {
                    return;
                }
                try {
                    System.out.format("Sending data: '%s'\n", msg + " time=" + (System.currentTimeMillis() - start));
                    // translate address to relative address in accessory decoder;
                    int relAdr = lbDev.getRelativeAddress(Integer.parseInt(cmd[1]));
                    String relMsg = cmd[0]+" "+relAdr;
                    if (cmd.length >=3) relMsg+=" "+cmd[2];
                    myDevice.sendData(lbDev.getXbee(), relMsg.toUpperCase().getBytes());  // TODO make arduino insensitive for case
                } catch (XBeeException ex) {
                    System.out.println("could not send to XBeeDevice");
                    Logger.getLogger(Lanbahn2XBeeApp.class.getName()).log(Level.SEVERE, null, ex);
                } */
                break;
            default:
            // cannot interpret this command

        }
    }
}
