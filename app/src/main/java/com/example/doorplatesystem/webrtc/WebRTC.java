package com.example.doorplatesystem.webrtc;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import androidx.activity.ComponentActivity;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static org.webrtc.ContextUtils.getApplicationContext;

public class WebRTC {
    private static WebRTC instance;
    private MediaStream mediaStream;

    private WebRTC() {
    }

    public static WebRTC get() {
        if (instance == null) {
            synchronized (SignalingClient.class) {
                if (instance == null) {
                    instance = new WebRTC();
                }
            }
        }
        return instance;
    }





    private SurfaceTextureHelper surfaceTextureHelper;
    private PeerConnectionFactory peerConnectionFactory;
    private EglBase.Context eglBaseContext;

    private Context context;

    private MediaProjectionManager manager;

    public static final int REQ_REMOTE_HELP = 1;
    private SurfaceViewRenderer surfaceViewRenderer;
    private ScreenCapturerAndroid videoCapturer;

    //初始化webrtc
    public void init(Context applicationContext) {
        this.context = applicationContext;
        eglBaseContext = EglBase.create().getEglBaseContext();
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);

        // create PeerConnectionFactory
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(applicationContext)
                .createInitializationOptions());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                new DefaultVideoEncoderFactory(eglBaseContext, true, true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory =
                new DefaultVideoDecoderFactory(eglBaseContext);
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();
    }

    public void renderView(SurfaceViewRenderer surfaceViewRenderer) {
        surfaceViewRenderer.init(eglBaseContext, null);
        this.surfaceViewRenderer=surfaceViewRenderer;
    }

    public void getScreenSourceData() {
        if (Build.VERSION.SDK_INT < 21) {
            Toast.makeText(context, "您的手机不支持这个功能", Toast.LENGTH_SHORT).show();
            return;
        }
        manager = (MediaProjectionManager) context.getSystemService(MEDIA_PROJECTION_SERVICE);
        if (manager == null) {
            Toast.makeText(context, "截屏服务不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = manager.createScreenCaptureIntent();
        ((ComponentActivity) context).startActivityForResult(intent, REQ_REMOTE_HELP);

    }
    //设置屏幕捕获器
    public void setScreenCapturer(Intent data) {
        videoCapturer = new ScreenCapturerAndroid(data, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
            }
        });
    }
    public MediaStream getScreenStream(){
        if(mediaStream!=null){
            return mediaStream;
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        //创建视频源
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture( wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight(), 30);
        //创建视频轨道
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("101", videoSource);
        // display in localView
        if(surfaceViewRenderer!=null){
            videoTrack.addSink(surfaceViewRenderer);
        }
        mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream");
        mediaStream.addTrack(videoTrack);
        return mediaStream;
    }

    public PeerConnectionFactory getPeerConnectionFactory() {
        return peerConnectionFactory;
    }
}
