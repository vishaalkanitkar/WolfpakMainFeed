package com.wolfpak.vkanitkar.wolfpakfeed;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.net.URI;
import java.net.URL;

/**
 * Created by aadeshpatel on 7/21/15.
 */
public class MediaView extends RelativeLayout {
    public enum LikeStatus {
        Like,
        Neutral,
        Dislike
    };

    // Private variables
    private ImageView mediaImageView;
    private VideoView mediaVideoView;

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
                this.likeStatusOverlayView.setBackgroundColor(Color.GREEN);
                break;

            case Dislike:
                this.likeStatusOverlayView.setBackgroundColor(Color.RED);
                break;

            default:
                this.likeStatusOverlayView.setBackgroundColor(Color.TRANSPARENT);
                break;
        }
    }

    /**
     * Initialize media view based on url and media type
     *
     * @param mediaUrl AWS S3 URL for the media
     * @param isImage boolean of whether or not the mediaUrl is an image
     */
    public void setMediaView(Uri mediaUrl, boolean isImage) {
        if (isImage) {
            this.mediaImageView.setVisibility(View.VISIBLE);
            Picasso.with(this.mediaImageView.getContext()).load(mediaUrl).into(this.mediaImageView);
        } else {
            this.mediaImageView.setVisibility(View.VISIBLE);
            this.mediaVideoView.setVideoURI(mediaUrl);
        }
    }

    /**
     * Base Initialization for this class
     */
    private void baseInit() {
        LayoutInflater.from(this.getContext()).inflate(R.layout.media_view, this);

        this.mediaImageView = (ImageView)findViewById(R.id.mediaImageView);
        this.mediaVideoView = (VideoView)findViewById(R.id.mediaVideoView);

        this.likeStatusOverlayView = (View)findViewById(R.id.likeStatusOverlayView);
    }
}
