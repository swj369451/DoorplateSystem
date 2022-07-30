package com.example.doorplatesystem.hardware;


public abstract class Button implements InputDevices {

    private static final Long PRESS_TIME = 5_000L;
    protected Callback mCallback;

    void onClick(){
        if(mCallback!=null){
            mCallback.onClick();
        }
    };
     void onDoubleClick(){
         if(mCallback!=null){
             mCallback.onDoubleClick();
         }
     };
     void onLongPress(){
         if(mCallback!=null){
             mCallback.onLongPress();
         }
     };

    public interface Callback{
        void onClick();
        void onDoubleClick();
        void onLongPress();
    }
}
