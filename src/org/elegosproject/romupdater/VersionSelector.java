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
import java.util.Iterator;
import java.util.Vector;

import org.elegosproject.romupdater.types.AvailableVersion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class VersionSelector extends ROMSuperActivity {
	private SharedData shared;
	
	private String versionUri;

	private ListView versionsAvailableListView;
	private TextView versionsTextView;
	private Vector<AvailableVersion> availableVersions;
	
	private JSONParser myParser = new JSONParser();
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		shared = SharedData.getInstance();
		versionUri = getIntent().getExtras().getString("org.elegosproject.romupdater.VersionSelector.versionUri");

		setContentView(R.layout.version);
		
		versionsAvailableListView = (ListView)this.findViewById(R.id.availableVersions);
		versionsTextView = (TextView)this.findViewById(R.id.versionsTextView);
		
		setVersionView(versionUri);
		
		versionsAvailableListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String selectedDownload = arg0.getItemAtPosition(arg2).toString();
				String file = "";
				if(selectedDownload.equals("Full"))
					file = myParser.parsedAvailableVersions.getFullUri();
				else file = myParser.getUrlForVersion(SharedData.LOCAL_VERSION);
				
				SharedData sdata = SharedData.getInstance();
				String url = sdata.getRepositoryUrl();
				if(!url.endsWith("/")) url += "/";
				url += versionUri;
				if(!url.endsWith("/")) url += "/";
				url += file;
				
				sdata.setDownloadedFile(DOWNLOAD_DIRECTORY+file);
				
				new DownloadFile().execute(url, DOWNLOAD_DIRECTORY+file);
			}
		});
	}
	
	@Override
	void onDownloadComplete(Boolean success) {
		// download exit with true -> success
		if(success) {
			// the ROM name is different from the
			// actual one, ask to wipe or backup and wipe before
			final SharedData sdata = SharedData.getInstance();
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			
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
				alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
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
			// send anonymous data (if accepted)
			if(preferences.getBoolean("anon_stats", false))
				DownloadManager.sendAnonymousData();

			// create and show the dialog
			alert.create().show();
		} else {
			// download failed
			// alert the user and delete the file
			AlertDialog.Builder error = new AlertDialog.Builder(this);
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

	private void setVersionView(String versionUri) {
		versionsTextView.setText(getString(R.string.capital_version)+" "+shared.getDownloadVersion());
		if(!DownloadManager.checkHttpFile(shared.getRepositoryUrl()+versionUri+"/mod.json")) {
			AlertDialog.Builder notFoundBuilder = new AlertDialog.Builder(VersionSelector.this);
			notFoundBuilder.setCancelable(false)
				.setTitle(getString(R.string.version_descriptor_not_found_title))
				.setMessage(getString(R.string.version_descriptor_not_found_message))
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
		
		new DownloadJSON().execute(shared.getRepositoryUrl()+versionUri+"/mod.json");
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
		versionsList.add("Full");
		// Search for an incremental update, in case add it to the list
		if(SharedData.LOCAL_ROMNAME.contains(shared.getRepositoryROMName()))
			while(versionsIterator.hasNext()) {
				iteratorVersion = versionsIterator.next().getVersion();
				if(Integer.parseInt(SharedData.LOCAL_VERSION) == Integer.parseInt(iteratorVersion)) {
					versionsList.add("Incremental");
					break;
				}
			}

		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, versionsList);
		versionsAvailableListView.setAdapter(adapter);
	}
}
