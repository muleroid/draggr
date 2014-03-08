package edu.uchicago.proprio.draggr.transfer;

import static edu.uchicago.proprio.draggr.transfer.Server.LogLevel.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PassiveHandler extends Thread {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private Server parent;
	
	public enum Command {
		/* Ensure there are less than 256 of these.
		 * They are serialized as a single byte.
		 */
		NAME, MOTD, TRANSFER, LIST_FILES, UPLOAD, CLOSE;
	}
	
	public PassiveHandler(String name, Socket socket, Server parent) 
			throws IOException {
		super(name);
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		this.parent = parent;
	}
	
	public void close() throws IOException {
		socket.close();
	}
	
	public void run() {
		boolean done = false;
		while (!done) {
			try {
				switch (recvCommand()) {
				case NAME:
					sendString(parent.getName());
					break;
				case MOTD:
					sendString(parent.getMotd());
					break;
				case LIST_FILES:
					String filter = recvString();
					sendString(parent.listFiles(filter));
					break;
				case TRANSFER:
					handleTransfer();
					break;
				case UPLOAD:
					handleUpload();
					break;
				case CLOSE:
					done = true;
					break;
				default:
					throw new IOException("Unknown command for draggr protocol");
				}
			} catch (IOException e) {
				done = true;
			}
		}
		
		try { this.close(); }
		catch (IOException e) {}
	}
	
	public String toString() {
		return "PassiveHandler for device connected to local port"
				+ socket.getLocalPort();
	}

	private void log(Server.LogLevel level, String msg) {
		parent.log(level, msg);
	}
	
	private Command recvCommand() throws IOException {
		return Command.values()[in.readByte()];
	}
	
	private void sendString(String s) throws IOException {
		out.writeInt(s.length());
		out.write(s.getBytes("UTF-8"));
	}
	
	private String recvString() throws IOException {
		int len = in.readInt();
		byte[] b = new byte[len];
		in.readFully(b);
		return new String(b, "UTF-8");
	}
	
	private void handleTransfer() throws IOException {
		String deviceName = recvString();
		int port = in.readInt();
		String filename = recvString();
		
		/* From here on, we catch I/O errors, because we do not want
		 * connection problems with the new device to cause the
		 * preexisting connection to be aborted.
		 */
		Device otherDevice = new Device(deviceName, port);
		if (otherDevice.tryConnect()) {
			try {
				otherDevice.upload(filename, parent.getFile(filename));
				otherDevice.close();
				return;
			} catch (IOException e) {
				
			}
		}
		log(WARN, "Transfer of " + filename + " could not be completed");
	}
	
	private void handleUpload() throws IOException {
		String filename = recvString();
		FileOutputStream s = new FileOutputStream(
				parent.createFile(filename));
		long bytesLeft = in.readLong();
		
		byte[] buf = new byte[4096];
		while (bytesLeft > 0) {
			int bytesRead = in.read(buf);
			s.write(buf, 0, bytesRead);
			bytesLeft -= bytesRead;
		}
		
		s.close();
	}
}
