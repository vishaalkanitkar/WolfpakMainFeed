package com.wolfpak.vkanitkar.wolfpakfeed;

import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

/**
 * Created by Vishaal on 7/20/15.
 */
public class CustomView_MainFeed{
    private FrameLayout myLayout;
    private Networking_MainFeed network;
    private MainFeed mainFeed;

    public CustomView_MainFeed(MainFeed mainFeed, Networking_MainFeed network){
        this.mainFeed = mainFeed;
        this.network = network;
    }

    //PreLoad Views
    public void loadViews(String string, String url){
        myLayout = (FrameLayout) mainFeed.findViewById(R.id.frame);
        if(Objects.equals(string, "true"))
            addImageView(url);
        else
            addVideoView(url);
    }

    //Loads ImageView
    public void addImageView(String url) {
        ImageView imageView = new ImageView(mainFeed);

        imageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        //imageView.setId(R.id.classic + number);

        Picasso.with(imageView.getContext()).load(url).into(imageView);
        imageView.setOnTouchListener(new ImageOnTouchListener());
        myLayout.addView(imageView);
        mainFeed.share.bringToFront();
        mainFeed.report.bringToFront();
    }

    //Loads VideoView
    public void addVideoView(String url){
        VideoView videoView = new VideoView(mainFeed);

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

//                    if(dy>50){
//                    }
//                    else if(dy<-50){
//                    }
//                    else{
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
                        network.incrHowls(1);
                        mainFeed.number++;
                        SlideToAbove(v);
                    }
                    else if(totaldy<-50){
                        network.incrHowls(-1);
                        mainFeed.number++;
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
