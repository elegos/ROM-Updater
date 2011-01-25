package incremental;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SystemIO {
	public static String getMD5Sum(String file) {
		MessageDigest md5;
		FileInputStream fis;
		int read = 0;
		byte[] bytes = new byte[1024];
		BigInteger bigInt;
		String hash = "";
		
		try {
			md5 = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(file);
			while((read = fis.read(bytes)) != -1)
				md5.update(bytes, 0, read);
			bigInt = new BigInteger(1,md5.digest());
			hash = bigInt.toString(16);
			while(hash.length() < 32)
				hash = "0"+hash;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return hash;
	}
	
	public static Map<String, String> loadDirectory(String dir, String dir_root) {
		Map<String, String> listOfFilesAndMD5 = new HashMap<String, String>();
		Map<String, String> recursiveList;
		File directory, innerDirectory;
		String[] files;
		String filePath;
		int i;
		
		if(!dir_root.endsWith("/")) dir_root += "/";
		if(dir.endsWith("/")) dir = dir.substring(0, dir.length()-1);
		
		directory = new File(dir);
		if(directory.isDirectory()) {
			files = directory.list();
			for(i = 0; i < files.length; i++) {
				innerDirectory = new File(dir+"/"+files[i]);
				if(innerDirectory.isDirectory()) {
					recursiveList = loadDirectory(innerDirectory.toString(),dir_root);
					listOfFilesAndMD5.putAll(recursiveList);
				} else {
					if(files[i].contains("CERT.") ||
							files[i].contains("MANIFEST.MF") ||
							files[i].contains("updater-script")) continue;
					filePath = dir+"/"+files[i];
					listOfFilesAndMD5.put(filePath.substring(dir_root.length()), getMD5Sum(dir+"/"+files[i]));
				}
			}
		}
		
		
		return listOfFilesAndMD5;
	}
	
	public static void copyFiles(ArrayList<String> files, String destRoot, String fromRoot) {
		if(!destRoot.endsWith("/")) destRoot += "/";
		if(!fromRoot.endsWith("/")) fromRoot += "/";
		
		File destDir = new File(destRoot);
		if(destDir.exists()) {
			System.out.println("Please delete the incremental directory ("+destRoot+") before proceding. Aborted.");
			return;
		}
		
		if(destDir.mkdirs()) {
			Iterator<String> iter = files.iterator();
			while(iter.hasNext()) {
				String file = iter.next();
				String dir = destRoot+file.substring(0,file.lastIndexOf("/"));
				File dirFile = new File(dir);
				dirFile.mkdirs();
				
				File destFile = new File(destRoot+file);
				File fromFile = new File(fromRoot+file);
				byte[] buffer = new byte[1024];
				int len = 0;
				
				try {
					InputStream in = new FileInputStream(fromFile);
					OutputStream out = new FileOutputStream(destFile);
					
					while ((len = in.read(buffer)) > 0)
						out.write(buffer, 0, len);
					in.close();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else System.out.println("It was impossible to create directory "+destRoot);
	}
	
	public static void createUpdaterScript(ArrayList<String> rmFiles, String destRoot, Boolean app2sd, String block, String mountPoint, String sdType) {
		if(!destRoot.endsWith("/")) destRoot += "/";
		File destDir = new File(destRoot+"META-INF/com/google/android/");
		destDir.mkdirs();
		
		try {
			OutputStream script = new FileOutputStream(destRoot+"META-INF/com/google/android/updater-script");
			Iterator<String> iter = rmFiles.iterator();
			
			script.write("ui_print(\"Incremental update - automatically created with the ROM Updater tool\");\n".getBytes());
			script.write("mount(\"yaffs2\", \"MTD\", \"system\", \"/system\");\n".getBytes());
			script.write("mount(\"yaffs2\", \"MTD\", \"userdata\", \"/data\");\n".getBytes());
			if(app2sd) {
				if(block.equals("")) block = "mmcblk0p2";
				if(mountPoint.equals("")) mountPoint = "/system/sd";
				if(sdType.equals("")) sdType = "ext3";
				String mount = "mount(\""+sdType+"\", \"MCC\", \"/dev/block/"+block+"\", \""+mountPoint+"\");\n";
				script.write(mount.getBytes());
			}

			script.write("ui_print(\"Deleting old files...\");\n".getBytes());
			while(iter.hasNext()) {
				String command = "delete(\"/"+iter.next()+"\");\n";
				script.write(command.getBytes());
			}
			
			File system = new File(destRoot+"system");
			if(system.isDirectory()) {
				script.write("ui_print(\"Extracting /system files...\");\n".getBytes());
				script.write("package_extract_dir(\"system\", \"/system\");\n".getBytes());
			}
			File data = new File(destRoot+"data");
			if(data.isDirectory()) {
				script.write("ui_print(\"Extracting /data files...\");\n".getBytes());
				script.write("package_extract_dir(\"data\", \"/data\");\n".getBytes());
			}
			File boot = new File(destRoot+"boot.img");
			if(boot.isFile()) {
				script.write("ui_print(\"Installing new boot image...\");\n".getBytes());
				script.write("assert(package_extract_file(\"boot.img\", \"/tmp/boot.img\"), write_raw_image(\"/tmp/boot.img\", \"boot\"), delete(\"/tmp/boot.img\"));\");\n".getBytes());
			}
			
			if(app2sd)
				script.write("unmount(\"/system/sd\");\n".getBytes());
			script.write("unmount(\"/data\");\n".getBytes());
			script.write("unmount(\"/system\");\n".getBytes());
			
			script.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
