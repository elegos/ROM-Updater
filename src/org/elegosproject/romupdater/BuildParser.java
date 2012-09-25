package org.elegosproject.romupdater;

import java.io.*;

public class BuildParser {
	private static final String TAG = "RomUpdater[BuildParser]";

	private static String build_prop = "/system/build.prop";
	
	private static String readProperty(String prop) {
		File propFile = new File(build_prop);
		
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		
		try {
			fis = new FileInputStream(propFile);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			String buffer;
			
			while(dis.available() > 0) {
				buffer = dis.readLine();
				if(buffer.startsWith(prop))
					return buffer.substring(buffer.indexOf("=")+1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static String parseString(String prop) {
		return readProperty(prop);
	}
	
	public static Integer parseInt(String prop) {
		Integer result;
		
		try {
			result = Integer.parseInt(readProperty(prop));
		} catch (Exception e) {
			// not an integer
			result = -1;
		}
		return result;
	}
}
