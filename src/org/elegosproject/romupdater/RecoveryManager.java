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
	
	public static void rebootRecovery() {
		SharedData sdata = SharedData.getInstance();
		
		while(sdata.getRecoveryCounter() < sdata.getRecoveryOperations()) {
			while(sdata.getLockProcess());
		}
		
		sdata.addRecoveryMessage("reboot recovery\n");
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
	
	public static void setupExtendedCommand() {
		SharedData sdata = SharedData.getInstance();
		
		while(sdata.getLockProcess());
		sdata.setLockProcess(true);
		
		sdata.addRecoveryMessage("mkdir -p /cache/recovery/\n");
		sdata.addRecoveryMessage("echo 'boot-recovery' >/cache/recovery/command\n");
		sdata.addRecoveryMessage("echo 'print ROM Updater by elegos' > /cache/recovery/extendedcommand\n");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}
	
	public static void addUpdate(String file) {
		SharedData sdata = SharedData.getInstance();
		
		while(sdata.getLockProcess());
		sdata.setLockProcess(true);
		
		if(file.startsWith("/sdcard/"))
			file = file.substring(8);
		
		sdata.addRecoveryMessage("print Installing file SDCARD:"+file+"\n");
		sdata.addRecoveryMessage("echo 'install_zip SDCARD:"+file+"' >> /cache/recovery/extendedcommand\n");
		
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
			sdata.addRecoveryMessage("echo 'print Backing up the current ROM to \""+backupFolder+format.format(date)+"\"' >> /cache/recovery/extendedcommand\n");
			sdata.addRecoveryMessage("echo 'backup_rom "+backupFolder+format.format(date)+"' >> /cache/recovery/extendedcommand\n");
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
		
		sdata.addRecoveryMessage("print Restoring ROM from SDCARD:"+backupDirectory+"\n");
		sdata.addRecoveryMessage("echo 'restore_rom /sdcard/"+backupDirectory+"' >> /cache/recovery/extendedcommand\n");
		
		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}
	
	public static void wipeCache() {
		SharedData sdata = SharedData.getInstance();
		
		while(sdata.getLockProcess());
		sdata.setLockProcess(true);
		
		sdata.addRecoveryMessage("echo 'print Wiping CACHE' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'delete_recursive DATA:dalvik-cache' >> /cache/recovery/extendedcommand\n");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}
	
	public static void wipeData() {
		SharedData sdata = SharedData.getInstance();
		while(sdata.getLockProcess());
		
		sdata.setLockProcess(true);
		sdata.addRecoveryMessage("echo 'print Wiping USER DATA' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'delete_recursive DATA:' >> /cache/recovery/extendedcommand\n");
		
		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}
	
	public static void wipeSDExt() {
		SharedData sdata = SharedData.getInstance();
		
		while(sdata.getLockProcess());
		sdata.setLockProcess(true);

		sdata.addRecoveryMessage("echo 'print Wiping SD-EXT' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'delete_recursive SDEXT:app' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'delete_recursive SDEXT:app-private' >> /cache/recovery/extendedcommand\n");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}
	
	// Command section
	
	public static void setupCommand() {
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			
			os.write("mkdir -p /cache/recovery/\n".getBytes());
			os.write("echo 'boot-recovery' >/cache/recovery/command\n".getBytes());
			
			os.flush();
		} catch (Exception e) {
			Log.e(TAG,"Unable to reboot into Recovery mode for wiping cache");
			e.printStackTrace();
		}
	}
}
