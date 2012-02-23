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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import org.elegosproject.romupdater.types.ROMVersion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class VersionsList extends ROMSuperActivity {
	private static final String TAG = "ROM Updater (VersionsList.class)";
	
	private SharedPreferences preferences;
	private SharedData shared;
	
	private JSONParser myParser = new JSONParser();
	private ListView versionsListView;
	
	private Vector<ROMVersion> modVersions;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.versions_list);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		shared = SharedData.getInstance();
		
		String prefRepo = preferences.getString("repository_url", getString(R.string.reposerver_url));
		if(!TextUtils.isEmpty(prefRepo))
			shared.setRepositoryUrl(prefRepo);
		else {
			String buildRepo = BuildParser.parseString("ro.romupdater.repository");
			shared.setRepositoryUrl(buildRepo);
		}

		// repository not set
		if(TextUtils.isEmpty(shared.getRepositoryUrl())) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setMessage(getString(R.string.error_repository_not_set))
			.setCancelable(false)
			.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent settings = new Intent(VersionsList.this, Preferences.class);
					startActivity(settings);
					finish();
				}
			});
			AlertDialog alert = dialog.create();
			alert.show();
			return;
		}

		Log.i(TAG, "Repository set to " + shared.getRepositoryUrl());
		
		versionsListView = (ListView)this.findViewById(R.id.versionsList);
		Toast t = Toast.makeText(this, getString(R.string.changelog_toast),Toast.LENGTH_LONG);
		t.show();
		setMainView();

		versionsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				String selectedItem = parent.getItemAtPosition(position).toString();
				Log.i(TAG,"ITEM: "+selectedItem);
				
				String version;
				if (selectedItem.lastIndexOf(" ") > 0)
					version = selectedItem.substring(selectedItem.lastIndexOf(" ")+1);
				else
					version = selectedItem;

				String changelog = getChangeLog(version);

				AlertDialog.Builder dialog = new AlertDialog.Builder(VersionsList.this);
				dialog.setMessage(selectedItem+" changelog:\n\n"+changelog)
					.setCancelable(false)
					.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							dialog = null;
						}
					});
				AlertDialog alert = dialog.create();
				alert.show();
				
				return true;
			}
		});
		
		versionsListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				String selectedVersion = arg0.getItemAtPosition(arg2).toString();
				Log.i(TAG,"Item selected: "+selectedVersion);
				shared.setDownloadVersion(selectedVersion);

				Intent selector = new Intent(VersionsList.this, VersionSelector.class);
				String dlVersion =  myParser.getROMVersionUri(shared.getDownloadVersion());
				selector.putExtra(PackageName + ".VersionSelector.versionUri", dlVersion);
				selector.putExtra(PackageName + ".VersionSelector.changeLog", getChangeLog(selectedVersion));
				startActivity(selector);
			}
		});
	}

	private String getChangeLog(String version) {
		String changelog = "";
		ROMVersion currentVersion = new ROMVersion();
		Iterator<ROMVersion> iVersion = modVersions.iterator();
		while(iVersion.hasNext()) {
			currentVersion = iVersion.next();
			if(currentVersion.getVersion().equals(version)) {
				changelog = currentVersion.getChangelog();
				break;
			}
		}
		return changelog;
	}

	private void setMainView() {
		String repositoryUrl = shared.getRepositoryUrl();
		
		// repository URL is void -> must set it before, finish
		if(TextUtils.isEmpty(repositoryUrl)) {
			AlertDialog.Builder error = new AlertDialog.Builder(VersionsList.this);
			error.setMessage(getString(R.string.error_repository_not_set))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent settings = new Intent(VersionsList.this, Preferences.class);
						startActivity(settings);
					}
				});
			error.create().show();
			finish();
		}
		if (!repositoryUrl.contains("json") && !repositoryUrl.contains("php") && !repositoryUrl.contains("?"))
			new DownloadJSON().execute(repositoryUrl+"main.json");
		else
			new DownloadJSON().execute(repositoryUrl);
	}
	@Override
	void onJSONDataDownloaded(Boolean success) {
		super.onJSONDataDownloaded(success);

		// download failed
		// activity ends from superclass
		if(!success) {
			return;
		}

		modVersions = myParser.getROMVersions();

		// JSON parse failed, alert and return
		if(myParser.failed){
			AlertDialog.Builder error = new AlertDialog.Builder(VersionsList.this);
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
		
		/* Global variables */
		shared.setRespositoryModel(myParser.parsedVersions.getPhoneModel());
		shared.setRepositoryROMName(myParser.parsedVersions.getName());

		// the repository is not for the current model (and not empty for common stuff)
		if(!shared.getRepositoryModel().equals(SharedData.LOCAL_MODEL) && !TextUtils.isEmpty(shared.getRepositoryModel())) {
			AlertDialog.Builder modelAlert = new AlertDialog.Builder(this);
			modelAlert.setCancelable(false)
				.setMessage(getString(R.string.error_model_mismatch))
				.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
			modelAlert.create().show();
			return;
		}
		
		// Vector of versions, one per line
		Vector<String>versionsList = new Vector<String>();

		Iterator<ROMVersion> versionsIterator = modVersions.iterator();
		// 2. insert the versions in a vector
		String iteratorVersion = "";
		// iterate through the JSON result
		while(versionsIterator.hasNext()) {
			iteratorVersion = versionsIterator.next().getVersion();
			// if same ROM name and greater version
			// or different ROM name, add the update to the vector
			try {
				if(!SharedData.LOCAL_ROMNAME.contains(shared.getRepositoryROMName()) ||
					(SharedData.LOCAL_ROMNAME.contains(shared.getRepositoryROMName()) && Integer.parseInt(SharedData.LOCAL_VERSION) < Integer.parseInt(iteratorVersion)))
					versionsList.add(iteratorVersion);
			} catch (NumberFormatException e) {
				versionsList.add(iteratorVersion);
			}
		}

		// sort the vector
		Comparator<String> r = Collections.reverseOrder();
		Collections.sort(versionsList,r);

		// ROM name differs between the local and the remote one
		// alert the user he'll only be able to download FULL versions
		if(!SharedData.LOCAL_ROMNAME.contains(shared.getRepositoryROMName()) && !versionsList.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(VersionsList.this);
			builder.setCancelable(true)
				.setMessage(getString(R.string.modname_mismatch) + "\n\n" +
					"Local:" + SharedData.LOCAL_ROMNAME  + "\n" +
					"Server:" + shared.getRepositoryROMName()
				)
				.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						dialog = null;
					}
				});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
		// if the vector is empty, alert and finish
		if(versionsList.isEmpty()) {
			AlertDialog.Builder updatedBuilder = new AlertDialog.Builder(VersionsList.this);
			updatedBuilder.setCancelable(true)
				.setMessage(getString(R.string.versionlist_is_empty))
				.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						dialog = null;
						finish();
					}
				});
			AlertDialog upToDateDialog = updatedBuilder.create();
			upToDateDialog.show();
			return;
		}

		// 3. set the versions list
		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, versionsList);
		versionsListView.setAdapter(adapter);
	}
}
