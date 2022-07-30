package com.example.doorplatesystem.hardware;

import java.util.concurrent.atomic.AtomicBoolean;

public class GPIOButton extends Button {

    public final static String ZERO = "0";
    public final static String ONE = "1";
    public final static String GPIO_PATH = "sys/class/smatek_gpio/gpio5b";

    private String mGIIOPath;
    private String mInitValue;
    private String mActivityValue;
    private String mButtonName;
    private AtomicBoolean mActivity = new AtomicBoolean(false);


    public GPIOButton(String mGIIOPath, String mInitValue, String mActivityValue, String mButtonName) {
        this(mGIIOPath,mInitValue,mActivityValue,mButtonName,null);
    }

    public GPIOButton(String mGIIOPath, String mInitValue, String mActivityValue, String mButtonName,Callback callback) {
        this.mGIIOPath = mGIIOPath;
        this.mInitValue = mInitValue;
        this.mActivityValue = mActivityValue;
        this.mButtonName = mButtonName;
        this.mCallback = callback;
    }

    public void active(){
        this.mActivity.set(true);
    }
    public void inactive(){
        this.mActivity.set(false);
    }

    public Boolean isActive(){return mActivity.get();}

    public String getmGIIOPath() {
        return mGIIOPath;
    }

    public String getmInitValue() {
        return mInitValue;
    }

    public String getmActivityValue() {
        return mActivityValue;
    }

    public String getmButtonName() {
        return mButtonName;
    }

    public AtomicBoolean getmActivity() {
        return mActivity;
    }

    @Override
    public void onClick() {
        super.onClick();

    }

    @Override
    public void onDoubleClick() {
        super.onDoubleClick();

    }

    @Override
    public void onLongPress() {
        super.onLongPress();

    }
}
