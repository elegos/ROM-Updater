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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Vector;

import org.elegosproject.romupdater.types.AvailableVersion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import android.util.Log;

public class VersionSelector extends ROMSuperActivity {
	private static final String TAG = "RomUpdater[VersionSelector]";

	private SharedData shared;

	private String versionUri;
	private String changeLog;

	private ListView versionsAvailableListView;
	private TextView versionsTextView;

	private TextView changelogTitle;
	private TextView changelogTextView;

	private Vector<AvailableVersion> availableVersions;

	private JSONParser myParser = new JSONParser();

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		shared = SharedData.getInstance();
		versionUri = getIntent().getExtras().getString(PackageName + ".VersionSelector.versionUri");
		changeLog = getIntent().getExtras().getString(PackageName + ".VersionSelector.changeLog");

		if (TextUtils.isEmpty(versionUri)) {
			// forced repository, not compatible with original version
			versionUri = getApplicationContext().getString(R.string.reposerver_url);
		}

		setContentView(R.layout.version);

		versionsAvailableListView = (ListView)this.findViewById(R.id.availableVersions);
		versionsTextView = (TextView)this.findViewById(R.id.versionsTextView);

		changelogTitle    = (TextView)this.findViewById(R.id.versionChangeLog);
		changelogTextView = (TextView)this.findViewById(R.id.versionChangeLog);

		setVersionView(versionUri);

		versionsAvailableListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String selectedDownload = arg0.getItemAtPosition(arg2).toString();
				String file = "";
				if(selectedDownload.equals(getString(R.string.start_full_download)))
					file = myParser.parsedAvailableVersions.getFullUri();
				else
					file = myParser.getUrlForVersion(SharedData.LOCAL_VERSION);

				Log.w(TAG, "getUrlFor("+file+")");
				String url = getUrlFor(versionUri, file);

