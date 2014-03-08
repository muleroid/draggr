package edu.uchicago.proprio.draggr.transfer;

import static edu.uchicago.proprio.draggr.transfer.Connector.Command.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

class Connector {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	enum Command {
		/* Ensure there are less than 256 of these.
		 * They are serialized as a single byte.
		 */
		NAME, MOTD, TRANSFER, LIST_FILES, UPLOAD, PREVIEW, CLOSE;
	}
	
	Connector() {
		super();
	}

	Connector(Socket socket) throws IOException {
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}

	boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}
	
	boolean connect(SocketAddress a, int timeout, String name) {
		socket = new Socket();
		
		try {
			socket.connect(a, timeout);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			sendCommand(NAME);
			if (recvString().equals(name))
				return true;
		} catch (IOException e) {}

		try { socket.close(); }
		catch (IOException e) {}
		
		return false;
	}
	
	void close() throws IOException {
		socket.close();
	}
	
	public String toString() {
		return "Connector("+socket.getInetAddress().toString()+")";
	}

	void sendCommand(Command c) throws IOException {
		out.writeByte(c.ordinal());
	}

	Command recvCommand() throws IOException {
		return Command.values()[in.readByte()];
	}
	
	void sendInt(int i) throws IOException {
		out.writeInt(i);
	}
	
	int recvInt() throws IOException {
		return in.readInt();
	}

	void sendString(String s) throws IOException {
		out.writeInt(s.length());
		out.write(s.getBytes("UTF-8"));
	}
	
	String recvString() throws IOException {
		int len = in.readInt();
		byte[] b = new byte[len];
		in.readFully(b);
		return new String(b, "UTF-8");
	}

	void sendFile(File f) throws IOException {
		long bytesLeft = f.length();
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
	
	void recvFile(File dest) throws IOException {
		FileOutputStream s = new FileOutputStream(dest);
		long bytesLeft = in.readLong();
		
		byte[] buf = new byte[4096];
		while (bytesLeft > 0) {
			int toRead = (bytesLeft < 4096) ? (int) bytesLeft : 4096;
			int bytesRead = in.read(buf, 0, toRead);
			s.write(buf, 0, bytesRead);
			bytesLeft -= bytesRead;
		}
		
		s.close();
	}
}
