package com.wolfpak.vkanitkar.wolfpakfeed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Vishaal on 7/20/15.
 */
public class Networking_MainFeed{

    public int length;
    private MainFeed mainFeed;
    private CustomView_MainFeed customView;

    //Arrays for JSON Object String
    private String[] HowlsURL;
    private String[] HowlsIsImage;
    private String[] HowlsUserID;
    private String[] HowlsPostID;
    private String[] HowlsHandle;

    //Location
    public LocationManager lm;
    public Location location;
    private double longitude;
    private double latitude;
    public String deviceId;

    //Random Number
    private String random_string;
    private String random_input;

    public Networking_MainFeed(MainFeed mainFeed){
        this.mainFeed = mainFeed;

        customView = new CustomView_MainFeed(mainFeed, this);
        length = 10;
        HowlsURL = new String[length];
        HowlsIsImage = new String[length];
        HowlsUserID = new String[length];
        HowlsPostID = new String[length];
        HowlsHandle = new String[length];
        random_input = "";
    }

    public void intializeQueryString() {
        //Setting Location for get() query string
        lm = (LocationManager) mainFeed.getSystemService(Context.LOCATION_SERVICE);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        //Location Update Detector
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        };
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

        //Android System Unique ID
        final TelephonyManager tm = (TelephonyManager) mainFeed.getBaseContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(
                mainFeed.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(),
                ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        deviceId = deviceUuid.toString();
    }

    //Asynchronous HTTP Client - Pull Image/Video from Server
    public void getHowls(){
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.get("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/?user_id=" + "temp_test_id" + "&latitude=" + latitude + "&longitude=" + longitude + "&isNSFW=true&limit=5/", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final JSONArray arr;
                try {
                    arr = new JSONArray(new String(response));
                    Log.v("com.wolfpakapp.httpreqs", arr.getJSONObject(0).optString("media_url"));
                    Log.v("com.wolfpakapp.httpreqs", arr.getJSONObject(0).optString("is_image"));
                    Log.v("com.wolfpakapp.httpreqs", String.valueOf(arr.length()));

                    mainFeed.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int x = 0; x < 5; x++) {

                                try {
                                    HowlsURL[x] = arr.getJSONObject(x).optString("media_url");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    HowlsIsImage[x] = arr.getJSONObject(x).optString("is_image");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    HowlsUserID[x] = arr.getJSONObject(x).optString("user_id");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    HowlsPostID[x] = arr.getJSONObject(x).optString("id");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    HowlsHandle[x] = arr.getJSONObject(x).optString("handle");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    for (int x = 4; x > -1; x--) {
                        customView.loadViews(HowlsIsImage[x], HowlsURL[x]);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
            }
        });

    }

    //Asynchronous HTTP Client - Incr/Decr Image/Video in Server
    public void incrHowls(int status) {
        AsyncHttpClient client1 = new AsyncHttpClient(true, 80, 443);
        client1.post("https://ec2-52-4-176-1.compute-1.amazonaws.com/like_status/?post=" + HowlsPostID[mainFeed.number] + "&user_liked=temp_test_id&status=" + status + "/", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    //Asynchronous HTTP Client - Reports Image/Video in Server
    public void reportHowl(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainFeed);
        final EditText random = new EditText(mainFeed);
        random.setInputType(InputType.TYPE_CLASS_TEXT);

        // set title
        alertDialogBuilder.setTitle("FLAG!!");
        final EditText input = new EditText(mainFeed);

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you really want to report this howl?")
                .setCancelable(false)
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        randomstring();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainFeed);
                        // set title
                        alertDialogBuilder.setTitle("Type Captcha in order to report!");
                        // set dialog message

                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        alertDialogBuilder.setView(input);
                        final AsyncHttpClient reportput = new AsyncHttpClient(true, 80, 443);

                        alertDialogBuilder
                                .setMessage("CAPTCHA = " + random_string)
                                .setCancelable(false)
                                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        random_input = input.getText().toString();
                                        if (Objects.equals(random_string, random_input)) {
                                            reportput.put("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/flag/" + HowlsPostID[mainFeed.number] + "/", new AsyncHttpResponseHandler() {
                                                @Override
                                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                }

                                                @Override
                                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                }
                                            });
                                        } else {
                                            dialog.cancel();
                                        }

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing
                                        dialog.cancel();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    //Random Number Generator for reportHowl()
    public void randomstring(){
        char[] chars1 = "ABCDEF012GHIJKL345MNOPQR678STUVWXYZ9".toCharArray();
        StringBuilder sb1 = new StringBuilder();
        Random random1 = new Random();
        for (int i = 0; i < 8; i++)
        {
            char c1 = chars1[random1.nextInt(chars1.length)];
            sb1.append(c1);
        }
        random_string = sb1.toString();
    }
}
