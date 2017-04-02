package me.shrimadhavuk.fortilogin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    EditText userTxt;
    EditText userPass;
    String username;
    String password;

    public static final String tag = "me.shrimadhavuk";
    public static final String MyPREFERENCES = "me.shrimadhavuk.forti_login" ;
    SharedPreferences sharedpreferences;
    Timer timer;
    String keepalivestr = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //disableSSLCertificateChecking();

        userTxt = (EditText) findViewById(R.id.editText1);
        userPass = (EditText) findViewById(R.id.editText2);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        username = sharedpreferences.getString("username",null);
        password = sharedpreferences.getString("password", null);
        if(username != null) {
            userTxt.setText(username);
        }
        if(password != null) {
            userPass.setText(password);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO do your thing
                if(keepalivestr != null && !keepalivestr.isEmpty()) {
                    String h = openHttpConnection(keepalivestr);
                }
            }
        }, 0, 2400000);

    }

    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private String openHttpConnection(String urlStr) {

        String hostname = null;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }
            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(false);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            hostname = httpConn.getHeaderField("Location");

        }

        catch (MalformedURLException e) {
           // e.printStackTrace();
        }

        catch (IOException e) {
            //e.printStackTrace();
        }
        return hostname + "";
    }

    public void showLoginNotification(){
        int mId = 1;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("@rpn")
                        .setContentText("firewall going to timeout! login again");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

    }

    public void loginBtnClk(View v){

        username = userTxt.getText().toString();
        password = userPass.getText().toString();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                FortinetActions();
                return null;
            }
        }.execute();

    }

    public void FortinetActions() {
        String realurl = "http://shrimadhavuk.me";
        String str = openHttpConnection(realurl);
        Log.i(tag, str);
        keepalivestr = str.replaceAll("fgtauth", "keepalive");
        Log.i(tag, keepalivestr);
        String[] a = str.split("\\?", 2);
        String[] b = str.split("fgtauth", 2);
        String realstr = b[0];
        Log.i(tag, realstr);
        String magic = a[1];
        Log.i(tag, magic);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        username = sharedpreferences.getString("username", null);
        password = sharedpreferences.getString("password", null);


        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("4Tredir","shrimadhavuk.me");
        hm.put("magic",magic);
        hm.put("username",username);
        hm.put("password",password);
        String r = performPostCall(realstr,hm);
        Log.i(tag, "tst"+r);

//        String qryLst = null;
//        try {
//            qryLst = URLEncoder.encode("4Tredir","UTF-8") + "=" +
//                    "" + URLEncoder.encode("shrimadhavuk.me","UTF-8") + "&" + URLEncoder.encode("magic","UTF-8") + "=" +
//                    "" + URLEncoder.encode(magic,"UTF-8") + "&" + URLEncoder.encode("username","UTF-8") + "=" +
//                    "" + URLEncoder.encode(username,"UTF-8") + "&" + URLEncoder.encode("password","UTF-8") + "=" +
//                    "" + URLEncoder.encode(password,"UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        Log.i(tag, qryLst);

    }

    /**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     */
  /*  private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }*/


    public String  performPostCall(String requestURL,
                                   HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";
                Log.i(tag, "r");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }


    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}

