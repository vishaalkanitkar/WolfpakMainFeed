package com.wolfpak.vkanitkar.wolfpakfeed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;
import java.util.UUID;

public class MainFeed extends Activity implements SimpleGestureFilter.SimpleGestureListener {

    private SimpleGestureFilter detector;

    ImageView howls;
    VideoView howls1;
    int length = 10;
    int number = 0;

    String[] HowlsURL = new String[length];
    String[] HowlsIsImage = new String[length];
    String[] HowlsUserID = new String[length];
    String[] HowlsPostID = new String[length];

    final Context context = this;
    private ImageButton report;
    private ImageButton share;
    //ImageView refresh;

    LocationManager lm;
    Location location;
    double longitude;
    double latitude;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // get reference to the views
        howls = (ImageView) findViewById(R.id.imageView);
        howls1 = (VideoView) findViewById(R.id.videoView);
        howls1.setVisibility(View.INVISIBLE);

        detector = new SimpleGestureFilter(this, this);

        report = (ImageButton) findViewById(R.id.imageButton);
        share = (ImageButton) findViewById(R.id.imageButton1);
        //refresh = (ImageView) findViewById(R.id.imageView2);


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();

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

        //DeviceID
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        deviceId = deviceUuid.toString();

        getHowls();

        // add buttonReport listener
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set title
                alertDialogBuilder.setTitle("FLAG!");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Do you really want to report this howl?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current activity
                                MainFeed.this.finish();
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
        });

        /*refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getHowls();
            }
        });*/

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT:
                break;
            case SimpleGestureFilter.SWIPE_LEFT:
                break;
            case SimpleGestureFilter.SWIPE_DOWN:
                int x =-1;
                incrHowls(x);
                number++;
                if (HowlsURL[number] == null) {
                    howls.setVisibility(View.VISIBLE);
                    howls1.setVisibility(View.INVISIBLE);
                    Picasso.with(howls.getContext()).load(R.drawable.wolfpaktest).into(howls);
                } else {
                    chooseView(HowlsIsImage[number], HowlsURL[number]);
                    Toast.makeText(getApplicationContext(), "DOWN", Toast.LENGTH_SHORT).show();
                }
                break; //what is this doing, no difference
            case SimpleGestureFilter.SWIPE_UP:
                int z =1;
                incrHowls(z);
                number++;
                if (HowlsURL[number] == null) {
                    howls.setVisibility(View.VISIBLE);
                    howls1.setVisibility(View.INVISIBLE);
                    Picasso.with(howls.getContext()).load(R.drawable.wolfpaktest).into(howls);
                } else {
                    chooseView(HowlsIsImage[number], HowlsURL[number]);
                    Toast.makeText(getApplicationContext(), "UP", Toast.LENGTH_SHORT).show();
                }
                break; //what is this doing, no difference

        }
    }

    @Override
    public void onDoubleTap() {
    }

    public void chooseView(String bool, String url) {
        if (Objects.equals(bool, "true")) {
            howls.setVisibility(View.VISIBLE);
            howls1.setVisibility(View.INVISIBLE);
            Picasso.with(howls.getContext()).load(url).into(howls);
        } else {
            howls.setVisibility(View.INVISIBLE);
            howls1.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse(url);
            howls1.setVideoURI(uri);
            howls1.requestFocus();
            howls1.start();
        }
    }

    public void getHowls() {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.get("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/region/?latitude=" + latitude + "&longitude=" + longitude + "&isNSFW=true&user_id=" + deviceId + "&isImage=true&limit=5", new AsyncHttpResponseHandler() {
        //client.get("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // ...
                final JSONArray arr;
                try {
                    arr = new JSONArray(new String(response));
                    Log.v("com.wolfpakapp.httpreqs", arr.getJSONObject(0).optString("media_url"));
                    Log.v("com.wolfpakapp.httpreqs", arr.getJSONObject(0).optString("is_image"));
                    Log.v("com.wolfpakapp.httpreqs", String.valueOf(arr.length()));

                    // final int check = arr.length();

                    MainFeed.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int x = 0; x < 10; x++) {

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
                            }

                            chooseView(HowlsIsImage[0], HowlsURL[0]); //load 1st image before swipe
                            // Toast.makeText(getApplicationContext(), HowlsURL[0], Toast.LENGTH_LONG).show();
                            // Toast.makeText(getApplicationContext(), "BREAK", Toast.LENGTH_SHORT).show();
                            // Toast.makeText(getApplicationContext(), HowlsIsImage[0], Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
            }
        });

    }

    public void incrHowls(int status) {
        AsyncHttpClient client1 = new AsyncHttpClient(true, 80, 443);
        AsyncHttpClient client2 = new AsyncHttpClient(true, 80, 443);
        RequestParams params = new RequestParams();

        //params.put("id",HowlsPostID[number]);
        params.put("user_id","temp_test_id");
       // params.put("likes_status",status);
        if(status==1) {
            client1.put("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/inc_likes/"+HowlsPostID[number]+"/", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
        else {
            client2.put("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/dec_likes/"+HowlsPostID[number]+"/", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }

//report method

//    public void reportHowls(){
//        AsyncHttpClient client3 = new AsyncHttpClient(true, 80, 443);
//        client3.delete("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/", new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.delete("id", HowlsPostID[number]);
//                    jsonObject.delete("user_id", HowlsUserID[number]);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//            }
//        });
//
//    }

}



