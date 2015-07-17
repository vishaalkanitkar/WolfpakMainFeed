package com.wolfpak.vkanitkar.wolfpakfeed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;


public class MainFeed extends Activity implements SimpleGestureFilter.SimpleGestureListener {

    private SimpleGestureFilter detector;

    ImageView howls;
    ImageView switch_howls;

    VideoView howls1;
    VideoView switch_howls1;

    ImageView refresh_howl;

    int length = 6;
    int number = 0;
    int oldhowl;
    int swipedirection = 0;
    int animation_view = 0;

    String[] HowlsURL = new String[length];
    String[] HowlsIsImage = new String[length];
    String[] HowlsUserID = new String[length];
    String[] HowlsPostID = new String[length];

    final Context context = this;
    private String random_string;
    private String random_input = "";
    private ImageButton share;
    private ImageButton report;


    LocationManager lm;
    Location location;
    double longitude;
    double latitude;
    String deviceId;

    ShareDialog shareDialog;
    private CallbackManager callbackManager;
    private LoginManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get rid of status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_feed);

        //Initialize SDK & Check Security Key Hash
        FacebookSdk.sdkInitialize(getApplicationContext());
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.wolfpak.vkanitkar.wolfpakfeed",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }

        // get reference to the views
        howls = (ImageView) findViewById(R.id.imageView);
        howls1 = (VideoView) findViewById(R.id.videoView);
        switch_howls = (ImageView) findViewById(R.id.imageView1);
        switch_howls1 = (VideoView) findViewById(R.id.videoView1);
        refresh_howl = (ImageView) findViewById(R.id.imageView2);

        refresh_howl.setVisibility(View.INVISIBLE);
        switch_howls.setVisibility(View.INVISIBLE);
        howls.setVisibility(View.INVISIBLE);
        howls1.setVisibility(View.INVISIBLE); //removes initial empty black VideoView
        switch_howls1.setVisibility(View.INVISIBLE);

        //Swipe Detector
        detector = new SimpleGestureFilter(this,this);

        //Dialogs
        report = (ImageButton) findViewById(R.id.imageButton);
        share = (ImageButton) findViewById(R.id.imageButton1);

        //setting location for query string
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
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
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        deviceId = deviceUuid.toString();

        //Pull Howls from server
        getHowls();

        //Facebook Share Feature
        callbackManager = CallbackManager.Factory.create();
        List<String> permissionNeeds = Arrays.asList("publish_actions");
        manager = LoginManager.getInstance();
        manager.logInWithPublishPermissions(this, permissionNeeds);
        shareDialog = new ShareDialog(this);
        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (howls.getVisibility() == View.VISIBLE || switch_howls.getVisibility() == View.VISIBLE){
                            sharePicFB();
                        } else {
                            shareVideoFB();
                        }
                    }
                });
            }
            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }
            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
            }
        });

        //buttonReport listener
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                reportHowl();
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {
       // SlideToDown();
        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT :
                break;
            case SimpleGestureFilter.SWIPE_LEFT :
                break;
            case SimpleGestureFilter.SWIPE_DOWN :
                int a =-1;
                incrHowls(a);
                swipedirection=1;
                ++number;
                oldhowl = number - 1;
                //moveAnimation();
                // Calls No Howls Page if array is empty
//                MainFeed.this.runOnUiThreadMainFeed.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        SlideToDown();
//                    }
//                });(new Runnable() {
//                    @Override
//                    public void run() {
//                        SlideToDown();
//                    }
//                });
                if(HowlsURL[number]== null){
                    refresh_howl.setVisibility(View.VISIBLE);
                    howls.setVisibility(View.INVISIBLE);
                    howls1.setVisibility(View.INVISIBLE);
                    switch_howls.setVisibility(View.INVISIBLE);
                    switch_howls1.setVisibility(View.INVISIBLE);
                    //Turns swipe detector off (stops array out of bounds error)
                    detector.setEnabled(false);
                    Picasso.with(refresh_howl.getContext()).load(R.drawable.wolfpaktest).into(refresh_howl);
                    //Turns swipe detector back on
                    refresh_howl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            number = 0;
                            getHowls();
                            detector.setEnabled(true);
                        }
                    });
                }
                else {
                    chooseView(HowlsIsImage[number], HowlsIsImage[oldhowl], HowlsURL[number]);
                    Toast.makeText(getApplicationContext(), "DOWN", Toast.LENGTH_SHORT).show();

                }
                break;
            case SimpleGestureFilter.SWIPE_UP :
                int b =1;
                incrHowls(b);
                swipedirection=2;
                ++number;
                oldhowl = number - 1;
