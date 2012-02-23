package org.elegosproject.romupdater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.lang.reflect.Field;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.util.Log;

public class ROMSuperActivity extends Activity {
    private final boolean DBG = false;

    private ProgressDialog progress;
    protected AlertDialog.Builder alert;
    public static String DOWNLOAD_DIRECTORY = "/sdcard/romupdater/";

    public static String PackageName;
    public static String Namespace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progress = new ProgressDialog(this);
        alert = new AlertDialog.Builder(this);
        PackageName = getPackageName();
        Namespace = "";
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
        case R.id.menu_settings:
            Intent p = new Intent(this, Preferences.class);
            startActivity(p);
            return true;
        case R.id.menu_info:
            PackageManager pm = getPackageManager();
            String version = "";
            try {
                version = pm.getPackageInfo(PackageName, 0).versionName;
            } catch (Exception e) {
                e.printStackTrace();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(ROMSuperActivity.this);
            builder.setIcon(android.R.drawable.ic_menu_help)
                .setTitle(getString(R.string.app_name) + "\nv"+version)
                .setMessage("\n" + SharedData.ABOUT_LICENCE)
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

    class DownloadFile extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            // initialize the progress dialog
            progress = new ProgressDialog(ROMSuperActivity.this);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String toDownload = params[0];
            String destination = params[1];

            // creates the base folder, if it doesn't exist
            try {
                File dir = new File(destination.substring(0,destination.lastIndexOf("/")));
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String fileName = toDownload.substring(toDownload.lastIndexOf("/")+1);

            // for dynamic json, only show filename param
            if (fileName.contains("f=")) {
                fileName = fileName.substring(fileName.lastIndexOf("f=")+2);
            }

            // initialize the progress dialog
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setMessage(getString(R.string.downloading)+":\n" + fileName);
            progress.setCancelable(false);
            progress.setMax(100);
            publishProgress(0);

            // File not reachable
            if(!DownloadManager.checkHttpFile(toDownload)) {
                alert.setMessage(getString(R.string.repository_file_not_found))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            return;
                        }
                    });
                publishProgress(-1);
                return false;
            }

            try {
                // initialize some timeouts
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters,3000);

                // create the connection
                URL url = new URL(toDownload);
                URLConnection connection = url.openConnection();
                HttpURLConnection httpConnection = (HttpURLConnection) connection;

                // connection accepted
                if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try {
                        File file = new File(destination);
                        // delete the file if exists
                        file.delete();
                    } catch (Exception e) {
                        // nothing
                    }

                    int size = connection.getContentLength();

                    int index = 0;
                    int current = 0;

                    try {
                        FileOutputStream output = new FileOutputStream(destination, false);
                        InputStream input = connection.getInputStream();
                        BufferedInputStream buffer = new BufferedInputStream(input);
                        byte[] bBuffer = new byte[10240];

                        while((current = buffer.read(bBuffer)) != -1) {
                            try {
                                output.write(bBuffer, 0, current);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            index += current;
                            publishProgress(index/(size/100));
                        }
                        output.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }

                    progress.dismiss();
                    return true;
                }

                // connection refused
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... iProgress) {
            switch(iProgress[0]) {
            // an error occurred, popup an error
            case -1:
                alert.create().show();
                progress.dismiss();
                break;
            // initialize the progress bar to 0
            case 0:
                progress.show();
                progress.setProgress(iProgress[0]);
                break;
            default:
                progress.setProgress(iProgress[0]);
            }
            super.onProgressUpdate(iProgress);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progress.dismiss();
            super.onPostExecute(result);
            onDownloadComplete(result);
        }
    }  // class DownloadFile

    class DownloadJSON extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            // initialize the progress dialog
            progress = new ProgressDialog(ROMSuperActivity.this);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String url = params[0];

            // initialize the progress dialog
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage(getString(R.string.loading));
            progress.setCancelable(false);

            // show the progress dialog
            publishProgress(0);

            // File not reachable
            if(!DownloadManager.checkHttpFile(url)) {
                alert.setMessage(getString(R.string.error_json_not_found))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            return;
                        }
                    });
                    publishProgress(-1);
                    return false;
            }

            // initialize local variables
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters,3000);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            URI uri;
            InputStream data;

            try {
                uri = new URI(url);
                HttpGet method = new HttpGet(uri);
                HttpResponse response = httpClient.execute(method);
                data = response.getEntity().getContent();

                SharedData sdata = SharedData.getInstance();
                sdata.setInputStreamData(data);

            } catch (Exception e) {
                alert.setMessage(getString(R.string.error_json_download))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                publishProgress(-1);
                e.printStackTrace();
                return false;
            }

            publishProgress(100);
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... iProgress) {
            switch(iProgress[0]) {
            // an error occurred, popup an error
            case -1:
                alert.create().show();
                progress.dismiss();
                break;
            // initialize the progress bar to 0
            case 0:
                progress.show();
                progress.setProgress(iProgress[0]);
                break;
            case 100:
                progress.dismiss();
                break;
            default:
                progress.setProgress(iProgress[0]);
            }
            super.onProgressUpdate(iProgress);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            onJSONDataDownloaded(result);
            super.onPostExecute(result);
        }

    } // class DownloadJSON

    void onJSONDataDownloaded(Boolean success) {}
    void onDownloadComplete(Boolean success) {
        if (DBG) Log.v("SuperActivity", "onDownloadComplete "+success);
    }

} // class ROMSuperActivity
