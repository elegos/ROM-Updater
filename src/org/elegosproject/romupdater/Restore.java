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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Restore extends ROMSuperActivity {
	private static final String TAG = "RomUpdater[Restore]";

	private ListView listOfRestores;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.restore);
		listOfRestores = (ListView) findViewById(R.id.listOfRestores);
		createRestoreList();
		
		listOfRestores.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				final String selected = arg0.getItemAtPosition(arg2).toString();
				AlertDialog.Builder confirm = new AlertDialog.Builder(Restore.this);
				confirm.setCancelable(true);
				confirm.setMessage(getString(R.string.confirm_restore_backup));
				// execute the backup
				confirm.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String backupDirectory;
						SharedData shared = SharedData.getInstance();
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Restore.this);
						
						backupDirectory = prefs.getString("backup_folder","");
						if(!backupDirectory.endsWith("/"))
							backupDirectory += "/";
						
						// something like [/sdcard/][my_folder/of_backups/][backup_folder]
						backupDirectory = "/sdcard/"+backupDirectory+selected;
						
						shared.setRecoveryOperations(2);
						RecoveryManager.setupExtendedCommand();
						RecoveryManager.restoreBackup(backupDirectory);
						RecoveryManager.rebootRecovery();
					}
				});
				
				// delete the backup
				confirm.setNeutralButton(getString(R.string.delete_backup), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String backupDirectory;
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Restore.this);
						
						backupDirectory = prefs.getString("backup_folder","");
						if(!backupDirectory.endsWith("/"))
							backupDirectory += "/";
						
						// something like [/sdcard/][my_folder/of_backups/][backup_folder]
						backupDirectory = "/sdcard/"+backupDirectory+selected;
						
						File backup = new File(backupDirectory);
						// delete the backup directory
						Restore.deleteDirectory(backup);
						dialog.dismiss();
						// recreate the list
						createRestoreList();
					}
				});
				
				// cancel the restore
				confirm.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				confirm.create().show();
			}
		});
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
	
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }
}
