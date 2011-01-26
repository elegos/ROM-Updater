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

public class SharedData {
	static final Object mLock = new Object();
	static SharedData mInstance;

	public static final String LOCAL_MODEL = android.os.Build.MODEL;
	public static final String LOCAL_ROMNAME = android.os.Build.DISPLAY;
	public static final String LOCAL_VERSION = android.os.Build.VERSION.INCREMENTAL;

	private String repositoryModel;
	private String repositoryROMName;
	private String downloadVersion;
	
	private String repositoryUrl;

	private SharedData() {
		setRepositoryROMName("");
		setRespositoryModel("");
		setDownloadVersion("");
	}

	public static SharedData getInstance() {
		synchronized (mLock) {
			if (mInstance == null) {
				mInstance = new SharedData();
			}
			return mInstance;
		}
	}
	
	public void setRespositoryModel(String repository) {
		repositoryModel = repository;
	}
	
	public void setRepositoryROMName(String name) {
		repositoryROMName = name;
	}
	
	public void setDownloadVersion(String version) {
		downloadVersion = version;
	}
	
	public void setRepositoryUrl(String url) {
		if(!url.startsWith("http://"))
			url = "http://"+url;
		if(!url.endsWith("/"))
			url += "/";
		repositoryUrl = url;
	}
	
	public String getRepositoryModel() {
		return repositoryModel;
	}
	
	public String getRepositoryROMName() {
		return repositoryROMName;
	}
	
	public String getDownloadVersion() {
		return downloadVersion;
	}
	
	public String getRepositoryUrl() {
		return repositoryUrl;
	}
}
