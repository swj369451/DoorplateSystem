package com.example.doorplatesystem.media;


import com.example.doorplatesystem.webrtc.SignalingClient;
import com.example.doorplatesystem.webrtc.WebRTC;

import org.webrtc.MediaStream;

public class Media {
    private static Media instance;

    private Media() {
    }

    public static Media get() {
        if (instance == null) {
            synchronized (SignalingClient.class) {
                if (instance == null) {
                    instance = new Media();
                }
            }
        }
        return instance;
    }

    public MediaStream getDisplayMediaStream(){
        MediaStream screenStream = WebRTC.get().getScreenStream();
        return screenStream;
    }
}
