package com.wolfpak.vkanitkar.wolfpakfeed;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainFeed extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Get Rid of Status Bar **/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_feed);

        if(null == savedInstanceState)  {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new MainFeedFragment())
                    .commit();
        }
    }

    //    /** Layouts & Buttons **/
//    public ImageView refresh_howl;
//    public ImageButton report;
//    public ImageButton share;
//
//    Networking_MainFeed network = new Networking_MainFeed(this);
//    CustomView_MainFeed customView = new CustomView_MainFeed(this,network);
//
//    public int number = 0;
//
//    /** Facebook Share Features **/
//    private ShareDialog shareDialog;
//    private CallbackManager callbackManager;
//    public LoginManager manager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        /** Get Rid of Status Bar **/
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        setContentView(R.layout.activity_feed);
//
//        /** Initialize SDK & Check Security Key Hash **/
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.wolfpak.vkanitkar.wolfpakfeed",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {
//        }
//
//        /** Reference Refresh and FrameLayout **/
//        refresh_howl = (ImageView) findViewById(R.id.imageView2);
//
//        /** Dialogs **/
//        report = (ImageButton) findViewById(R.id.imageButton);
//        share = (ImageButton) findViewById(R.id.imageButton1);
//
//        network.initializeQueryString();
//
//        /** Pull Howls from Server **/
//        network.getHowls();
//
//        /** Facebook Share Feature **/
//        callbackManager = CallbackManager.Factory.create();
//        List<String> permissionNeeds = Arrays.asList("publish_actions");
//        manager = LoginManager.getInstance();
//        manager.logInWithPublishPermissions(this, permissionNeeds);
//        shareDialog = new ShareDialog(this);
//        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                share.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View arg0) {
////                        if (howls.getVisibility() == View.VISIBLE || switch_howls.getVisibility() == View.VISIBLE){
////                            sharePicFB();
////                        } else {
////                            shareVideoFB();
////                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onCancel() {
//                System.out.println("onCancel");
//            }
//
//            @Override
//            public void onError(FacebookException exception) {
//                System.out.println("onError");
//            }
//        });
//
//        /** Report_Button Listener **/
//        report.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                network.reportHowl();
//            }
//        });
//
//        /** Refresh_Button Listener **/
//        refresh_howl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                number = 0;
//                customView.num = 0;
//                network.getHowls();
//            }
//        });
//    }
//
//    /** Share Picture to Facebook **/
//    public void sharePicFB(ImageView imageView) {
//        imageView.buildDrawingCache();
//                Bitmap image = imageView.getDrawingCache();
//        SharePhoto photo = new SharePhoto.Builder()
//                        .setUserGenerated(true)
//                        .setBitmap(image)
//                .setCaption("#WOLFPAK2015")
//                        .build();
//                SharePhotoContent content = new SharePhotoContent.Builder()
//                        .addPhoto(photo)
//                        .build();
//                shareDialog.show(content);
//
//    }
//
//    /** Share Video to Facebook **/
//    public void shareVideoFB(String url){
//                    Uri mUri = Uri.parse(url);
//                    try {
//                        Field mUriField = VideoView.class.getDeclaredField("mUri");
//                        mUriField.setAccessible(true);
//                       // mUri = (Uri)mUriField.get(howls1);
//                    } catch(Exception ignored) {
//                    }
//
//                    ShareVideo video= new ShareVideo.Builder()
//                            .setLocalUrl(mUri) //alwaysNull - Check later
//                            .build();
//                    ShareVideoContent content = new ShareVideoContent.Builder()
//                            .setVideo(video)
//                            .build();
//
//                    shareDialog.show(content);
//    }
//
//    @Override
//    /**Facebook ReInitializer **/
//    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//    }
}