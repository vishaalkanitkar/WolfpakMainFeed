package com.wolfpak.vkanitkar.wolfpakfeed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
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
    }

    // Private variables
    private ImageView mediaImageView;
    public VideoView mediaVideoView;
    public  ImageView VideoBlack;

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
                this.likeStatusOverlayView.setBackgroundColor(Color.argb(100, 0, 255, 0));
                break;

            case Dislike:
                this.likeStatusOverlayView.setBackgroundColor(Color.argb(100, 255, 0, 0));
                break;

            case Neutral:
                this.likeStatusOverlayView.setBackgroundColor(Color.argb(0,0,0,0));
                break;

            default:
                this.likeStatusOverlayView.setBackgroundColor(Color.argb(0,0,0,0));
                break;
        }
    }

    /**
     * Initialize media view based on url and media type
     *
     * @param mediaUrl AWS S3 URL for the media
     * @param isImage boolean of whether or not the mediaUrl is an image
     */
    public void setMediaView(Uri mediaUrl, String url, String handle, String isImage) {
        if (Objects.equals(isImage, "true")) {
            this.mediaImageView.setVisibility(View.VISIBLE);
            Picasso.with(this.mediaImageView.getContext()).load(mediaUrl).into(this.mediaImageView);

        } else {
            this.mediaVideoView.setVisibility(View.VISIBLE);
            this.mediaVideoView.setVideoURI(mediaUrl);
            this.mediaVideoView.requestFocus();
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(url,
                    MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            VideoBlack.setImageBitmap(thumb);
            VideoBlack.setVisibility(VISIBLE);
        }
    }

    /**
     * Base Initialization for this class
     */
    private void baseInit() {
        LayoutInflater.from(this.getContext()).inflate(R.layout.media_view, this);

        this.mediaImageView = (ImageView)findViewById(R.id.mediaImageView);
        this.mediaVideoView = (VideoView)findViewById(R.id.mediaVideoView);
        this.VideoBlack = (ImageView)findViewById(R.id.mediaVideoBlackOverlay);

        this.likeStatusOverlayView = findViewById(R.id.likeStatusOverlayView);
    }
}
