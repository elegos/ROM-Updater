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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadPackage {
	private static final String TAG = "ROM Updater (DownloadPackage.class)";
	private static final String download_location = "/sdcard/";
	public static final String download_path = "romupdater/";
	
	public static boolean checkHttpFile(String url) {
		try {
			Log.i(TAG,"Testing "+url+"...");
			URL theUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) theUrl.openConnection();
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				connection.disconnect();
			} else { Log.i(TAG,"HTTP Response code: "+connection.getResponseCode()); return false; }
		} catch (IOException e) {
			Log.e(TAG,e.toString());
			return false;
		}
		return true;
	}
	
	public static boolean sendAnonymousData() {
		String link = "http://www.elegosproject.org/android/upload.php";
		String data;
		
		SharedData shared = SharedData.getInstance();
		String romName = shared.getRepositoryROMName();
		String romVersion = shared.getDownloadVersion();
		String romPhone = shared.getRepositoryModel();
		String romRepository = shared.getRepositoryUrl();
		
		if(romName.equals("") ||
				romVersion.equals("") ||
				romPhone.equals("") ||
				romRepository.equals("")) {
			Log.e(TAG,"Internal error - missing system variables.");
			return false;
		}
		
		if(!checkHttpFile(link)) return false;
		try {
			data = URLEncoder.encode("phone", "UTF-8") + "=" + URLEncoder.encode(romPhone, "UTF-8");
			data += "&"+URLEncoder.encode("rom_name", "UTF-8") + "=" + URLEncoder.encode(romName, "UTF-8");
			data += "&"+URLEncoder.encode("rom_version", "UTF-8") + "=" + URLEncoder.encode(romVersion,"UTF-8");
			data += "&"+URLEncoder.encode("rom_repository", "UTF-8") + "=" + URLEncoder.encode(romRepository,"UTF-8");
			
			URL url = new URL(link);
			url.openConnection();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", "ROMUpdater");
			conn.setDoOutput(true);
			PrintWriter out = new PrintWriter(conn.getOutputStream());
			out.println(data);
			out.close();
			
			int status = Integer.parseInt(conn.getHeaderField("ROMUpdater-status"));
			if(status == 1)
				return true;

			Log.e(TAG, "It was impossible to send data to the stastistics server ("+conn.getHeaderField("ROMUpdater-error")+").");
			return false;
				
		} catch (Exception e) {
			Log.e(TAG, "It was impossible to send data to the stastistics server.");
			Log.e(TAG, "Error: "+e.toString());
			return false;
		}
	}
	
	public boolean downloadFile(String path, final String fileName, final Context theContext) {
		SharedData shared = SharedData.getInstance();
		
		String repository = shared.getRepositoryUrl();
		
		if(!path.substring(path.length()-1).equals("/"))
			path += "/";
		Log.i(TAG,"Trying to get file "+repository+path+fileName);
		if(!checkHttpFile(repository+path+fileName))
			return false;
		
		try {
			URL url = new URL(repository+path+fileName);
			final URLConnection connection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				File file = new File(download_location+download_path);
				file.mkdirs();
				
				
				int size = connection.getContentLength();
				
				final ProgressDialog progress = new ProgressDialog(theContext);
				progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progress.setMessage("Downloading "+fileName+"...");
				progress.setCancelable(false);
				progress.setMax(size);
				progress.setOnDismissListener(new OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(theContext);
			    		progressDialogBuilder.setMessage(theContext.getString(R.string.upgrade_confirmation))
			    			.setCancelable(true)
			    			.setPositiveButton(theContext.getString(R.string.upgrade_ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(theContext);
									if(preferences.getBoolean("anon_stats", false)) {
										Log.i(TAG, "Sending anonymous data.");
										sendAnonymousData();
									}
									RecoveryManager.setupExtendedCommand();
									RecoveryManager.addUpdate(download_path+fileName);
									RecoveryManager.rebootRecovery();
								}
							})
							.setNegativeButton(theContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
			    		AlertDialog progressDialog = progressDialogBuilder.create();
			    		progressDialog.show();
					}
				});
				progress.show();
				new Thread() {
					public void run() {
						int index = 0;
						int current = 0;
						
						try {
							FileOutputStream output = new FileOutputStream(download_location+download_path+fileName, false);
							InputStream input = connection.getInputStream();
							BufferedInputStream buffer = new BufferedInputStream(input);
							byte[] bBuffer = new byte[10240];
							
							while((current = buffer.read(bBuffer)) != -1) {
								try {
									output.write(bBuffer, 0, current);
								} catch (IOException e) {
									e.printStackTrace();
								}
								index += current;
								progress.setProgress(index);
							}
							output.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						progress.dismiss();
					}
				}.start();
				
				return true;
			}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
