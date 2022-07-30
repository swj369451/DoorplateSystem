package com.example.doorplatesystem.hardware;

import android.app.SmatekManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class DoorplateFunctionButtonGroup implements InputDevices {
    public static final String TAG = "DoorplateFunctionButtonGroup";
    public static final String GPIO_2_BUTTON_NAME1 = "gpio2Button1";
    public static final String GPIO_2_BUTTON_NAME2 = "gpio2Button2";

    private SmatekManager smatekManager;
    private Map<String,GPIOButton> gpioButtonGroup = new HashMap<>();

    public DoorplateFunctionButtonGroup(@NonNull SmatekManager smatekManager) {
        this.smatekManager = smatekManager;
        if(smatekManager!=null){
            active.set(true);
        }
    }

    public void addButton(GPIOButton gpioButton) {
        if(active.get()){
            gpioButtonGroup.put(gpioButton.getmButtonName(),gpioButton);
        }else {
            Log.e(TAG,"device isn't active");
        }
    }


    public void init() {
        if(active.get()){
            gpioButtonGroup.forEach((key,value)->{
                value.active();

                smatekManager.writeToNode(value.getmGIIOPath(), value.getmInitValue());
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        while (value.isActive()){
                            // todo 单机和双击还不够
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String dataFromNode = smatekManager.getDataFromNode(value.getmGIIOPath());
                            if(value.getmActivityValue().equals(dataFromNode)){
                                value.onClick();
                            }
                        }
                    }
                }.start();
            });
        }else {
            Log.e(TAG,"device isn't active");
        }

    }

    @Override
    public void active() {
        if(smatekManager==null){
            throw new IllegalArgumentException("smatekManager can't be null");
        }else {
            active.set(true);
        }
    }

    @Override
    public void inactive() {
        active.set(false);
    }
}
