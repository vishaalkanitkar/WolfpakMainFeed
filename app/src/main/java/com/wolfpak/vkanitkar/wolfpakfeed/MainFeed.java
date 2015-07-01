package com.wolfpak.vkanitkar.wolfpakfeed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;


public class MainFeed extends Activity implements SimpleGestureFilter.SimpleGestureListener {

    private SimpleGestureFilter detector;

    ImageView howls;
    VideoView howls1;
    int length = 11;
    int number = 0;

    String[] HowlsURL = new String[length];
    String[] HowlsIsImage = new String[length];
    String[] HowlsUserID = new String[length];

    final Context context = this;
    private ImageButton report;
    private ImageButton share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // get reference to the views
        howls = (ImageView) findViewById(R.id.imageView);
        howls1 = (VideoView) findViewById(R.id.videoView);
        howls1.setVisibility(View.INVISIBLE);

        detector = new SimpleGestureFilter(this,this);

        report = (ImageButton) findViewById(R.id.imageButton);
        share = (ImageButton) findViewById(R.id.imageButton1);

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
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT :
                break;
            case SimpleGestureFilter.SWIPE_LEFT :
                break;
            case SimpleGestureFilter.SWIPE_DOWN :
                number++;
                if(HowlsURL[number]== ""){
                    howls.setVisibility(View.VISIBLE);
                    howls1.setVisibility(View.INVISIBLE);
                    Picasso.with(howls.getContext()).load(R.layout.no_howls).into(howls);
                }
                else {
                    chooseView(HowlsIsImage[number], HowlsURL[number]);
                    Toast.makeText(getApplicationContext(), "DOWN", Toast.LENGTH_SHORT).show();
                }
                break;
            case SimpleGestureFilter.SWIPE_UP :
                number++;
                if(HowlsURL[number]== ""){
                    howls.setVisibility(View.VISIBLE);
                    howls1.setVisibility(View.INVISIBLE);
                    Picasso.with(howls.getContext()).load(R.layout.no_howls).into(howls);
                }
                else {
                    chooseView(HowlsIsImage[number], HowlsURL[number]);
                    Toast.makeText(getApplicationContext(), "UP", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public void onDoubleTap() {
    }

    public void chooseView(String bool, String url){
        if(Objects.equals(bool, "true")) {
            howls.setVisibility(View.VISIBLE);
            howls1.setVisibility(View.INVISIBLE);
            Picasso.with(howls.getContext()).load(url).into(howls);
        }
        else {
            howls.setVisibility(View.INVISIBLE);
            howls1.setVisibility(View.VISIBLE);
        }
    }

    public void getHowls(){
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        //client.get("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/region/?latitude="+"&longitude="+"&isNSFW=true&user_id="+"&isImage=true", new AsyncHttpResponseHandler() {
        client.get("https://ec2-52-4-176-1.compute-1.amazonaws.com/posts/", new AsyncHttpResponseHandler() {

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
                            for (int x = 0; x < 11; x++) {

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
                            }
                            // public void

                            chooseView(HowlsIsImage[0], HowlsURL[0]);
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
}




