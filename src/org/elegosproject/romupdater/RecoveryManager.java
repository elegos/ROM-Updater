/*
 * This file is part of ROMUpdater.

 * ROMUpdater is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * ROMUpdater is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with ROMUpdater.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.elegosproject.romupdater;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class RecoveryManager {
	private static String TAG = "RomUpdater[RecoveryManager]";

	public static void rebootRecovery() {
		SharedData sdata = SharedData.getInstance();

		while(sdata.getRecoveryCounter() < sdata.getRecoveryOperations()) {
			while(sdata.getLockProcess());
		}

		sdata.addRecoveryMessage("mkdir -p /cache/recovery\n");
		sdata.addRecoveryMessage("mkdir -p /sdcard/clockworkmod\n");

		//put a random md5sum for sdcard marker !?! for firsts 5.0.x.x koush CWM variants
		sdata.addRecoveryMessage("echo '3f42727dd8641a3bb5734fa7ee78185f' > /sdcard/clockworkmod/.salted_hash\n");
		sdata.addRecoveryMessage("echo 0 > /sdcard/clockworkmod/.recoverycheckpoint\n");
		sdata.addRecoveryMessage("cp /cache/recovery/command /cache/recovery/.last_command\n");
		sdata.addRecoveryMessage("toolbox reboot recovery\n");
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			os.write(sdata.getRecoveryMessage().getBytes());
			os.flush();
		} catch(Exception e) {
			Log.e(TAG, "Unable to reboot into recovery");
		}
	}

	// Extended command section
	public static void recoveryExtCommand(SharedData sdata, String msg) {
		sdata.addRecoveryMessage("echo '"+ msg +"' >> /cache/recovery/extendedcommand\n");
	}

	public static void recoveryStartScript(SharedData sdata) {
		sdata.addRecoveryMessage("echo '#!/sbin/sh' > /cache/recovery/.romupdater.sh\n");
		sdata.addRecoveryMessage("echo 'export PATH=/sbin:$PATH' >> /cache/recovery/.romupdater.sh\n");
		sdata.addRecoveryMessage("chmod 755 /cache/recovery/.romupdater.sh\n");
	}

	public static void recoveryEndScript(SharedData sdata) {
		recoveryExtCommand(sdata, "run_program(\"/cache/recovery/.romupdater.sh\");\n");
		sdata.addRecoveryMessage("echo 'boot-recovery' > /cache/recovery/command\n");
	}

	public static void recoveryAddToScript(SharedData sdata, String msg) {
		sdata.addRecoveryMessage("echo '"+ msg +"' >> /cache/recovery/.romupdater.sh\n");
	}

	public static void setupExtendedCommand() {
		SharedData sdata = SharedData.getInstance();

		while(sdata.getLockProcess());
		sdata.setLockProcess(true);

		sdata.addRecoveryMessage("mkdir -p /sdcard/clockworkmod\n");
		sdata.addRecoveryMessage("mkdir -p /cache/recovery\n");

		sdata.addRecoveryMessage("echo 'boot-recovery' > /cache/recovery/command\n");
		sdata.addRecoveryMessage("echo > /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("rm -f /cache/recovery/.romupdater.sh\n");

		recoveryExtCommand(sdata, "ui_print(\"ROM Updater\");");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	public static void addUpdate(String file) {
		SharedData sdata = SharedData.getInstance();

		while(sdata.getLockProcess());
		sdata.setLockProcess(true);

		if(file.startsWith("/mnt/"))
			file = file.substring(4);
		else if(!file.startsWith("/"))
			file = "/sdcard/"+file;

		sdata.addRecoveryMessage("echo '--update_package="+file+"' > /cache/recovery/command\n");
		sdata.addRecoveryMessage("rm -f /cache/recovery/extendedcommand\n");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	public static void doBackup(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String backupFolder = "/sdcard/"+preferences.getString("backup_folder", "clockworkmod/backup");
		if(!backupFolder.endsWith("/"))
			backupFolder += "/";

		SharedData sdata = SharedData.getInstance();

		while(sdata.getLockProcess());
		sdata.setLockProcess(true);
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH.mm");
			Date date = new Date();

			sdata.addRecoveryMessage("mkdir -p "+backupFolder+"\n");
			recoveryExtCommand(sdata, "ui_print(\"Backing up the current ROM to "
				+ backupFolder + format.format(date) + "\");"
			);
			recoveryExtCommand(sdata, "backup_rom(\"" + backupFolder + format.format(date)+ "\");");

		} catch (Exception e) {
			Log.e(TAG,"Unable to setup environment for Nandroid backup");
			e.printStackTrace();
		} finally {
			sdata.incrementRecoveryCounter();
			sdata.setLockProcess(false);
		}
	}

	public static void restoreBackup(String backupDirectory) {
		SharedData sdata = SharedData.getInstance();

		while(sdata.getLockProcess());
		sdata.setLockProcess(true);
		recoveryExtCommand(sdata, "ui_print(\"Restoring ROM from SDCARD:"+backupDirectory+"\");");
		recoveryExtCommand(sdata, "restore_rom(\"" + backupDirectory + "\");");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	public static void wipeCache() {
		SharedData sdata = SharedData.getInstance();

		while(sdata.getLockProcess());
		sdata.setLockProcess(true);
		recoveryExtCommand(sdata, "ui_print(\"Wiping CACHE\");");
		recoveryExtCommand(sdata, "mount(\"/data\");");

		recoveryStartScript(sdata);
		recoveryAddToScript(sdata, "rm -rf /cache/dalvik-cache");
		recoveryAddToScript(sdata, "rm -rf /data/dalvik-cache");
		recoveryEndScript(sdata);

		//recoveryExtCommand(sdata, "delete_recursive(\"/cache/dalvik-cache\");");
		//recoveryExtCommand(sdata, "delete_recursive(\"/data/dalvik-cache\");");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	public static void wipeData() {
		SharedData sdata = SharedData.getInstance();
		while(sdata.getLockProcess());

		sdata.setLockProcess(true);
		recoveryExtCommand(sdata, "ui_print(\"Wiping USER DATA\");");
		recoveryExtCommand(sdata, "mount(\"/data\");");

		recoveryStartScript(sdata);
		recoveryAddToScript(sdata, "rm -rf /data/*");
		recoveryEndScript(sdata);

		//recoveryExtCommand(sdata, "delete_recursive(\"/data\");");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	public static void wipeDataAndSDExt() {
		SharedData sdata = SharedData.getInstance();
		
		while(sdata.getLockProcess());
		sdata.setLockProcess(true);

		recoveryExtCommand(sdata, "ui_print(\"Wiping Data and SD-EXT apps\");");
		recoveryExtCommand(sdata, "mount(\"/data\");");
		recoveryExtCommand(sdata, "mount(\"/sd-ext\");");

		recoveryStartScript(sdata);
		recoveryAddToScript(sdata, "rm -rf /data/*");
		recoveryAddToScript(sdata, "rm -rf /sd-ext/app");
		recoveryAddToScript(sdata, "rm -rf /sd-ext/app-private");
		recoveryEndScript(sdata);

		recoveryExtCommand(sdata, "umount(\"/sd-ext\");");

		//recoveryExtCommand(sdata, "delete_recursive(\"/sd-ext/app\");");
		//recoveryExtCommand(sdata, "delete_recursive(\"/sd-ext/app-private\");");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

}
