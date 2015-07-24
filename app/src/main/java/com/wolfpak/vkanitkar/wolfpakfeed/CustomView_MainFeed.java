package com.wolfpak.vkanitkar.wolfpakfeed;

import android.graphics.Point;
import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

/**
 * Created by Vishaal on 7/20/15.
 */
public class CustomView_MainFeed{
    private RelativeLayout myLayout;
    private Networking_MainFeed network;
    private MainFeed mainFeed;

    private MediaView[] views;
    public int num;


    public CustomView_MainFeed(MainFeed mainFeed, Networking_MainFeed network){
        this.mainFeed = mainFeed;
        this.network = network;

        views = new MediaView[5];
        num = 0;
    }

    //PreLoad Views
    public void loadViews(String string, String handle, String url){
        myLayout = (RelativeLayout) mainFeed.findViewById(R.id.frame);
        addMediaView(url, handle, string);
    }

    //Loads MediaView
    public void addMediaView(String url,String handle, String isImage) {
        MediaView mediaView = new MediaView(mainFeed);
        Uri uri = Uri.parse(url);
        mediaView.setMediaView(uri, handle, isImage);

        mediaView.setOnTouchListener(new ImageOnTouchListener());
        myLayout.addView(mediaView);

        Log.d("debug1test", String.valueOf(mainFeed.number));

        views[num] = mediaView;
        num++;

        if(network.HowlsIsImage[0]=="false"){
            views[0].mediaVideoView.start();
        }

//        mediaView.setId(mainFeed.number);

        mainFeed.share.bringToFront();
        mainFeed.report.bringToFront();
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
                    MediaView mediaView = (MediaView) v;

                    Display display = mainFeed.getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    double maxY = size.y;
                    double green = maxY * 0.35;
                    double red = maxY * 0.65;

                    if(event.getRawY()<green){
                        mediaView.setLikeStatus(MediaView.LikeStatus.Like);
                    }
                    else if(event.getRawY()>red){
                        mediaView.setLikeStatus(MediaView.LikeStatus.Dislike);
                    }
                    else{
                        mediaView.setLikeStatus(MediaView.LikeStatus.Neutral);
                    }
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
                    //final float totaldy = initialTouchY - lastTouchY;
                    MediaView mediaView = (MediaView) v;
                    Display display = mainFeed.getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    double maxY = size.y;
                    double green = maxY * 0.35;
                    double red = maxY * 0.65;

                    if(event.getRawY()<green){ //(totaldy>50)
                        network.incrHowls(1);
                        mainFeed.number++;
                        mediaView.setLikeStatus(MediaView.LikeStatus.Like);
                        SlideToAbove(v);
                        if(network.HowlsIsImage[mainFeed.number]=="false"){
//                            Log.d("debug1test", String.valueOf(mainFeed.number));
//                            Log.d("debug2test", views[mainFeed.number] == null ? "NULL" : "NOT NULL");
//                            Log.d("debug3test", views[mainFeed.number].mediaVideoView == null ? "NULL" : "NOT NULL");
//                            if(views[mainFeed.number].mediaVideoView==null){
//                                Log.d("customview", "aadeshisgay");
//                            }
                            views[mainFeed.number].mediaVideoView.start();
                        }
                    }
                    else if(event.getRawY()>red){
                        network.incrHowls(-1);
                        mainFeed.number++;
                        mediaView.setLikeStatus(MediaView.LikeStatus.Dislike);
                        SlideToDown(v);
                        if(network.HowlsIsImage[mainFeed.number]=="false"){
                            views[mainFeed.number].mediaVideoView.start();
                        }
                    } else {
                        v.setX(0);
                        v.setY(0);
                        mediaView.setLikeStatus(MediaView.LikeStatus.Neutral);
                    }

                    activePointerId = MotionEvent.INVALID_POINTER_ID;
                    break;
                }
            }

            return true;
        }
    }
}
