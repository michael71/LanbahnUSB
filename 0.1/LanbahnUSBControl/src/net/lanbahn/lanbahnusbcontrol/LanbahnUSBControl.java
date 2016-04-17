/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lanbahn.lanbahnusbcontrol;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Test program for lanbahn-usb-0.1 control
 * using pi4j lib
 * 
 * @author Michael Blank
 */

public class LanbahnUSBControl {
    static ArrayList<AccessoryDecoder> decoders = new ArrayList();
    static ArrayList<Integer> addresses = new ArrayList();
    
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
        
        int count = 0;
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
        }
                
     }  
    
}
