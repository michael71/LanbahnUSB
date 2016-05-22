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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.Timer;

/**
 * Test program for lanbahn-usb-0.1 control using pi4j lib
 *
 * TODO check acc.decoder behaviour when programs stops / after restart
 * @author Michael Blank
 */
public class LanbahnUSBControl {

    static ArrayList<AccessoryDecoder> decoders = new ArrayList();
    static ArrayList<Integer> addresses = new ArrayList();

    public static Queue<String> rxMessageQueue = new LinkedList<String>();
    public static Queue<String> txMessageQueue = new LinkedList<String>();
    public static final String VERSION = "0.34 - 22 Mai 2016";
    public static final int INVALID_INT = -1;
    public static Preferences prefs;  // preferences shared for all classes
    
    private static Lanbahn lanbahn;

    static boolean DEBUG = true;
    static boolean DEBUG_USB = true;

    public void run() {
        System.out.println("LanbahnUSBControl program - version="+VERSION);
        prefs = Preferences.userNodeForPackage(this.getClass());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(Calendar.getInstance().getTime());
        prefs.put("last_starttime", timeStamp);

        int max_devices = prefs.getInt("max_devices", INVALID_INT);
        if (max_devices == INVALID_INT) {  // no preferences stored until now
            prefs.put("devices_names", "/dev/ttyACM");
            prefs.putInt("max_devices", 10);
        }

        String devices_names = prefs.get("devices_names", "/dev/ttyACM");
        max_devices = prefs.getInt("max_devices", 10);

        lanbahn = new Lanbahn();
        try {
            lanbahn.init();
        } catch (Exception ex) {
            System.out.println("ERROR: Could not init lanbahn class. " + ex.getMessage());
            System.exit(1);
        }

        String device = "";
        for (int i = 0; i < max_devices; i++) {
            device = devices_names + i;
            AccessoryDecoder accDec = new AccessoryDecoder(device);
            if (accDec.usbport.isOpen()) {
                System.out.println("usb-device at " + device);
                decoders.add(accDec);
                accDec.usbport.writeln("ID");
            } else {
                System.out.println("NO usb-device at " + device);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(LanbahnUSBControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (DEBUG) {
            System.out.println("waiting for decoders IDs");
        }

        // wait 3 secs for decoders 'A' (=announce) response
        // if no A-string is received, then there might be an arduino at this
        // usb-port, but no "lanbahn-usb" decoder.
        long t = System.currentTimeMillis();
        while ((System.currentTimeMillis() - t) < 3000) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(LanbahnUSBControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // using Iterator to be able to manipulate "decoders" list
        Iterator<AccessoryDecoder> i = decoders.iterator();
        while (i.hasNext()) {
            AccessoryDecoder dec = i.next(); // must be called before you can call i.remove()
            if (!dec.isReady()) {
                i.remove();
                dec=null;   // delete this nonfunctional decoder
            }
        }

        System.out.println(decoders.size() + " accessory decoders found");
        if (decoders.size() == 0) {
            System.out.println("==> PROGRAM ENDS.");
            System.exit(0);
        }

        // store all valid addresses
        for (AccessoryDecoder dec : decoders) {
            addresses.addAll(dec.getAllAddresses());
        }

        try {
            while (true) {  // endless loop
                if (!txMessageQueue.isEmpty()) {
                    String msg = txMessageQueue.poll();
                    lanbahn.send(msg);
                }
                while (!rxMessageQueue.isEmpty()) {
                    String msg = rxMessageQueue.poll();
                    for (AccessoryDecoder dec : decoders) {
                        dec.sendMessage(msg);  // check for validity and address match done in AccessoryDecoder class
                    }
                }
                Thread.sleep(100); // =reduces CPU usage from 100 to 30% on Rasp.A
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(LanbahnUSBControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("interrupted. program ends."); // this does not seem to work as expected TODO: use shutdownHook instead.
        lanbahn.stop();  // stop lanbahn thread
    }

    public static void main(String args[]) {

        LanbahnUSBControl control = new LanbahnUSBControl();
        control.run();

    }

}
