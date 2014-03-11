package edu.uchicago.proprio.draggr.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class DraggrXmlParser {
	private static final String ns = null;
	private static final String LOGTAG = "DraggrXmlParser";
	
	public static class DeviceEntry {
		public final String name;
		public final String trackable;
		public final byte[] ipaddr;
		
		private DeviceEntry(String name, String trackable, byte[] ipaddr) {
			this.name = name;
			this.trackable = trackable;
			this.ipaddr = ipaddr;
		}
	}
	
	public List parse(InputStream in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} finally {
			in.close();
		}
	}
	
	private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
		List entries = new ArrayList();
		
		parser.require(XmlPullParser.START_TAG, ns, "device_list");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if(name.equals("device")) {
				entries.add(readDevice(parser));
			} else {
				skip(parser);
			}
		}
		return entries;
	}
	
	private DeviceEntry readDevice(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "device");
		String name = null;
		String trackable = null;
		byte[] ipaddr = null;
		while(parser.next() != XmlPullParser.END_TAG) {
			if(parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String womp = parser.getName();
			if(womp.equals("name")) {
				name = readName(parser);
			} else if (womp.equals("trackable")) {
				trackable = readTrackable(parser);
			} else if (womp.equals("ip_address")) {
				ipaddr = readIP(parser);
			} else {
				skip(parser);
			}
		}
		
		return new DeviceEntry(name, trackable, ipaddr);
	}
	
	private String readName(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "name");
		String name = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "name");
		return name;
	}
	
	private String readTrackable(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "trackable");
		String trackable = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "trackable");
		return trackable;
	}
	
	private byte[] readIP(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "ip_address");
		String ipString = readText(parser);
		Log.d(LOGTAG, ipString);
		parser.require(XmlPullParser.END_TAG, ns, "ip_address");
		return InetAddress.getByName(ipString).getAddress();
	}
	
	private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}
