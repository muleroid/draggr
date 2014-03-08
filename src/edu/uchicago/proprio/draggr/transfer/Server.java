package edu.uchicago.proprio.draggr.transfer;

import static edu.uchicago.proprio.draggr.transfer.Server.LogLevel.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server extends Thread {
	public static int defaultPort = 23400;
	
	private ServerSocket passive;
	private boolean closing;
	private Set<PassiveHandler> handlers;
	private LogLevel logLevel;
	private String motd; /* message of the day */
	private File root, thumbs, previews; /* location of the files under management */
	
	public enum LogLevel {
		FATAL, ERROR, WARN, INFO, DEBUG, TRACE;
	}
	
	public Server(String name, String root, String motd)
			throws IOException {
		this(name, root, motd, Server.defaultPort);
	}
	
	public Server(String name, String root, String motd, int port)
			throws IOException {
		super(name);
		passive = new ServerSocket(port);
		closing = false;
		handlers = new HashSet<PassiveHandler>();
		logLevel = INFO;
		setMotd(motd);
		setRoot(root);
	}
	
	public void setMotd(String m) {
		motd = m;
	}
	
	public void setRoot(String r) {
		root = new File(r);
		if (!root.exists())
			if (!root.mkdirs())
				log(ERROR, "Could not create root directory");
		thumbs = new File(r + "/thumbs");
		if (!thumbs.exists())
			if (!thumbs.mkdir())
				log(ERROR, "Could not create thumbnails directory");
		previews = new File(r + "/previews");
		if (!previews.exists())
			if (!previews.mkdir())
				log(ERROR, "Could not create previews directory");
	}
	
	public void setLogLevel(LogLevel l) {
		logLevel = l;
	}
	
	
	public void close() {
		this.closing = true;
		try { passive.close(); }
		catch (IOException e) {
			log(ERROR, "Error attempting close");
		}
	}
	
	String getMotd() {
		return motd;
	}
	
	File[] listFiles(final String filter) {
		log(TRACE, "listFiles()");
		FileFilter ff = new FileFilter () {
			public boolean accept(File f) {
				return f.getName().contains(filter)
						&& !f.getName().equals("thumbs")
						&& !f.getName().equals("previews");
			}
		};
		return root.listFiles(ff);
	}
	
	File getFile(String filename) {
		for (File f : root.listFiles()) {
			if (f.getName().equals(filename))
				return f;
		}
		return null;
	}
	
	File getThumbnail(String filename) {
		for (File f : thumbs.listFiles()) {
			if (f.getName().equals(filename))
				return f;
		}
		return null;
	}
	
	File getPreview(String filename) {
		for (File f : previews.listFiles()) {
			if (f.getName().equals(filename))
				return f;
		}
		return null;
	}
	
	File createFile(String filename) {
		return new File(root.getAbsolutePath() + "/" + filename);
	}

	File createThumbnail(String filename) {
		return new File(thumbs.getAbsolutePath() + "/" + filename);
	}

	File createPreview(String filename) {
		return new File(previews.getAbsolutePath() + "/" + filename);
	}
	
	public void run() {
		if (motd == null || root == null) {
			log(FATAL, "You must set motd and root before starting draggrd");
			return;
		}
		
		log(INFO, "Server starting on port " + passive.getLocalPort());
		log(INFO, "Name: " + this.getName());
		log(INFO, "MOTD: " + motd);
		log(INFO, "Root: " + root.getAbsolutePath());
		
		while (!closing) {
			try {
				Socket s = passive.accept();
				log(INFO, "Got connection from " + s.getInetAddress());
				PassiveHandler handler = new PassiveHandler(
						"passive-" + Integer.toString(s.getPort()), s, this);
				handlers.add(handler);
				handler.start();
			}
			catch (IOException e) {
				if (closing)
					log(INFO, "Close requested");
				else
					log(WARN, "Unknown error accepting new connection");
			}
		}
		
		cleanup();
		log(INFO, "Cleanup complete");
	}

	void log(LogLevel level, String msg) {
		if (level.compareTo(this.logLevel) <= 0)
			System.err.printf("%s: %s %s\n", Thread.currentThread().getName(),
					level, msg);
	}
	
	private void cleanup() {
		for (PassiveHandler h : handlers) {
			try { h.close(); }
			catch (IOException e) {
				log(WARN, "Error closing " + h);
			}
		}
		try {
			for (PassiveHandler h: handlers) {
				h.join();
			}
		} catch (InterruptedException e) {
			log(ERROR, "Interrupted during cleanup");
		}
	}
	
	public static void main(String []args) {
		try {
			System.out.println("starting");
			Server s = new Server(args[0], args[1], args[2]);
			s.setLogLevel(TRACE);
			s.start();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(System.in));
			String cmd;
			Device localDevice = new Device(args[0]);
			if (!localDevice.tryConnect()) {
				System.out.println("error connecting device");
			} else {
				while ((cmd = br.readLine()) != null) {
					if (cmd.startsWith("LIST_FILES")) {
						for (String f : localDevice.listFiles(""))
							System.out.println(f);
					} else if (cmd.startsWith("TRANSFER")) {
						String[] cmds = cmd.split(" ");
						localDevice.transfer(cmds[1], localDevice);
					} else if (cmd.startsWith("PREVIEW")) {
						String[] cmds = cmd.split(" ");
						File f = localDevice.preview(cmds[1]);
						System.out.println("received! " + f.length());
					} else if (cmd.startsWith("THUMB")) {
						String[] cmds = cmd.split(" ");
						File f = localDevice.thumbnail(cmds[1]);
						System.out.println("found! " + f.length());
					} else {
						System.out.println("Sorry, could you repeat that?");
					}
				}
				localDevice.close();
			}
			s.close();
			s.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("usage: java Server.java "
					+ "<server_name> <path_to_root> <motd>");
		}
	}
}
