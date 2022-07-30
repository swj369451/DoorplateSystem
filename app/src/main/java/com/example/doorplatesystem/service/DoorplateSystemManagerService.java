package com.example.doorplatesystem.service;

import android.app.Service;
import android.app.SmatekManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.doorplatesystem.MainActivity;
import com.example.doorplatesystem.hardware.Button;
import com.example.doorplatesystem.hardware.DoorplateFunctionButtonGroup;
import com.example.doorplatesystem.hardware.GPIOButton;


public class DoorplateSystemManagerService extends Service {

    public static final String TAG = "DoorplateSystemManagerService";

    public static SmatekManager smatekManager;
    private DoorplateFunctionButtonGroup doorplateFunctionButtonGroup;

//    public class MyBinder extends Binder{
//        public DoorplateSystemManagerService getService(){
//            return DoorplateSystemManagerService.this;
//        }
//    }


//    private MyBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        //init button group
        this.doorplateFunctionButtonGroup = new DoorplateFunctionButtonGroup(smatekManager);
        GPIOButton gpio2Button1 = new GPIOButton(GPIOButton.GPIO_PATH+'4', GPIOButton.ONE,
                GPIOButton.ZERO,
                DoorplateFunctionButtonGroup.GPIO_2_BUTTON_NAME1,
                new Button.Callback() {
                    @Override
                    public void onClick() {
                        MainActivity.redButtonClick();
                    }

                    @Override
                    public void onDoubleClick() {

                    }

                    @Override
                    public void onLongPress() {

                    }
                });
        GPIOButton gpio2Button2 = new GPIOButton(GPIOButton.GPIO_PATH+'5',
                GPIOButton.ZERO,GPIOButton.ONE,
                DoorplateFunctionButtonGroup.GPIO_2_BUTTON_NAME2,
                new Button.Callback() {
                    @Override
                    public void onClick() {
                        MainActivity.blueButtonClick();
//                        if(MainActivity.startConnect.get()){
//                            MainActivity.startConnect.set(false);
//                            Intent dialogIntent = new Intent(getBaseContext(), MainActivity.class);
//                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //必须添加 Intent.FLAG_ACTIVITY_NEW_TASK
//                            getApplication().startActivity(dialogIntent);
//                        }else{
//                            MainActivity.startConnect.set(true);
//                            Intent dialogIntent = new Intent(getBaseContext(), MediaShowActivity.class);
//                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //必须添加 Intent.FLAG_ACTIVITY_NEW_TASK
//                            getApplication().startActivity(dialogIntent);
//                        }
                    }

                    @Override
                    public void onDoubleClick() {

                    }

                    @Override
                    public void onLongPress() {

                    }
                });
        doorplateFunctionButtonGroup.addButton(gpio2Button1);
        doorplateFunctionButtonGroup.addButton(gpio2Button2);
        doorplateFunctionButtonGroup.init();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        return binder;
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(this.smatekManager==null){
            Log.e(TAG,"smatekManager is not init");
        }

        Log.e(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