//                MainFeed.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        SlideToAbove();
//                    }
//                });
                // moveAnimation();
                if(HowlsURL[number]== null){
                    refresh_howl.setVisibility(View.VISIBLE);
                    howls.setVisibility(View.INVISIBLE);
                    howls1.setVisibility(View.INVISIBLE);
                    switch_howls.setVisibility(View.INVISIBLE);
                    switch_howls1.setVisibility(View.INVISIBLE);
                    detector.setEnabled(false);
                    Picasso.with(refresh_howl.getContext()).load(R.drawable.wolfpaktest).into(refresh_howl);

                    refresh_howl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            number = 0;
                            getHowls();
                            detector.setEnabled(true);
                        }
                    });
                }
                else {
                    chooseView(HowlsIsImage[number], HowlsIsImage[oldhowl], HowlsURL[number]);
                    Toast.makeText(getApplicationContext(), "UP", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public void onDoubleTap() {
    }

    public void chooseView(String bool, String oldbool, String url){
        switch (bool){
            case "true":
                if(oldbool=="true" && howls.getVisibility() == View.VISIBLE ){
                    Picasso.with(switch_howls.getContext()).load(url).into(switch_howls);

                    howls1.setVisibility(View.INVISIBLE);
                    switch_howls1.setVisibility(View.INVISIBLE);
                    refresh_howl.setVisibility(View.INVISIBLE);

                    switch_howls.setVisibility(View.VISIBLE);
                    Log.v("Debug", "Visiblity");

//                    MainFeed.this.runOnUiThread(load);
//                    load=new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Picasso.with(switch_howls.getContext()).load(url).into(switch_howls);
//                        }
//                    });
//                    load.start();

                    if(swipedirection==1){
                        SlideToDown();Log.v("Debug","Swipe Down");}
                    else{
                        SlideToAbove();Log.v("Debug", "Swipe Above");}


                    howls.setVisibility(View.INVISIBLE);


                    animation_view=1;

                    if(number!=0) {
                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                switch_howls.buildDrawingCache();
                                Bitmap image = switch_howls.getDrawingCache();
                                SharePhoto photo = new SharePhoto.Builder()
                                        .setUserGenerated(true)
                                        .setBitmap(image)
                                        .setCaption("#WOLFPAK2015")
                                        .build();
                                SharePhotoContent content = new SharePhotoContent.Builder()
                                        .addPhoto(photo)
                                        .build();
                                shareDialog.show(content);
                            }
                        });
                    }

                }
                else if(oldbool=="true" && switch_howls.getVisibility() == View.VISIBLE ){
                    Picasso.with(howls.getContext()).load(url).into(howls);

                    howls1.setVisibility(View.INVISIBLE);
                    switch_howls1.setVisibility(View.INVISIBLE);
                    refresh_howl.setVisibility(View.INVISIBLE);

                    howls.setVisibility(View.VISIBLE);

                    if(swipedirection==1)
                        SlideToDown();
                    else
                        SlideToAbove();

                    switch_howls.setVisibility(View.INVISIBLE);

                    animation_view=0;

                    if(number!=0) {
                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                howls.buildDrawingCache();
                                Bitmap image = howls.getDrawingCache();
                                SharePhoto photo = new SharePhoto.Builder()
                                        .setUserGenerated(true)
                                        .setBitmap(image)
                                        .setCaption("#WOLFPAK2015")
                                        .build();
                                SharePhotoContent content = new SharePhotoContent.Builder()
                                        .addPhoto(photo)
                                        .build();
                                shareDialog.show(content);
                            }
                        });
                    }
                }
                else if(oldbool=="false"){
                    Picasso.with(howls.getContext()).load(url).into(howls);

                    switch_howls.setVisibility(View.INVISIBLE);
                    refresh_howl.setVisibility(View.INVISIBLE);

                    howls.setVisibility(View.VISIBLE);

                    if(swipedirection==1)
                        SlideToDown();
                    else
                        SlideToAbove();

                    switch_howls1.setVisibility(View.INVISIBLE);
                    howls1.setVisibility(View.INVISIBLE);


                    animation_view=0;

                    if(number!=0) {
                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                howls.buildDrawingCache();
                                Bitmap image = howls.getDrawingCache();
                                SharePhoto photo = new SharePhoto.Builder()
                                        .setUserGenerated(true)
                                        .setBitmap(image)
                                        .setCaption("#WOLFPAK2014")
                                        .build();
                                SharePhotoContent content = new SharePhotoContent.Builder()
                                        .addPhoto(photo)
                                        .build();
                                shareDialog.show(content);
                            }
                        });
                    }
                }
                else if(oldbool=="start"){
                    Picasso.with(howls.getContext()).load(url).into(howls);
                    howls.setVisibility(View.VISIBLE);
                    switch_howls.setVisibility(View.INVISIBLE);
                    howls1.setVisibility(View.INVISIBLE);
                    switch_howls1.setVisibility(View.INVISIBLE);
                    refresh_howl.setVisibility(View.INVISIBLE);

                    animation_view=0;

                        if(number!=0) {
                            share.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    sharePicFB();
                                }
                            });
                        }

                }
                break;
            case "false":
                if(oldbool=="false" && howls1.getVisibility() == View.VISIBLE ){
                    Uri uri = Uri.parse(url);
                    switch_howls1.setVideoURI(uri);
                    switch_howls1.requestFocus();

                    howls.setVisibility(View.INVISIBLE);
                    switch_howls.setVisibility(View.INVISIBLE);
                    switch_howls1.setVisibility(View.VISIBLE);
                    refresh_howl.setVisibility(View.INVISIBLE);

                    if(swipedirection==1)
                        SlideToDown();
                    else
                        SlideToAbove();

                    switch_howls1.start();
                    refresh_howl.setVisibility(View.INVISIBLE);
                    howls1.setVisibility(View.INVISIBLE);

                    animation_view=3;



                    switch_howls1.start();
                    if(number!=0) {
                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {

                            }
                        });
                    }

                }
                else if(oldbool=="false" && switch_howls1.getVisibility() == View.VISIBLE ){
                    Uri uri = Uri.parse(url);
                    howls1.setVideoURI(uri);
                    howls1.requestFocus();

                    howls.setVisibility(View.INVISIBLE);
                    switch_howls.setVisibility(View.INVISIBLE);
                    refresh_howl.setVisibility(View.INVISIBLE);

                    howls1.setVisibility(View.VISIBLE);

                    if(swipedirection==1)
                        SlideToDown();
                    else
                        SlideToAbove();

                    howls1.start();
                    switch_howls1.setVisibility(View.INVISIBLE);


                    animation_view = 2;


                    if(number!=0) {
                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {

                            }
                        });
                    }
                }
                else if(oldbool=="true"){
                    Uri uri = Uri.parse(url);
                    howls1.setVideoURI(uri);
                    howls1.requestFocus();

                    switch_howls1.setVisibility(View.INVISIBLE);
                    refresh_howl.setVisibility(View.INVISIBLE);

                    howls1.setVisibility(View.VISIBLE);

                    if(swipedirection==1)
                        SlideToDown();
                    else
                        SlideToAbove();

                    howls1.start();
                    howls.setVisibility(View.INVISIBLE);
                    switch_howls.setVisibility(View.INVISIBLE);
                    animation_view=2;

                    if(number!=0) {
                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {

                            }
                        });
                    }
                }
                else if(oldbool=="start"){
                    Uri uri = Uri.parse(url);
                    howls1.setVideoURI(uri);
                    howls1.requestFocus();
                    howls1.start();
                    howls1.setVisibility(View.VISIBLE);
                    switch_howls.setVisibility(View.INVISIBLE);
                    howls.setVisibility(View.INVISIBLE);
                    switch_howls1.setVisibility(View.INVISIBLE);
                    refresh_howl.setVisibility(View.INVISIBLE);
                    animation_view=2;

                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                if (number != 0) {
                                    share.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            shareVideoFB();
                                        }
                                    });
                                }
                            }
                        });
                    }
                break;
                }

        }
