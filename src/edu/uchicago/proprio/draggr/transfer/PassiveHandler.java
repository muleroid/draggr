package edu.uchicago.proprio.draggr.transfer;

import static edu.uchicago.proprio.draggr.transfer.Server.LogLevel.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PassiveHandler extends Thread {
	private Connector conn;
	private Server parent;
	
	public PassiveHandler(String name, Socket socket, Server parent) 
			throws IOException {
		super(name);
		this.conn = new Connector(socket);
		this.parent = parent;
	}
	
	public void close() throws IOException {
		conn.close();
	}
	
	public void run() {
		boolean done = false;
		while (!done) {
			try {
				switch (conn.recvCommand()) {
				case NAME:
					conn.sendString(parent.getName());
					break;
				case MOTD:
					conn.sendString(parent.getMotd());
					break;
				case LIST_FILES:
					handleListFiles();
					break;
				case TRANSFER:
					handleTransfer();
					break;
				case UPLOAD:
					handleUpload();
					break;
				case PREVIEW:
					String filename = conn.recvString();
					conn.sendFile(parent.getPreview(filename));
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
		
		log(INFO, "Closing connection " + conn);
		try { this.close(); }
		catch (IOException e) {}
	}
	
	public String toString() {
		return "PassiveHandler for " + conn;
	}

	private void log(Server.LogLevel level, String msg) {
		parent.log(level, msg);
	}
	
	private void handleListFiles() throws IOException {
		log(TRACE, "handleListFiles()");
		String filter = conn.recvString();
		File[] files = parent.listFiles(filter);
		
		String r = "";
		for (File f : files)
			r += f.getName() + "\n";
		conn.sendString(r);
		log(TRACE, "sent file list:\n" + r);
		
		for (File f : files) {
			File t = parent.getThumbnail(f.getName());
			log(TRACE, "sending thumbnail: " + f.getName() + " " + t.length());
			conn.sendFile(t);
		}
	}
	
	private void handleTransfer() throws IOException {
		String deviceName = conn.recvString();
		int port = conn.recvInt();
		String filename = conn.recvString();
		
		/* From here on, we catch I/O errors, because we do not want
		 * connection problems with the new device to cause the
		 * preexisting connection to be aborted.
		 */
		Device otherDevice = new Device(deviceName, port);
		if (otherDevice.tryConnect()) {
			try {
				otherDevice.upload(filename,
						parent.getFile(filename),
						parent.getThumbnail(filename),
						parent.getPreview(filename));
				otherDevice.close();
				return;
			} catch (IOException e) {
				
			}
		}
		log(WARN, "Transfer of " + filename + " could not be completed");
	}
	
	private void handleUpload() throws IOException {
		String filename = conn.recvString();
		conn.recvFile(parent.createFile(filename));
		conn.recvFile(parent.createThumbnail(filename));
		conn.recvFile(parent.createPreview(filename));
	}
}
