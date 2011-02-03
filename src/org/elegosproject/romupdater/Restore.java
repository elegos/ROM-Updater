package org.elegosproject.romupdater;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Restore extends ROMSuperActivity {
	private ListView listOfRestores;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.restore);
		listOfRestores = (ListView) findViewById(R.id.listOfRestores);
		createRestoreList();
	}
	
	private void createRestoreList() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String bFolder = prefs.getString("backup_folder", "");
		if(!bFolder.endsWith("/")) bFolder += "/";
		File dir = new File("/sdcard/"+bFolder);
		// check if the directory actually exists and it's a directory
		if(dir.isDirectory()) {
			String[] files = dir.list();
			if(files.length > 0) {
				Vector<String>restoreList = new Vector<String>();
				for(int i = 0; i < files.length; i++) {
					// we don't want hidden folders
					if(files[i].startsWith(".")) continue;
					File f = new File("/sdcard/"+bFolder+files[i]);
					if(f.isDirectory())
						restoreList.add(files[i]);
				}
				// Final list not empty
				if(!restoreList.isEmpty()) {
					// from newer to older
					Comparator<String> r = Collections.reverseOrder();
					Collections.sort(restoreList,r);
					
					ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, restoreList);
					listOfRestores.setAdapter(adapter);
				} else {
					// the directory is not containing any backup folder
					// alert and finish();
					AlertDialog.Builder noBackup = new AlertDialog.Builder(Restore.this);
					noBackup.setMessage(getString(R.string.error_backup_folder_empty));
					noBackup.setCancelable(false);
					noBackup.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
							return;
						}
					});
					noBackup.create().show();
				}
			} else {
				// the directory is not populated, alert and finish();
				AlertDialog.Builder noBackup = new AlertDialog.Builder(Restore.this);
				noBackup.setMessage(getString(R.string.error_backup_folder_empty));
				noBackup.setCancelable(false);
				noBackup.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
						return;
					}
				});
				noBackup.create().show();
			}
		} else {
			// print an alert: the backup directory doesn't exist
			// 	               or is not a directory, then finish();
			AlertDialog.Builder dirNotExists = new AlertDialog.Builder(Restore.this);
			dirNotExists.setMessage(getString(R.string.error_backup_folder_doesnt_exist));
			dirNotExists.setCancelable(false);
			dirNotExists.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
					return;
				}
			});
			dirNotExists.create().show();
		}
	}
}