				shared.setDownloadedFile(DOWNLOAD_DIRECTORY+file);
				new DownloadFile().execute(url, DOWNLOAD_DIRECTORY+file);
			}
		});
	}

	public String urlParamEncode(String param)
	{
		String res;
		try {
			res = URLEncoder.encode(param, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			res = param;
		}
		return res;
	}

	/**
	 * Construct Full URL, allow dynamic pages
	 */
	public String getUrlFor(String versionUri, String file)
	{
		String url = "";
		SharedData sdata = SharedData.getInstance();

		if(!versionUri.contains("://")) {
			url = sdata.getRepositoryUrl();
		}
		url += versionUri;
		if(!url.contains("?") && !url.contains(".php")) {
			if(!url.endsWith("/")) url += "/";
			url += file;
		} else {
			url += sdata.getDownloadVersion();
			url += "&f=" + urlParamEncode(file);
		}
		return url;
	}

	public String getJsonUrlFor(String uri)
	{
		String url = "";
		SharedData sdata = SharedData.getInstance();
		if(!uri.contains("://")) {
			url = sdata.getRepositoryUrl();
		}
		url += uri;
		if(!url.contains("?") && !url.contains(".php")) {
			if(!url.endsWith("/")) url += "/";
			url += "mod.json";
		} else {
			//mod.php?v=...
			url += sdata.getDownloadVersion();
		}
		return url;
	}

	class CheckHttpFile extends AsyncTask<String, Integer, Boolean>
	{
		public boolean success=false;
		@Override
		protected Boolean doInBackground(String... params) {
			String urlToCheck = params[0];
			success = DownloadManager.checkHttpFile(urlToCheck);
			Log.d(TAG, "CheckHttpFile: "+success);
			return success;
		}
	}

	private void setVersionView(String versionUri) {
		versionsTextView.setText(shared.getDownloadVersion());

		if (!TextUtils.isEmpty(changeLog))
			changelogTextView.setText(changeLog);
		else {
			changelogTitle.setVisibility(View.GONE);
			changelogTextView.setVisibility(View.GONE);
		}

		String uri = getJsonUrlFor(versionUri);
		CheckHttpFile check = new CheckHttpFile();
		try {
			check.execute(uri);
			check.get();
		}
		catch (Exception e) {
			AlertDialog.Builder notFoundBuilder = new AlertDialog.Builder(VersionSelector.this);
			notFoundBuilder.setCancelable(false)
				.setTitle(getString(R.string.version_descriptor_not_found_title))
				.setMessage(getString(R.string.version_descriptor_not_found_message)+
				"\n" + e.toString()
				)
				.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
			AlertDialog dialog = notFoundBuilder.create();
			dialog.show();
			return;
		}
		if (!check.success) {
			// http header check failed
			Log.w(TAG, "CheckHttpFile reported a failure !");
			return;
		}
		new DownloadJSON().execute(uri);
	}

	@Override
	void onDownloadComplete(Boolean success) {
		super.onDownloadComplete(success);
		Log.v(TAG, "DownloadComplete success="+success);

		// download exit with true -> success
		if(success) {
			// the ROM name is different from the
			// actual one, ask to wipe or backup and wipe before
			final SharedData sdata = SharedData.getInstance();
			AlertDialog.Builder alert = new AlertDialog.Builder(VersionSelector.this);

			if(!SharedData.LOCAL_ROMNAME.contains(shared.getRepositoryROMName())) {

				alert.setMessage(getString(R.string.ask_backup_wipe));
				alert.setPositiveButton(getString(R.string.backup_and_wipe), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// backup and wipe, then update
						sdata.setRecoveryOperations(3);
						RecoveryManager.doBackup(VersionSelector.this);
						RecoveryManager.wipeData();
						RecoveryManager.addUpdate(sdata.getDownloadedFile());
						RecoveryManager.rebootRecovery();
					}
				});
				alert.setNeutralButton(getString(R.string.backup_only), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Backup only, then update
						sdata.setRecoveryOperations(2);
						RecoveryManager.doBackup(VersionSelector.this);
						RecoveryManager.addUpdate(sdata.getDownloadedFile());
						RecoveryManager.rebootRecovery();
					}
				});
				alert.setNegativeButton(getString(R.string.install), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Just update
						sdata.setRecoveryOperations(1);
						RecoveryManager.addUpdate(sdata.getDownloadedFile());
						RecoveryManager.rebootRecovery();
					}
				});
			} else {
				// ROM name are the same, just ask to install now
				alert.setMessage(getString(R.string.upgrade_confirmation))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// apply update
							sdata.setRecoveryOperations(1);
							RecoveryManager.addUpdate(sdata.getDownloadedFile());
							RecoveryManager.rebootRecovery();
						}
					})
					.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// end the activity
							dialog.dismiss();
							finish();
						}
					});
			}
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(VersionSelector.this);


			// create and show the dialog
			alert.create().show();

			// send anonymous data (if accepted)
			if(preferences.getBoolean("anon_stats", false)) {
				DownloadManager dm = new DownloadManager();
				dm.sendAnonymousData(getApplicationContext());
			}

		} else {
			// download failed
			// alert the user and delete the file
			AlertDialog.Builder error = new AlertDialog.Builder(VersionSelector.this);
			error.setMessage(getString(R.string.error_download_file))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedData sdata = SharedData.getInstance();
						// delete the corrupted file, if any
						File toDelete = new File(sdata.getDownloadedFile());
						toDelete.delete();

						// dismiss
						dialog.dismiss();
					}
				});

			// create and show the dialog
			error.create().show();
		}
	}

	@Override
	void onJSONDataDownloaded(Boolean success) {
		super.onJSONDataDownloaded(success);
		// super class popups an error
		// and finishes the activity, so just return
		if(!success)
			return;

		availableVersions = myParser.getAvailableVersions();

		// JSON parse failed, alert and return
		if(myParser.failed){
			AlertDialog.Builder error = new AlertDialog.Builder(VersionSelector.this);
			error.setMessage(getString(R.string.error_json_download))
				.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			error.create().show();
			finish();
		}

		Vector<String>versionsList = new Vector<String>();
		Iterator<AvailableVersion> versionsIterator = availableVersions.iterator();
		String iteratorVersion = "";

		// The "Full" element is always present
		versionsList.add(getString(R.string.start_full_download));
		// Search for an incremental update, in case add it to the list
		if (SharedData.LOCAL_ROMNAME != null &&
		    SharedData.LOCAL_ROMNAME.contains(shared.getRepositoryROMName()))
		{
			while(versionsIterator.hasNext()) {
				iteratorVersion = versionsIterator.next().getVersion();
				try {
					if (SharedData.LOCAL_VERSION != null &&
					    SharedData.LOCAL_VERSION.equals(iteratorVersion))
					{
						versionsList.add("Incremental");
						Log.w(TAG, "adding incremental "+iteratorVersion);
						break;
					}
				} catch (NumberFormatException e) {
					Log.w(TAG, "ignoring incremental "+iteratorVersion);
					break;
				}
			}
		}

		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, versionsList);
		versionsAvailableListView.setAdapter(adapter);
	}
}
