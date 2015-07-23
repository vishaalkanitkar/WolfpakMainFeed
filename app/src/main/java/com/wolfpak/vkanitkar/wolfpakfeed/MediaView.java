package com.wolfpak.vkanitkar.wolfpakfeed;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MediaView extends RelativeLayout {
    public enum LikeStatus {
        Like,
        Neutral,
        Dislike
    };

    // Private variables
    private ImageView mediaImageView;
    private VideoView mediaVideoView;
//    private TextView mediaTextView;

    private View likeStatusOverlayView;

    // Constructors
    public MediaView(Context context) {
        super(context);
        baseInit();
    }

    public MediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        baseInit();
    }

    public MediaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        baseInit();
    }

    /**
     * Handles Changing Opaque View on Top of ImageView/VideoView Based on LikeStatus
     * - Like: Green
     * - Dislike: Red
     * - Neutral: Transparent
     *
     * @param likeStatus Status of the view
     */
    public void setLikeStatus(LikeStatus likeStatus) {
        switch (likeStatus) {
            case Like:
//                this.likeStatusOverlayView.setBackgroundResource(R.drawable.paw);
                this.likeStatusOverlayView.setBackgroundColor(Color.argb(100, 0, 255, 0));
                break;

            case Dislike:
//                this.likeStatusOverlayView.setBackgroundResource(R.drawable.paw);
                this.likeStatusOverlayView.setBackgroundColor(Color.argb(100, 255, 0, 0));
                break;

            case Neutral:
                this.likeStatusOverlayView.setBackgroundColor(Color.TRANSPARENT);
                break;
//            default:
//                this.likeStatusOverlayView.setBackgroundColor(Color.TRANSPARENT);
//                break;
        }
    }

    /**
     * Initialize media view based on url and media type
     *
     * @param mediaUrl AWS S3 URL for the media
     * @param isImage boolean of whether or not the mediaUrl is an image
     */
    public void setMediaView(Uri mediaUrl, String handle, String isImage) {
//        mediaTextView.setText(handle.toCharArray(), 0, handle.length());

        if (Objects.equals(isImage, "true")) {
            this.mediaImageView.setVisibility(View.VISIBLE);
            Picasso.with(this.mediaImageView.getContext()).load(mediaUrl).into(this.mediaImageView);
//            mediaImageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                   if(mediaTextView.getVisibility() == View.INVISIBLE)
//                       mediaTextView.setVisibility(View.VISIBLE);
//                    else
//                       mediaTextView.setVisibility(View.INVISIBLE);
//                }
//            });

        } else {
            this.mediaVideoView.setVisibility(View.VISIBLE);
            this.mediaVideoView.setVideoURI(mediaUrl);
            this.mediaVideoView.requestFocus();
            this.mediaVideoView.start();
//            mediaVideoView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                    if (mediaTextView.getVisibility() == View.INVISIBLE)
//                        mediaTextView.setVisibility(View.VISIBLE);
//                    else
//                        mediaTextView.setVisibility(View.INVISIBLE);
//                }
//            });
        }
    }

    /**
     * Base Initialization for this class
     */
    private void baseInit() {
        LayoutInflater.from(this.getContext()).inflate(R.layout.media_view, this);

        this.mediaImageView = (ImageView)findViewById(R.id.mediaImageView);
        this.mediaVideoView = (VideoView)findViewById(R.id.mediaVideoView);
//        this.mediaTextView = (TextView) this.findViewById(R.id.handle);

        this.likeStatusOverlayView = (View)findViewById(R.id.likeStatusOverlayView);
    }
}
