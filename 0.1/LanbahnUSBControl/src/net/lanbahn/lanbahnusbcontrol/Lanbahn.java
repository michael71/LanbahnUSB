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



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.Preferences;

import static net.lanbahn.lanbahnusbcontrol.LanbahnUSBControl.*;

public class Lanbahn {

    // z.B 80 = weiche, wirkt auf i2c=0x10, bit0+, bit1-
    // 80 1 = bit0 an, bit1 aus
    // 81 = signalRG, wirkt auf i2c=0x10, bit2+, bit3-
    // 90 = signalRGY. wirkt auf i2c=0x10, bit4,5,6
    // 90:0 => Red, 90:1 => Green 90:2 =>Yellow (red and green on)
    public static final int LANBAHN_PORT = 27027;
    public static final String LANBAHN_GROUP = "239.200.201.250";
    private static final int MAX_LANBAHN_ADDRESS = 1024; // currently not used
    private static String TEXT_ENCODING = "UTF8";
    protected InetAddress mgroup;
    protected MulticastSocket multicastsocket;
    static LanbahnServer lbServer;
    private volatile boolean running = true;
    protected Thread t;

    // Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    Preferences prefs = Preferences.userRoot();

    // can be found at
    // /root/.java/.userPrefs/de/blankedv/jlanbahn
    public int init() throws Exception {
        
        List<InetAddress> myip = NIC.getmyip();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(Calendar.getInstance().getTime());
        prefs.put("time", timeStamp);
        // loadPrefs();

        if (!myip.isEmpty()) {
            try {
                multicastsocket = new MulticastSocket(LANBAHN_PORT);
                mgroup = InetAddress.getByName(LANBAHN_GROUP);
                multicastsocket.joinGroup(mgroup);
                //multicastsocket.setLoopbackMode(true); //yes, disable receive of own messages
                // s = new ServerSocket(SXNET_PORT,0,myip.get(0));
                // only listen on 1 address on multi homed systems
                System.out.println("new lanbahn multicast socket "
                        + myip.get(0) + ":" + LANBAHN_PORT);
                System.out.println("multicast does not seem to work on Raspi3/onboard-wlan");
                // DatagramPacket hi = new DatagramPacket(msg.getBytes(),
                // msg.length(), group, 6789);

            } catch (IOException ex) {
                System.out.println("could not open server socket on port="
                        + LANBAHN_PORT);
                return 1;
            }
            startLanbahnServer(); // for receiving multicast messages

            // Timer timer = new Timer(); // for sending multicast messages
            // timer.schedule(new MCSendTask(), 1000, 1000);
        } else {
            System.out.println("no network adapter, cannot listen to lanbahn messages.");
            return 1;
        }
        return 0;
    }

    public void stop() {
        running = false;
        t.stop();  // TODO
    }

    void send(String msg) {
        if ((msg == null) || (msg.length() == 0)) {
            return;
        }

        byte[] buf = new byte[256];
        buf = msg.getBytes();
        DatagramPacket packet;
        packet = new DatagramPacket(buf, buf.length,
                mgroup, LANBAHN_PORT);

        System.out.println("sending to lanbahn " + msg);
        try {
            multicastsocket.send(packet);
        } catch (IOException ex) {
            System.out.println("ERROR when sending to lanbahn " + ex.getMessage());
        }

    }

    private void startLanbahnServer() {
        if (lbServer == null) {
            lbServer = new LanbahnServer();
            t = new Thread(lbServer);
            t.start();

        }

    }

    class LanbahnServer implements Runnable {

        public void run() {
            try {
                System.out.println("starting LanbahnServer");
                byte[] bytes = new byte[65536];
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

                while (running) {
                    // Warten auf Nachricht
                    multicastsocket.receive(packet);   //TODO check timeout 
                    // TODO to be able to interrupt thread
                    String message = new String(packet.getData(), 0,
                            packet.getLength(), TEXT_ENCODING);

                    // receiving queue for main program
                    message = message.trim().toUpperCase();
                    System.out.println("received:" + message);

                    rxMessageQueue.add(message);

                }
                System.out.println("lanbahn Server closing.");
                multicastsocket.leaveGroup(mgroup);
                multicastsocket.close();

            } catch (IOException ex) {
                System.out.println("lanbahnServer error:" + ex);
            }

        }

    }

}
