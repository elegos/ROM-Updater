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
	private static String TAG = "ROM Updater (Recovery Manager)";
	public static void applyUpdate(String file) {
		try {
			// request super user rights
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			
			os.write("mkdir -p /cache/recovery/\n".getBytes());
			os.write("echo 'boot-recovery' >/cache/recovery/command\n".getBytes());
			os.write("echo 'print ROM Updater by elegos\n' > /cache/recovery/extendedcommand\n".getBytes());
			String cmd = "echo 'install_zip SDCARD:"+file+"' >> /cache/recovery/extendedcommand\n";
			os.write(cmd.getBytes());
			
			os.write("reboot recovery\n".getBytes());
			os.flush();
		} catch (Exception e) {
			Log.e(TAG,"Unable to reboot into Recovery mode for applying package");
			e.printStackTrace();
		}
	}
	
	public static void wipeCache() {
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			
			os.write("mkdir -p /cache/recovery/\n".getBytes());
			os.write("echo 'boot-recovery' >/cache/recovery/command\n".getBytes());
			String cmd = "echo '--wipe_cache' >> /cache/recovery/command\n";
			os.write(cmd.getBytes());
			
			os.write("reboot recovery\n".getBytes());
			os.flush();
		} catch (Exception e) {
			Log.e(TAG,"Unable to reboot into Recovery mode for wiping cache");
			e.printStackTrace();
		}
	}
	
	public static void wipeData() {
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			
			os.write("mkdir -p /cache/recovery/\n".getBytes());
			os.write("echo 'boot-recovery' >/cache/recovery/command\n".getBytes());
			String cmd = "echo '--wipe_data' >> /cache/recovery/command\n";
			os.write(cmd.getBytes());
			
			os.write("reboot recovery\n".getBytes());
			os.flush();
		} catch (Exception e) {
			Log.e(TAG,"Unable to reboot into Recovery mode for wiping data");
			e.printStackTrace();
		}
	}
	
	public static void recoveryMode() {
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			
			os.write("reboot recovery\n".getBytes());
			os.flush();
		} catch (Exception e) {
			Log.e(TAG,"Unable to reboot into Recovery mode");
			e.printStackTrace();
		}
	}
	
	public static void doBackup(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String backupFolder = "/sdcard/"+preferences.getString("backup_folder", "clockworkmod/backup");
		if(!backupFolder.endsWith("/"))
			backupFolder += "/";
		
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH.mm");
			Date date = new Date();
			
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			
			os.write("mkdir -p /cache/recovery/\n".getBytes());
			String mkdirCmd = "mkdir -p "+backupFolder+"\n";
			os.write(mkdirCmd.getBytes());
			
			os.write("rm /cache/recovery/command\n".getBytes());
			os.write("echo 'print \"ROM Updater Backup script\"\n' > /cache/recovery/extendedcommand".getBytes());
			String backupCommand = "echo 'backup_rom "+backupFolder+format.format(date)+"' >> /cache/recovery/extendedcommand\n";
			os.write(backupCommand.getBytes());

			os.write("reboot recovery\n".getBytes());
			os.flush();
		} catch (Exception e) {
			Log.e(TAG,"Unable to reboot into recovery for backup");
			e.printStackTrace();
		}
	}
}
