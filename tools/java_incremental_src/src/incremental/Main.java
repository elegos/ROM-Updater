package incremental;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Main {
	private static String oldROM = "";
	private static String newROM = "";
	private static String incremental = "";
	private static Boolean doWrite = false;
	private static Boolean app2sd = false;
	private static String sdBlock = "";
	private static String sdMountPoint = "";
	private static String sdType = "";
	
	public static void main(String[] args) {
		System.out.println("EleGoS ROM incremental creator");
		
		int i;
		for(i = 0; i < args.length; i++) {
			if(args[i].equals("-h") || args[i].equals("--help")) {
				printHelp();
				return;
			}
			if(args[i].length() > 3) {
				if(args[i].startsWith("-o=")) oldROM = args[i].substring(3);
				if(args[i].startsWith("-n=")) newROM = args[i].substring(3);
				if(args[i].startsWith("-i=")) {
					incremental = args[i].substring(3);
					doWrite = true;
				}
				if(args[i].equals("--app2sd")) app2sd = true;
				if(args[i].startsWith("--sdBlock=")) sdBlock = args[i].substring(10);
				if(args[i].startsWith("--sdMountPoint=")) sdMountPoint = args[i].substring(15);
				if(args[i].startsWith("--sdType=")) sdType = args[i].substring(9);
			}
			if(args[i].equals("-w")) doWrite = true;
		}
		if(args.length == 0 || oldROM.equals("") || newROM.equals("")) {
			printHelp();
			return;
		}
		
		Map<String, String> oldMap;
		Map<String, String> newMap;
		oldMap = SystemIO.loadDirectory(oldROM, oldROM);
		newMap = SystemIO.loadDirectory(newROM, newROM);
		
		DirChanges changes = new DirChanges();
		changes.setLists(oldMap, newMap);
		
		ArrayList<String> newFiles = changes.getNewFiles();
		ArrayList<String> modFiles = changes.getModifiedFiles();
		ArrayList<String> rmFiles = changes.getRemovedFiles();
		
		Iterator<String> iter;

		System.out.println("New files:");
		iter = newFiles.iterator();
		while(iter.hasNext())
			System.out.println("\t"+iter.next());

		System.out.println("Modified files:");
		iter = modFiles.iterator();
		while(iter.hasNext())
			System.out.println("\t"+iter.next());
		
		System.out.println("Removed files:");
		iter = rmFiles.iterator();
		while(iter.hasNext())
			System.out.println("\t"+iter.next());
		
		if(doWrite || !incremental.equals("")) {
			if(incremental.equals("")) incremental = "incremental";
			newFiles.addAll(modFiles);
			newFiles.add("META-INF/com/android/metadata");
			newFiles.add("META-INF/com/google/android/update-binary");
			System.out.println("Copying files...");
			SystemIO.copyFiles(newFiles, incremental, newROM);
			System.out.println("Creating updater-script file...");
			SystemIO.createUpdaterScript(rmFiles, incremental, app2sd, sdBlock, sdMountPoint, sdType);
			System.out.println("Done. Please check the contents before zipping and signing!");
		}
	}
	
	private static void printHelp() {
		System.out.println("Usage: (option=value or option)");
		System.out.println("\t-h, --help\tprint this help");
		System.out.println("\t-o\t\tprevious (old) version directory");
		System.out.println("\t-n\t\tnew version directory");
		System.out.println("\t-i\t\t(optional) incremental version directory\n"+
				"\t\t\tit must not exist (default: incremental).");
		System.out.println("\t-w\t\t(optional, no value) write the incremental folder (useless if -i is set)");
		System.out.println("\t--app2sd\t(optional, no value) enables app2sd in the updater-script");
		System.out.println("\t--sdType\t(optional) type of the app2sd partition (default: ext3)");
		System.out.println("\t--sdMountPoint\t(optional) mount point of the app2sd partition (default: /system/sd)");
		System.out.println("\t--sdBlock\t(optional) app2sd partition block (default: mmcblk0p2)");
	}

}
