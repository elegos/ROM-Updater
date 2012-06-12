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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.elegosproject.romupdater.types.AvailableVersion;
import org.elegosproject.romupdater.types.AvailableVersions;
import org.elegosproject.romupdater.types.ROMVersion;
import org.elegosproject.romupdater.types.ROMVersions;
import org.elegosproject.romupdater.types.RepoList;

import com.google.gson.Gson;

import android.text.TextUtils;
import android.util.Log;

public class JSONParser {
	private static final String TAG = "RomUpdater[JSONParser]";
	public String modName = "";
	public AvailableVersions parsedAvailableVersions;
	public ROMVersions parsedVersions;
	public Boolean failed = false;

	public static boolean checkRepository(String repository_url) {
		if(!repository_url.contains("://"))
			repository_url = "http://"+repository_url;
		if(!repository_url.contains("?") && !repository_url.contains("json")) {
			if (!repository_url.endsWith("/")) repository_url += "/";
			repository_url += "main.json";
		}
		return DownloadManager.checkHttpFile(repository_url);
	}
	
	public static InputStream getJSONData(String url) throws Exception {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters,3000);
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
            throw e;
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
	
	public Vector<ROMVersion> getROMVersions() {
		failed = false;

		Vector<ROMVersion> versions = new Vector<ROMVersion>();
		Gson gson = new Gson();

		try {
			parsedVersions = new ROMVersions();

			SharedData shared = SharedData.getInstance();
			Reader r = new InputStreamReader(shared.getInputStreamData());
			parsedVersions = gson.fromJson(r, ROMVersions.class);

			shared.setRepositoryROMName(parsedVersions.getName());
			shared.setRespositoryModel(parsedVersions.getPhoneModel());

		} catch(Exception e) {
			e.printStackTrace();
			failed = true;
		}

		if (failed) {
			Log.w(TAG, "Failed to parse ROMVersions !");
			return versions;
		}

		modName = parsedVersions.getName();
		if (TextUtils.isEmpty(modName)) {
			failed = true;
			Log.e(TAG, "Failed to parse ROMVersions ! (no name)");
			return versions;
                }

		List<ROMVersion> lst = parsedVersions.getVersions();
		for(ROMVersion rv : lst) {
			Log.i(TAG, "Version: " + rv.getVersion() + " - " + rv.getUri());
			Log.i(TAG, rv.getChangelog());
			versions.add(rv);
			rv.getUri();
		}
		return versions;
	}

	public Vector<AvailableVersion> getAvailableVersions() {
		failed = false;
		
		parsedAvailableVersions = new AvailableVersions();
		Vector<AvailableVersion> versions = new Vector<AvailableVersion>();
		Gson gson = new Gson();
		SharedData shared = SharedData.getInstance();
		
		// InputStream is given from the async task
		Reader r = new InputStreamReader(shared.getInputStreamData());
		try {
			parsedAvailableVersions = gson.fromJson(r, AvailableVersions.class);
		} catch (Exception e) {
			e.printStackTrace();
			failed = true;
		}
		
		if (failed) {
			failed = true;
			Log.w(TAG, "No available version !");
			return new Vector<AvailableVersion>();
		}

		List<AvailableVersion> lst = parsedAvailableVersions.getAvailableVersions();
		for(AvailableVersion av : lst) {
			Log.i(TAG, "Version: " + av.getVersion() + " ("+av.getUri()+")");
			versions.add(av);
		}

		return versions;
	}
	
	public RepoList[] getRepositoriesFromJSON() {
		failed = false;
		
		// get the json input stream from the async task
		SharedData shared = SharedData.getInstance();
		Reader r = new InputStreamReader(shared.getInputStreamData());
		
		RepoList[] theList;
		Gson gson = new Gson();
		try {
			theList = gson.fromJson(r, RepoList[].class);
			return theList;
		} catch (Exception e) {
			e.printStackTrace();
			failed = true;
			return new RepoList[0];
		}
	}
	
	public String getUrlForVersion(String version) {
		for(AvailableVersion av : parsedAvailableVersions.getAvailableVersions()) {
			if(av.getVersion().equals(version))
				return av.getUri();
		}
		return "";
	}
}
