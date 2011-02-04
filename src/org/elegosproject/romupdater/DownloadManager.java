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

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadManager {
	private static final String TAG = "ROM Updater (DownloadPackage.class)";
	private static final String download_location = "/sdcard/";
	public static final String download_path = "romupdater/";
	
	public static boolean checkHttpFile(String url) {
		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters,3000);
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

			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters,3000);
			
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
}
