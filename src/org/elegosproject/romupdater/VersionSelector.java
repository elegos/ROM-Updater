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

import java.util.Iterator;
import java.util.Vector;

import org.elegosproject.romupdater.types.AvailableVersion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class VersionSelector extends Activity {
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
				else file = myParser.getUrlForVersion(shared.getDownloadVersion());
				
	   			DownloadPackage download = new DownloadPackage();
				
				if(!download.downloadFile(versionUri, file, VersionSelector.this)) {
					AlertDialog.Builder errorDialog = new AlertDialog.Builder(VersionSelector.this);
					errorDialog.setMessage(getString(R.string.repository_file_not_found))
						.setCancelable(false)
						.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
					AlertDialog dialog = errorDialog.create();
					dialog.show();
				}
			}
		});
	}

	private void setVersionView(String versionUri) {
		versionsTextView.setText(getString(R.string.capital_version)+" "+shared.getDownloadVersion());
		if(!DownloadPackage.checkHttpFile(shared.getRepositoryUrl()+versionUri+"/mod.json")) {
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
		availableVersions = myParser.getAvailableVersions(shared.getRepositoryUrl()+versionUri+"/mod.json");
		Vector<String>versionsList = new Vector<String>();
		Iterator<AvailableVersion> versionsIterator = availableVersions.iterator();
		String iteratorVersion = "";
		
		versionsList.add("Full");
		if(SharedData.LOCAL_ROMNAME.equals(shared.getRepositoryROMName()))
			while(versionsIterator.hasNext()) {
				iteratorVersion = versionsIterator.next().getVersion();
				if(Integer.parseInt(SharedData.LOCAL_ROMNAME) == Integer.parseInt(iteratorVersion)) {
					versionsList.add("Incremental");
					break;
				}
			}

		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, versionsList);
		versionsAvailableListView.setAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_exit:
			finish();
			return true;
		case R.id.menu_info:
			AlertDialog.Builder builder = new AlertDialog.Builder(VersionSelector.this);
			builder.setIcon(R.drawable.ic_menu_info)
				.setTitle(R.string.menu_info_title)
				.setMessage("ROM Updater by elegos\n\nThis is a freeware, banner-free software.\nPlease donate via PayPal to giacomo.furlan@fastwebnet.it.\n\nThanks\nGiacomo 'elegos' Furlan")
				.setCancelable(false)
				.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
