package org.elegosproject.romupdater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ROMSuperActivity extends Activity {
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
        	PackageManager pm = getPackageManager();
        	String version = "";
        	try {
        		version = pm.getPackageInfo("org.elegosproject.romupdater", 0).versionName;
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
            AlertDialog.Builder builder = new AlertDialog.Builder(ROMSuperActivity.this);
            builder.setIcon(R.drawable.ic_menu_info)
            	.setTitle("ROM Updater v."+version)
            	.setMessage(SharedData.ABOUT_LICENCE+"\n\nPlease donate via PayPal to giacomo.furlan@fastwebnet.it.\nThanks\n\nGiacomo 'elegos' Furlan")
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
