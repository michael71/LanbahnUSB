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

import static net.lanbahn.lanbahnusbcontrol.LanbahnUSBControl.*;

/** handles LANBAHN UDP messages, for protocol definition,
 *  see http://www.lanbahn.net/protocol
 * 
 * @author Michael Blank 
 * 
 * 
 */
public class Lanbahn {

    public static final int LANBAHN_PORT = 27027;
    public static final String LANBAHN_GROUP = "239.200.201.250";
    //private static final int MAX_LANBAHN_ADDRESS = 1024; // currently not used
   
    private static String TEXT_ENCODING = "UTF8";
    protected InetAddress mgroup;
    protected MulticastSocket multicastsocket;
    static LanbahnServer lbServer;

    protected Thread t;

    public int init() throws Exception {

        try {
            multicastsocket = new MulticastSocket(LANBAHN_PORT);
            mgroup = InetAddress.getByName(LANBAHN_GROUP);
            multicastsocket.joinGroup(mgroup);
            multicastsocket.setLoopbackMode(false);
            // true = disable receive of own messages
            // false = receive own messages
            // MUST be set to false to be able to receive messages from
            // other programs on the same computer like Java Panel !!!
            System.out.println("new lanbahn multicast socket at port=" + LANBAHN_PORT);
            System.out.println("Remark: multicast does not seem to work on Raspi3/onboard-wlan");

        } catch (IOException ex) {
            System.out.println("could not open server socket on port="
                    + LANBAHN_PORT);
            return 1;
        }
        startLanbahnServer(); // for receiving multicast messages

        return 0;
    }

    public void stop() {
        t.interrupt();    // preferred over t.stop() which is deprecated.
    }

    /**
     * send a lanbahn message to the multicast UDP port
     * 
     * @param msg 
     */
    public void send(String msg) {
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
            //  setNetworkInterface(NetworkInterface netIf)
            //            throws SocketException
            //Specify the network interface for OUTGOING multicast datagrams sent on this socket.
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

    /** LanbahnServer waits for messages on UDP port and add them to the 
     *  RX queue
     */
    class LanbahnServer implements Runnable {

        public void run() {
            try {
                System.out.println("starting LanbahnServer");
                byte[] bytes = new byte[65536];
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

                while (!Thread.currentThread().isInterrupted()) {
                    // Warten auf Nachricht
                    multicastsocket.receive(packet);   //TODO check timeout 

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
