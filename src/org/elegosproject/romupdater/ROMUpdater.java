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

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ROMUpdater extends Activity {
	private static final String TAG = "ROM Updater (ROMUpdater.class)";
	
	private ListView actions;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        actions = (ListView)this.findViewById(R.id.listOfActions);
    	fillActionsList();
    	
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	if(!preferences.getBoolean("anon_stats_verified", false)) {
    		AlertDialog.Builder anonBuilder = new AlertDialog.Builder(this);
    		anonBuilder.setCancelable(false)
    			.setMessage(getString(R.string.anon_send_message))
    			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
					}
				})
				.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						Intent preferences = new Intent(ROMUpdater.this, Preferences.class);
						startActivity(preferences);
					}
				});
    		AlertDialog anonDialog = anonBuilder.create();
    		anonDialog.show();
    		Editor editor = preferences.edit();
    		editor.putBoolean("anon_stats_verified", true);
    		editor.commit();
    	}
    	
    	actions.setOnItemClickListener(new OnItemClickListener() {
    		SharedData sdata = SharedData.getInstance();
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
        		
				Log.i(TAG,arg0.getItemAtPosition(arg2).toString()+" ("+arg2+")");
				switch(arg2) {
				case 0:
					Intent versionsList = new Intent(ROMUpdater.this, VersionsList.class);
					startActivity(versionsList);
					break;
				case 1:
					Intent preferences = new Intent(ROMUpdater.this, Preferences.class);
					startActivity(preferences);
					break;
				case 2:
					AlertDialog.Builder dialog = new AlertDialog.Builder(ROMUpdater.this);
		    		dialog.setMessage(getString(R.string.wipe_cache_message))
		    			.setTitle(getString(R.string.wipe_cache))
		    			.setCancelable(true)
		    			.setPositiveButton(getString(R.string.wipe), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								sdata.setRecoveryOperations(2);
								RecoveryManager.setupExtendedCommand();
								RecoveryManager.wipeCache();
								
								RecoveryManager.rebootRecovery();
							}
						})
						.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
		    		
		    		AlertDialog alert = dialog.create();
		    		alert.show();
					break;
				case 3:
					final AlertDialog.Builder sdext = new AlertDialog.Builder(ROMUpdater.this);
					sdext.setMessage(getString(R.string.wipe_sdext_too))
						.setCancelable(false)
						.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								sdata.setRecoveryOperations(3);
								RecoveryManager.setupExtendedCommand();
								RecoveryManager.wipeData();
								RecoveryManager.wipeSDExt();
								
								RecoveryManager.rebootRecovery();
							}
						})
						.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								sdata.setRecoveryOperations(2);
								RecoveryManager.setupExtendedCommand();
								RecoveryManager.wipeData();
								
								RecoveryManager.rebootRecovery();
							}
						});
					
					AlertDialog.Builder dataDialog = new AlertDialog.Builder(ROMUpdater.this);
		    		dataDialog.setMessage(getString(R.string.wipe_data_message))
		    			.setTitle(getString(R.string.wipe_data))
		    			.setCancelable(true)
		    			.setPositiveButton(getString(R.string.wipe), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								sdext.create().show();
							}
						})
						.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
		    		
		    		AlertDialog dataAlert = dataDialog.create();
		    		dataAlert.show();
					break;
				case 4:
					AlertDialog.Builder backupDialog = new AlertDialog.Builder(ROMUpdater.this);
		    		backupDialog.setMessage(getString(R.string.backup_rom_message))
		    			.setTitle(getString(R.string.backup_rom))
		    			.setCancelable(true)
		    			.setPositiveButton(getString(R.string.backup), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								sdata.setRecoveryOperations(2);
								RecoveryManager.setupExtendedCommand();
								RecoveryManager.doBackup(ROMUpdater.this);
								
								RecoveryManager.rebootRecovery();
							}
						})
						.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
		    		
		    		AlertDialog backupAlert = backupDialog.create();
		    		backupAlert.show();
					break;
				case 5:
					AlertDialog.Builder recoveryDialog = new AlertDialog.Builder(ROMUpdater.this);
		    		recoveryDialog.setMessage(getString(R.string.recovery_message))
		    			.setTitle(getString(R.string.recovery))
		    			.setCancelable(true)
		    			.setPositiveButton(getString(R.string.recovery_ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								RecoveryManager.rebootRecovery();
							}
						})
						.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
		    		
		    		AlertDialog recoveryAlert = recoveryDialog.create();
		    		recoveryAlert.show();
					break;
				}
			}
        });
    }
    
    private void fillActionsList() {
    	Vector<String>versionsList = new Vector<String>();
    	versionsList.add(getString(R.string.check_for_updates));
    	versionsList.add(getString(R.string.settings));
    	versionsList.add(getString(R.string.wipe_cache));
    	versionsList.add(getString(R.string.wipe_data));
    	versionsList.add(getString(R.string.backup_rom));
    	versionsList.add(getString(R.string.recovery));
    	
    	ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, versionsList);
    	actions.setAdapter(adapter);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(ROMUpdater.this);
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