//
//            if (Objects.equals(bool, "true")) {
//                howls.setVisibility(View.VISIBLE);
//                howls1.setVisibility(View.INVISIBLE);
//
//                Picasso.with(howls.getContext()).load(url).into(howls);
//                if(number!=0) {
//                    share.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View arg0) {
//                            sharePicFB();
//                        }
//                    });
//                }
//            } else {
//                howls.setVisibility(View.INVISIBLE);
//                howls1.setVisibility(View.VISIBLE);
//                Uri uri = Uri.parse(url);
//
//                howls1.setVideoURI(uri);
//                howls1.requestFocus();
//                howls1.start();
//                share.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View arg0) {
//                        if (number != 0) {
//                            share.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View arg0) {
//                                    shareVideoFB();
//                                }
//                            });
//                        }
//                    }
//                });
//            }

    public void chooseView(){
            howls.setVisibility(View.VISIBLE);
            howls1.setVisibility(View.INVISIBLE);
            Picasso.with(howls.getContext()).load(R.drawable.wolfpaktest).into(howls);
            howls.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    number = 0;
                    getHowls();
                }
            });
    }

    public void getHowls(){
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.get("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/?user_id="+"temp_test_id"+"&latitude=" + latitude + "&longitude=" + longitude + "&isNSFW=true&limit=5/", new AsyncHttpResponseHandler() {
            // "?latitude=" + latitude + "&longitude=" + longitude + "&isNSFW=true&user_id=" + deviceId + "&limit=5", new AsyncHttpResponseHandler() {
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
                            }
                            // public void
//                            if (HowlsURL[number] == null) {
//                                chooseView();
//                            } else {
                                chooseView(HowlsIsImage[0], "start" , HowlsURL[0]);

//                            }
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
//        AsyncHttpClient client2 = new AsyncHttpClient(true, 80, 443);
        RequestParams params = new RequestParams();

//        params.put("post",HowlsPostID[number]);
//        params.put("user_liked=temp_test_id");
//        params.put("status",status);
            client1.post("https://ec2-52-4-176-1.compute-1.amazonaws.com/like_status/?posts="+HowlsPostID[number]+ "&user_liked=temp_test_id&status="+status+"/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
    }
//        else {
//            client2.put("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/dec_likes/"+HowlsPostID[number]+"/", params, new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//
//                }
//            });
//        }

//    public void sharedialog(){
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Share!");
//        builder.setItems(new CharSequence[]
//                        {"Facebook", "Twitter", "Instagram"},
//                new DialogInterface.OnClickListener() {
//                    //@SuppressLint("ShowToast")
//                    public void onClick(DialogInterface dialog, int which) {
//                        // The 'which' argument contains the index position
//                        // of the selected item
//                        switch (which) {
//                            case 0:
//                                manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//                                    @Override
//                                    public void onSuccess(LoginResult loginResult) {
//                                        //share.setOnClickListener(new View.OnClickListener() {
//                                        //  @Override
//                                        //public void onClick(View arg0) {
//                                        if (howls.getVisibility() == View.VISIBLE) {
//                                            sharePicFB();
//                                        } else {
//                                            shareVideoFB();
//                                        }
//                                    }
//
//                                    // });
//                                    //}
//
//                                    @Override
//                                    public void onCancel() {
//                                        System.out.println("onCancel");
//                                    }
//
//                                    @Override
//                                    public void onError(FacebookException exception) {
//                                        System.out.println("onError");
//                                    }
//                                });
//                                break;
//                            case 1:
//                                break;
//                            case 2:
//                                break;
//                        }
//                    }
//                });
//        builder.create().show();
//
//    }

    public void sharePicFB() {
        howls.buildDrawingCache();
                Bitmap image = howls.getDrawingCache();
        SharePhoto photo = new SharePhoto.Builder()
                        .setUserGenerated(true)
                        .setBitmap(image)
                .setCaption("#WOLFPAK2015")
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);

    }

    public void shareVideoFB(){
                    Uri mUri = null;
                    try {
                        Field mUriField = VideoView.class.getDeclaredField("mUri");
                        mUriField.setAccessible(true);
                        mUri = (Uri)mUriField.get(howls1);
                    } catch(Exception e) {
                    }

                    ShareVideo video= new ShareVideo.Builder()
                            .setLocalUrl(mUri)
                            .build();
                    ShareVideoContent content = new ShareVideoContent.Builder()
                            .setVideo(video)
                            .build();

                    shareDialog.show(content);
    }

    public void reportHowl(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        final EditText random = new EditText(this);
        random.setInputType(InputType.TYPE_CLASS_TEXT);

        // set title
        alertDialogBuilder.setTitle("FLAG!!");
        final EditText input = new EditText(this);

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you really want to report this howl?")
                .setCancelable(false)
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        randomstring();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
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
                                            reportput.put("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/flag/" + HowlsPostID[number] + "/", new AsyncHttpResponseHandler() {
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

    public void SlideToAbove() {
        Animation slide;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, -5.0f);

        slide.setDuration(5000);

//        RotateAnimation rotate = new RotateAnimation(180, 250, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
//        rotate.setDuration(5000);

        switch(animation_view){
            case 0:
                howls.startAnimation(slide);
                break;
//                howls.startAnimation(rotate);
            case 1:
                switch_howls.startAnimation(slide);
                break;
//                switch_howls.startAnimation(rotate);
            case 2:
                howls1.startAnimation(slide);
                break;
//                howls1.startAnimation(rotate);
            case 3:
                switch_howls1.startAnimation(slide);
                break;
//                switch_howls1.startAnimation(rotate);

        }

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

        });

    }

    public void SlideToDown() {
        Animation slide;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, 5.2f);
        // RotateAnimation rotate = new RotateAnimation(180, 250, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
        slide.setDuration(5000);


        switch(animation_view){
            case 0:
                howls.startAnimation(slide);
//                howls.startAnimation(rotate);
                break;
            case 1:
                switch_howls.startAnimation(slide);
//                switch_howls.startAnimation(rotate);
                break;
            case 2:
                howls1.startAnimation(slide);
//                howls1.startAnimation(rotate);
                break;
            case 3:
                switch_howls1.startAnimation(slide);
//                switch_howls1.startAnimation(rotate);
                break;

        }

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

        });

    }

    public void moveAnimation(View v){
        //Rotate ImageView
//        RotateAnimation rotate = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
//        rotate.setDuration(500);
//        howls.startAnimation(rotate);

//        howls.setAlpha(0.0f);
//
//        howls.animate()
//                .translationY(howls.getHeight())
//                .alpha(1.0f);
       // howls.setVisibility(View.INVISIBLE);


    }

    public void shiftAnimation(){
        howls.animate()
                .translationY(0)
                .alpha(0.0f);
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                    }
//                });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}