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

import java.net.*;
import java.util.*;

class NIC {

	public static List<InetAddress> getmyip() {

		List<InetAddress> addrList = new ArrayList<InetAddress>();
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
                        return null;
		}

		InetAddress localhost = null;

		try {
			localhost = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
                        return null;
		}

		while (interfaces.hasMoreElements()) {
			NetworkInterface ifc = interfaces.nextElement();
			Enumeration<InetAddress> addressesOfAnInterface = ifc.getInetAddresses();

			while (addressesOfAnInterface.hasMoreElements()) {
				InetAddress address = addressesOfAnInterface.nextElement();
                // look for IPv4 addresses which are not==127.0.0.1
				if (!address.equals(localhost) && !address.toString().contains(":")) {
					addrList.add(address);
				//	System.out.println("FOUND ADDRESS ON NIC: " + address.getHostAddress());

				}
			}
		}
		return addrList;
	}
}
