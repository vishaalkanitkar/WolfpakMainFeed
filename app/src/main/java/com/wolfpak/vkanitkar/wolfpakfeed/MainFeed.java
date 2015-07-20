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
import android.support.v4.view.MotionEventCompat;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class MainFeed extends Activity {
    //Layouts & Buttons
    FrameLayout myLayout;
    ImageView refresh_howl;
    private ImageButton share;
    private ImageButton report;

    int length = 10;
    int number = 0;

    //Arrays for JSON Object String
    String[] HowlsURL = new String[length];
    String[] HowlsIsImage = new String[length];
    String[] HowlsUserID = new String[length];
    String[] HowlsPostID = new String[length];
    String[] HowlsHandle = new String[length];

    //Random Number
    final Context context = this;
    private String random_string;
    private String random_input = "";

    //Location
    LocationManager lm;
    Location location;
    double longitude;
    double latitude;
    String deviceId;

    //Facebook Share Features
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

        //Reference Refresh and FrameLayout
        myLayout = (FrameLayout) findViewById(R.id.frame);
        refresh_howl = (ImageView) findViewById(R.id.imageView2);

        //Dialogs
        report = (ImageButton) findViewById(R.id.imageButton);
        share = (ImageButton) findViewById(R.id.imageButton1);

        //Setting Location for get() query string
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
//                        if (howls.getVisibility() == View.VISIBLE || switch_howls.getVisibility() == View.VISIBLE){
//                            sharePicFB();
//                        } else {
//                            shareVideoFB();
//                        }
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

        //Report_Button listener
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                reportHowl();
            }
        });

        refresh_howl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                number = 0;
                getHowls();
            }
        });
    }

    //PreLoad Views
    public void loadViews(String string, String url){
        if(Objects.equals(string, "true"))
            addImageView(url);
        else
            addVideoView(url);
    }

    //Loads ImageView
    public void addImageView(String url) {
        ImageView imageView = new ImageView(this);

        imageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        //imageView.setId(R.id.classic + number);

        Picasso.with(imageView.getContext()).load(url).into(imageView);
        imageView.setOnTouchListener(new ImageOnTouchListener());
        myLayout.addView(imageView);
        share.bringToFront();
        report.bringToFront();
    }

    //Loads VideoView
    public void addVideoView(String url){
        VideoView videoView = new VideoView(this);

        videoView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        videoView.setId(videoView.generateViewId());
     //   videoView.setId(R.id.classic1+number);

        Uri uri = Uri.parse(url);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();

        videoView.setOnTouchListener(new ImageOnTouchListener());
        myLayout.addView(videoView);
        share.bringToFront();
        report.bringToFront();
    }

    //Asynchronous HTTP Client - Pull Image/Video from Server
    public void getHowls(){
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.get("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/?user_id=" + "temp_test_id" + "&latitude=" + latitude + "&longitude=" + longitude + "&isNSFW=true&limit=5/", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // ...
                final JSONArray arr;
                try {
                    arr = new JSONArray(new String(response));
                    Log.v("com.wolfpakapp.httpreqs", arr.getJSONObject(0).optString("media_url"));
                    Log.v("com.wolfpakapp.httpreqs", arr.getJSONObject(0).optString("is_image"));
                    Log.v("com.wolfpakapp.httpreqs", String.valueOf(arr.length()));

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
                                try {
                                    HowlsHandle[x] = arr.getJSONObject(x).optString("handle");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    for (int x = 4; x > -1; x--) {
                        loadViews(HowlsIsImage[x], HowlsURL[x]);
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
            client1.post("https://ec2-52-4-176-1.compute-1.amazonaws.com/like_status/?post=" + HowlsPostID[number] + "&user_liked=temp_test_id&status=" + status + "/", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
    }

    //Share Picture to Facebook
    public void sharePicFB(ImageView imageView) {
        imageView.buildDrawingCache();
                Bitmap image = imageView.getDrawingCache();
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

    //Share Video to Facebook
    public void shareVideoFB(){
                    Uri mUri = null;
                    try {
                        Field mUriField = VideoView.class.getDeclaredField("mUri");
                        mUriField.setAccessible(true);
                       // mUri = (Uri)mUriField.get(howls1);
                    } catch(Exception e) {
                    }

                    ShareVideo video= new ShareVideo.Builder()
                            .setLocalUrl(mUri) //alwaysNull - Check later
                            .build();
                    ShareVideoContent content = new ShareVideoContent.Builder()
                            .setVideo(video)
                            .build();

                    shareDialog.show(content);
    }

    //Asynchronous HTTP Client - Reports Image/Video in Server
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

    //SlideToAbove Animation
    public void SlideToAbove(View v) {
        Animation slide;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, -5.0f);

        slide.setDuration(1500);
//        RotateAnimation rotate = new RotateAnimation(180, 250, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
//        rotate.setDuration(5000);
        v.startAnimation(slide);
        v.animate().rotation(-30).start();

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

        myLayout.removeView(v);

    }

    //SlideToDown Animation
    public void SlideToDown(View v) {
        Animation slide;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, 5.2f);
        // RotateAnimation rotate = new RotateAnimation(180, 250, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
        slide.setDuration(1500);

        v.startAnimation(slide);
        v.animate().rotation(30).start();

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

        myLayout.removeView(v);


    }

    @Override //Facebook ReInitializer
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //DragView Function
    private final class ImageOnTouchListener implements View.OnTouchListener {
        private int activePointerId = MotionEvent.INVALID_POINTER_ID;

        private float initialTouchX = 0;
        private float initialTouchY = 0;

        private float lastTouchX = 0;
        private float lastTouchY = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = MotionEventCompat.getActionMasked(event);

            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    activePointerId = MotionEventCompat.getPointerId(event, 0);

                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();

                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();

                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getRawX();
                    final float y = event.getRawY();

                    final float dx = x - lastTouchX;
                    final float dy = y - lastTouchY;

                    v.setX(v.getX() + dx);
                    v.setY(v.getY() + dy);

                    lastTouchX = x;
                    lastTouchY = y;

//                    ImageView imageView = (ImageView) v;
//
//                    if(dy>50){
//                        imageView.setColorFilter(Color.argb(75,0, 100,0)); //green
//
//                    }
//                    else if(dy<-50){
//                        imageView.setColorFilter(Color.argb(75,100,0,0));  //red
//                    }
//                    else{
//                        imageView.setColorFilter(Color.argb(0, 0, 0, 0));  //clear
//
//                    }
                    break;
                }
                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final int pointerId = MotionEventCompat.findPointerIndex(event, pointerIndex);
                    if (pointerId == activePointerId) {
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        lastTouchX = event.getRawX();
                        lastTouchY = event.getRawY();
                        activePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                    }

                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                    break;
                case MotionEvent.ACTION_UP: {
                    final float totaldy = initialTouchY - lastTouchY;

                    if(totaldy>50){
                        incrHowls(1);
                        number++;
                        SlideToAbove(v);
                    }
                    else if(totaldy<-50){
                        incrHowls(-1);
                        number++;
                        SlideToDown(v);
                    } else {
                        v.setX(initialTouchX);
                        v.setY(initialTouchY);
                    }
                    activePointerId = MotionEvent.INVALID_POINTER_ID;
                    break;
                }
            }

            return true;
        }
    }
}