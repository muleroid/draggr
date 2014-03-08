package edu.uchicago.proprio.draggr.transfer;

import static edu.uchicago.proprio.draggr.transfer.PassiveHandler.Command.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;


public class Device {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String name;
	private int port;
	
	public Device (String name) {
		this(name, Server.defaultPort);
	}
	
	public Device (String name, int port) {
		this.name = name;
		this.port = port;
	}
	
	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}
	
	public String getName() {
		return name;
	}
	
	public int getPort() {
		return port;
	}
	
	/* Returns true if successful, false otherwise. */
	public boolean tryConnect() {
		/* Search all interfaces for a site-local IPv4 address */
		Enumeration<NetworkInterface> e;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException x) {
			return false;
		}
		while (e.hasMoreElements()) {
			Enumeration<InetAddress> ee = e.nextElement().getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress a = ee.nextElement();
				if (a instanceof Inet4Address && a.isSiteLocalAddress()) {
					
					/* Scan through the 256 closest addresses for a match */
					byte[] addr = a.getAddress();
					/* TODO: TEMPORARY */
					addr[0] = 10;
					addr[1] = (byte) 150;
					addr[2] = 121;
					/* END TEMPORARY */
					for (byte i = 0; i < 256; i++) {
						addr[3] = i;
						try {
							InetSocketAddress aa = new InetSocketAddress(
									InetAddress.getByAddress(addr), this.port);
							this.socket = new Socket();
							socket.connect(aa, 20); // TODO: pick a good timeout
							in = new DataInputStream(socket.getInputStream());
							out = new DataOutputStream(socket.getOutputStream());
							out.writeByte(NAME.ordinal());
							if (recvString().equals(this.name))
								return true;
							socket.close();
						} catch (UnknownHostException x) {
							
						} catch (IOException x) {
							
						}
					}
				}
			}
		}
		
		socket = null; in = null; out = null;
		return false;
	}
	
	public String motd() throws IOException {
		out.writeByte(MOTD.ordinal());
		return recvString();
	}
	
	public String[] listFiles(String filter) throws IOException {
		out.writeByte(LIST_FILES.ordinal());
		sendString(filter);
		return recvString().split("\n");
	}
	
	public void transfer(String filename, Device otherDevice)
			throws IOException {
		out.writeByte(TRANSFER.ordinal());
		sendString(otherDevice.getName());
		out.writeInt(otherDevice.getPort());
		sendString(filename);
	}
	
	public void upload(String filename, File f) throws IOException {
		long bytesLeft = f.length();
		out.writeByte(UPLOAD.ordinal());
		sendString(filename);
		out.writeLong(bytesLeft);
		FileInputStream s = new FileInputStream(f);
		
		byte[] buf = new byte[4096];
		while (bytesLeft > 0) {
			int bytesRead = s.read(buf);
			out.write(buf, 0, bytesRead);
			bytesLeft -= bytesRead;
		}
		
		s.close();
	}
	
	public void close() throws IOException {
		out.writeByte(CLOSE.ordinal());
		socket.close();
	}

	public String toString() {
		return "Device connected to local port " + socket.getLocalPort();
	}
	
	/* TODO: remove the code duplication with PassiveHandler class */
	
	private String recvString() throws IOException {
		int len = in.readInt();
		byte[] b = new byte[len];
		in.readFully(b);
		return new String(b, "UTF-8");
	}

	private void sendString(String s) throws IOException {
		out.writeInt(s.length());
		out.write(s.getBytes("UTF-8"));
	}
}
