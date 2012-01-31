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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import java.security.cert.*;
import javax.net.ssl.*;

import org.apache.http.params.BasicHttpParams;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;

public class DownloadManager {
    private static final String TAG = "ROM Updater (DownloadManager)";
    public static final String download_path = "romupdater/";

    // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String referer;
    public static String cookies;

    public static boolean checkHttpFile(String theUrl) {
        try {
            Log.i(TAG, "Testing "+theUrl+"...");
            URL url = new URL(theUrl);
            HttpURLConnection conn;
            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                Log.d(TAG,"...using https protocol");
                conn = https;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(3000);
            if (!TextUtils.isEmpty(cookies)) {
                conn.addRequestProperty("Referer", referer);
                conn.addRequestProperty("Cookie", cookies);
                Log.d(TAG, "Cookie sent :" + cookies);
            }
            conn.connect();

            String setcookie="";
            switch (conn.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                Log.d(TAG, "HTTP OK");
                conn.getInputStream().close();
                conn.disconnect();
                break;
            case HttpURLConnection.HTTP_MOVED_TEMP:
            case HttpURLConnection.HTTP_MOVED_PERM:
                referer=theUrl;
                String loc = conn.getHeaderField("Location");
                conn.getInputStream().close();
                Log.d(TAG, "HTTP redirect to " + loc);
                try {
                    setcookie = conn.getHeaderField("Set-Cookie");
                } catch (Exception e) {}
                if (TextUtils.isEmpty(cookies) && !TextUtils.isEmpty(setcookie)) {
                    if (setcookie.contains("expires")) {
                        setcookie = setcookie.substring(0, setcookie.indexOf("expires"));
                        setcookie = setcookie.substring(0, setcookie.lastIndexOf(";"));
                    }
                    cookies = setcookie;
                    Log.i(TAG, "Cookies received " + cookies);
                }
                conn.getInputStream().close();
                return checkHttpFile(loc);
                //throw new RedirectRequiredException(httpConn);
            default:
                referer=theUrl;
                try {
                    setcookie = conn.getHeaderField("Set-Cookie");
                } catch (Exception e) {}
                if (TextUtils.isEmpty(cookies) && !TextUtils.isEmpty(setcookie)) {
                    if (setcookie.contains("expires")) {
                        setcookie = setcookie.substring(0, setcookie.indexOf("expires"));
                        setcookie = setcookie.substring(0, setcookie.lastIndexOf(";"));
                    }
                    cookies = setcookie;
                    Log.i(TAG, "Cookies received " + cookies);
                    conn.getInputStream().close();
                    return checkHttpFile(theUrl);
                }
                Log.w(TAG, "HTTP Response code: "+conn.getResponseCode());
                Map<String, List<String>> hf = conn.getHeaderFields();
                for (String key : hf.keySet())
                    Log.d(TAG, key + ": " + conn.getHeaderField(key));

                int size = conn.getContentLength();
                int index = 0;
                int current = 0;
                File file = new File("/sdcard/404_debug.txt");
                try {
                    FileOutputStream output = new FileOutputStream(file);
                    InputStream input = conn.getInputStream();
                    BufferedInputStream buffer = new BufferedInputStream(input);
                    byte[] bBuffer = new byte[4096];

                    while((current = buffer.read(bBuffer)) != -1) {
                        try {
                            output.write(bBuffer, 0, current);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        index += current;
                    }
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                conn.disconnect();
                Log.e(TAG, "check content of /sdcard/404_debug.txt");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG,e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
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

    public boolean sendAnonymousData(Context ctx) {

        // <string name="statserver_url">http://www.elegosproject.org/android/upload.php</string>

        String link = ctx.getResources().getString(R.string.statserver_url);
        String data;

        Log.d(TAG, "Stat report url set to " + link);

        SharedData shared = SharedData.getInstance();
        String romName = shared.getRepositoryROMName();
        String romVersion = shared.getDownloadVersion();
        String romPhone = shared.getRepositoryModel();
        String romRepository = shared.getRepositoryUrl();

        if(romName.equals("") ||
                romVersion.equals("") ||
                romPhone.equals("") ||
                romRepository.equals("")) {
            Log.e(TAG,"Internal error - missing system variables.");
            return false;
        }

        cookies = "";

        CheckHttpFile check = new CheckHttpFile();
        try {
            check.execute(link);
            check.get();
        } catch (Exception e) {
            return false;
        }

        if (!check.success) return false;
        try {
            data = URLEncoder.encode("phone", "UTF-8") + "=" + URLEncoder.encode(romPhone, "UTF-8");
            data += "&"+URLEncoder.encode("rom_name", "UTF-8") + "=" + URLEncoder.encode(romName, "UTF-8");
            data += "&"+URLEncoder.encode("rom_version", "UTF-8") + "=" + URLEncoder.encode(romVersion,"UTF-8");
            data += "&"+URLEncoder.encode("rom_repository", "UTF-8") + "=" + URLEncoder.encode(romRepository,"UTF-8");

            URL url = new URL(link);
            HttpURLConnection conn;
            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                conn = https;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setFollowRedirects(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "ROMUpdater");
            if (!TextUtils.isEmpty(cookies)) {
                conn.addRequestProperty("Cookie", cookies);
            }
            conn.setDoOutput(true);
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.println(data);
            out.close();

            int status = Integer.parseInt(conn.getHeaderField("ROMUpdater-status"));
            if(status == 1)
                return true;

            Log.e(TAG, "It was impossible to send data to the stastistics server ("+conn.getHeaderField("ROMUpdater-error")+").");
            return false;

        } catch (Exception e) {
            Log.e(TAG, "It was impossible to send data to the stastistics server.");
            Log.e(TAG, "Error: "+e.toString());
            return false;
        }
    }
}
