package com.example.doorplatesystem.hardware;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.SmatekManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.example.doorplatesystem.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xpf on 2017/6/3 :)
 * 检测APP页面是否一直运行,不运行就直接启动
 */

public class MonitoringService extends Service {

    private final static String TAG = "MonitoringService";
    SmatekManager smatekManager;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("kill_self".equals(intent.getAction())) {
                Log.e(TAG, "onReceive:杀死自己的进程！");
                killMyselfPid(); // 杀死自己的进程
            }
        }
    };

    private Timer timer = new Timer();
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            checkIsAlive();
        }
    };


    @SuppressLint("WrongConstant")
    public static void shutdownNotify(Context context){
        SmatekManager smatekManager = (SmatekManager) context.getSystemService("smatek");
        // 模拟键值输入
        long now = SystemClock.uptimeMillis();
//                KeyEvent down =  new KeyEvent(now, now,KeyEvent.ACTION_DOWN, 25, 0);
//                KeyEvent up = new KeyEvent(now, now,KeyEvent.ACTION_UP, 25, 0);
//                smatekManager.injectInputEvent(down);
//                smatekManager.injectInputEvent(up);

        //模拟触摸点触摸
        MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 1223, 694, 0);
        MotionEvent event1 = MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 1223, 694, 0);
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) {
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        }

        if ((event1.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) {
            event1.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        }
        smatekManager.injectInputEvent(event);
        smatekManager.injectInputEvent(event1);
    }

    /**
     * 检测应用是否活着
     */
    private void checkIsAlive() {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.CHINA).format(new Date());
        Log.e(TAG, "CustodyService Run: " + format);

        boolean AIsRunning = CheckUtil.isForeground2(
                MonitoringService.this, "MediaShowActivity");
        boolean BIsRunning = CheckUtil.isForeground2(
                MonitoringService.this, "MainActivity");
        boolean flag = (AIsRunning || BIsRunning);
//        boolean CIsRunning = CheckUtil.isClsRunning(
//                MonitoringService.this, "com.example.myapplicationdfsd", "com.example.myapplicationdfsd.MainActivity");

//        Log.e(TAG, "AIsRunning || BIsRunning is running:" + b + ",CIsRunning:" + CIsRunning); com.example.myapplicationdfsd.software.activity.

//        if (!CIsRunning) {
            if (!flag) { //如果界面挂掉直接启动AActivity
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(MonitoringService.this, MainActivity.class);
                startActivity(intent);
            }
//        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: 启动监控服务! ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("kill_self");
        registerReceiver(broadcastReceiver, intentFilter);
        timer.schedule(task, 0, 1000*10);// 设置检测的时间周期(毫秒数)
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * 杀死自身的进程
     */
    private void killMyselfPid() {
        int pid = android.os.Process.myPid();
        String command = "kill -9 " + pid;
        Log.e(TAG, "killMyselfPid: " + command);
        stopService(new Intent(MonitoringService.this, MonitoringService.class));
        try {
            Runtime.getRuntime().exec(command);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if (task != null) {
            task.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }
}