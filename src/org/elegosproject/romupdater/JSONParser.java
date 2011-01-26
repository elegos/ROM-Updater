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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.elegosproject.romupdater.types.AvailableVersion;
import org.elegosproject.romupdater.types.AvailableVersions;
import org.elegosproject.romupdater.types.ROMVersion;
import org.elegosproject.romupdater.types.ROMVersions;

import com.google.gson.Gson;

import android.util.Log;

public class JSONParser {
	private static final String TAG = "ROM Updater (JSONParser.class)";
	public String modName = "";
	public AvailableVersions parsedAvailableVersions;
	public ROMVersions parsedVersions;
	
	public static boolean checkRepository(String repository_url) {
		if(!repository_url.startsWith("http://"))
			repository_url = "http://"+repository_url;
		if(!repository_url.endsWith("/"))
			repository_url += "/";
		repository_url += "main.json";
		return DownloadPackage.checkHttpFile(repository_url);
	}
	
	public InputStream getJSONData(String url) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		URI uri;
        InputStream data = null;
        try {
            uri = new URI(url);
            HttpGet method = new HttpGet(uri);
            HttpResponse response = httpClient.execute(method);
            data = response.getEntity().getContent();
        } catch (Exception e) {
            Log.e(TAG,"Unable to download file: "+e);
            data = null;
        }
        return data;
	}
	
	public String getROMVersionUri(String version) {
		for(ROMVersion rv : parsedVersions.getVersions()) {
			if(rv.getVersion().equals(version))
				return rv.getUri();
		}
		return "";
	}
	
	public Vector<ROMVersion> getROMVersions(String url){
		Vector<ROMVersion> versions = new Vector<ROMVersion>();
    	Log.i(TAG,"Requesting "+url+"...");
    	Gson gson = new Gson();
    	Reader r = new InputStreamReader(this.getJSONData(url));
        try{
        	parsedVersions = gson.fromJson(r, ROMVersions.class);
        	
        	SharedData shared = SharedData.getInstance();
        	shared.setRepositoryROMName(parsedVersions.getName());
        	shared.setRespositoryModel(parsedVersions.getPhoneModel());
        } catch(Exception e) {
            e.printStackTrace();
            return new Vector<ROMVersion>();
        }
        
        modName = parsedVersions.getName();
    	for(ROMVersion rv : parsedVersions.getVersions()){
    		Log.i(TAG, "Version: " + rv.getVersion() + " - " + rv.getUri());
    		Log.i(TAG, rv.getChangelog());
    		versions.add(rv);
    		rv.getUri();
    	}
    	
        return versions;
    }
	
	public Vector<AvailableVersion> getAvailableVersions(String url) {
		parsedAvailableVersions = new AvailableVersions();
		Vector<AvailableVersion> versions = new Vector<AvailableVersion>();
		Log.i(TAG,"Requesting "+url+"...");
		Gson gson = new Gson();
		Reader r = new InputStreamReader(this.getJSONData(url));
		try {
			parsedAvailableVersions = gson.fromJson(r, AvailableVersions.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(AvailableVersion av : parsedAvailableVersions.getAvailableVersions()) {
			Log.i(TAG, "Version: " + av.getVersion() + " ("+av.getUri()+")");
			versions.add(av);
		}
		
		return versions;
	}
	
	public String getUrlForVersion(String version) {
		for(AvailableVersion av : parsedAvailableVersions.getAvailableVersions()) {
			if(av.getVersion().equals(version))
				return av.getUri();
		}
		return "";
	}
}
