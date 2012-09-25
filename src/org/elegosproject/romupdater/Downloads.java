package org.elegosproject.romupdater;

import java.io.File;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Downloads extends ROMSuperActivity {
	private static final String TAG = "RomUpdater[Downloads]";

	private ListView listOfDownloads;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.downloads);

		listOfDownloads = (ListView) findViewById(R.id.listOfDownloads);
		createDownloadsList();
		
		listOfDownloads.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				final String selected = arg0.getItemAtPosition(arg2).toString();
				AlertDialog.Builder confirm = new AlertDialog.Builder(Downloads.this);
				confirm.setCancelable(true)
					.setMessage(getString(R.string.upgrade_confirmation))
					.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// install the update
							SharedData sdata = SharedData.getInstance();
							sdata.setRecoveryOperations(2);
							RecoveryManager.setupExtendedCommand();
							RecoveryManager.addUpdate(DOWNLOAD_DIRECTORY+selected);
							RecoveryManager.rebootRecovery();
						}
					})
					.setNeutralButton(getString(R.string.delete_zip), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// delete the zip file, then recreate the list
							File toDelete = new File(DOWNLOAD_DIRECTORY+selected);
							toDelete.delete();
							createDownloadsList();
						}
					})
					.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// cancel
							dialog.dismiss();
						}
					});
				// popup the dialog
				confirm.create().show();
			}
		});
	}
	
	private void createDownloadsList() {
		File dir = new File(DOWNLOAD_DIRECTORY);
		
		// if the download directory doesn't exist,
		// it means that nothing has been downloaded.
		if(!dir.isDirectory() || dir.list().length == 0) {
			AlertDialog.Builder info = new AlertDialog.Builder(Downloads.this);

			info.setMessage(getString(R.string.info_no_downloads))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
			info.create().show();
			return;
		}
		
		// The directory exists and it's not empty
		Vector<String> zipFiles = new Vector<String>();
		
		// Cycle for each file and check zip files
		for(String f : dir.list()) {
			if(f.endsWith(".zip"))
				zipFiles.add(f);
		}
		
		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, zipFiles);
		listOfDownloads.setAdapter(adapter);
	}

}
