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
