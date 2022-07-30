package com.example.doorplatesystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.SmatekManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.doorplatesystem.hardware.CrashHandler;
import com.example.doorplatesystem.hardware.DoorplateUtil;
import com.example.doorplatesystem.hardware.MonitoringService;
import com.example.doorplatesystem.service.DoorplateSystemManagerService;
import com.example.doorplatesystem.webrtc.MediaCommunication;
import com.example.doorplatesystem.webrtc.WebRTC;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    static WebView webView;
    static String macAddress;
    public static String doorplateId;

    private Handler handler;
    private Timer timer;
    private TimerTask timerTask;
    private Timer timer1;
    private TimerTask timerTask1;
    private Context context;
    private WebRTC webRTC;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        setStatusBarFullTransparent();


        //检查设备的安卓版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }

        //检查软件版本
        if (!checkVersion()) {
            // 启动自重启
            Intent intent1 = new Intent(MainActivity.this, MonitoringService.class);
            intent1.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
            startService(intent1);
            handler = new Handler() {
                @SuppressLint("HandlerLeak")
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            MonitoringService.shutdownNotify(context);
                            break;
                        case 2:
                            int a = 1 / 0;
                            break;
                        default:
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
            rstart();
        }


        //门牌管理设备
        DoorplateSystemManagerService.smatekManager = (SmatekManager) getSystemService("smatek");
        Intent intent = new Intent(this, DoorplateSystemManagerService.class);
        startService(intent);

        if (DoorplateSystemManagerService.smatekManager == null) {

        } else {
            //获取mac地址
            macAddress = DoorplateSystemManagerService.smatekManager.getEthMacAddress();
            DoorplateUtil.getDoorplateId(this, macAddress);
        }

        webView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //适应屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        //允许DOM的本地存储
        webSettings.setDomStorageEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        CrashHandler.getInstance().init(getApplicationContext());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i("console", "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
//                super.onPermissionRequest(request);
                request.grant(request.getResources());
            }
        });
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("+++++++访问的连接：" + url);
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                    return false;
                } else {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url) );
//                    startActivity(intent);
                    return true;
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                System.out.println("+++++++访问的连接：" + url);
                super.onLoadResource(view, url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                super.onReceivedError(webView, webResourceRequest, webResourceError);
                if (webResourceRequest.isForMainFrame()) {//是否是为 main frame创建
                    webView.loadUrl("about:blank");// 避免出现默认的错误界面
                    webView.loadUrl("file:///android_asset/err.html");// 加载自定义错误页面
                }
            }
        };
        webView.setWebViewClient(webViewClient);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setWebContentsDebuggingEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        //设置js接口
        MediaCommunication mediaCommunication = new MediaCommunication(this);
            webView.addJavascriptInterface(mediaCommunication, "AndroidWebRTC");

        //屏幕共享
        webRTC = WebRTC.get();
        webRTC.init(this);
        webRTC.getScreenSourceData();


    }

    //权限
    private final static int PERMISSIONS_REQUEST_CODE = 1;
    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.VIBRATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.READ_LOGS,
//            Manifest.permission.WRITE_SETTINGS,
//            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
    };


    public static void startInit() {
        webView.post(new Runnable() {
            @Override
            public void run() {
                System.out.println("macAddress" + macAddress + "，doorplateId" + doorplateId);
                String state = "start";
                String url = "https://webrtccommunication.ppamatrix.com:1447/doorplatePage/page/doorplateTransit/doorplateTransit.html?macAddress="
                        + macAddress + "&state=" + state + "&deviceId=" + doorplateId;
//                  url="https://webrtccommunication.ppamatrix.com:1447/testrtc/index.html";
//                  url = "https://webrtccommunication.ppamatrix.com:1447/tool/webrtc/index.html?deviceId=test0";
//                    url = "https://webrtccommunication.ppamatrix.com:1447";
                url = "https://webrtccommunication.ppamatrix.com:1447/rtc/doorplate.html";
//                url = String.format("https://webrtccommunication.ppamatrix.com:1447/rtc/doorplate.html?macAddress=%s&state=%s&deviceId=%s", macAddress, state, doorplateId);
                webView.loadUrl(url);
            }
        });
    }

    public static void redButtonClick() {
        webView.post(new Runnable() {
            @Override
            public void run() {
                String state = "redButtonClick";
                String url = "https://webrtccommunication.ppamatrix.com:1447/doorplatePage/page/doorplateTransit/doorplateTransit.html?macAddress="
                        + macAddress + "&state=" + state + "&deviceId=" + doorplateId;
                webView.loadUrl(url);
            }
        });
    }


    public static void blueButtonClick() {
        webView.post(new Runnable() {
            @Override
            public void run() {
                String state = "blueButtonClick";
                String url = "https://webrtccommunication.ppamatrix.com:1447/doorplatePage/page/doorplateTransit/doorplateTransit.html?macAddress="
                        + macAddress + "&state=" + state + "&deviceId=" + doorplateId;
                webView.loadUrl(url);
            }
        });
    }

    /**
     * 全透状态栏
     */
    protected void setStatusBarFullTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    String upURL;
    String appName = "";

    private void sdkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {//高于6.0版本，动态申请权限
            if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 111);
            } else {
                new DownloadUtils(this, upURL, appName);
            }
        } else {
            new DownloadUtils(this, upURL, appName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 111:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //权限已经都通过了，可以下载apk到SDk中了
//                    new DownloadUtils(this,upURL,appName);
                } else {
                    // 没有申请权限
                    // 使用Toast来显示异常信息
                    new Thread() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            Toast.makeText(context, "没有申请权限", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }.start();
                }
                break;
            default:
        }
    }


    private void requestPermission() {
        if (!checkPermissionAllGranted()) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSIONS_REQUEST_CODE);
        }
        checkPermissionAllGranted();
    }

    private boolean checkPermissionAllGranted() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                Log.e("err", "权限" + permission + "没有授权");
                return false;
            }
        }
        return true;
    }

    private void initTimer() {

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        Date mydate = new Date();
        int hours = mydate.getHours();
        int minutes = mydate.getMinutes();
        int seconds = mydate.getSeconds();


        timer.schedule(timerTask, 30 * 1000, 20 * 1000);
    }

    private void rstart() {
        timer1 = new Timer();
        timerTask1 = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }
        };
        timer1.schedule(timerTask1, 24 * 60 * 60 * 1000, 24 * 60 * 60 * 1000);
    }

    boolean vflag = false;

    public boolean checkVersion() {
        String url = "https://gcc.ppamatrix.com:443/wss/appController/upApp?equipmentInfo=&appName=DoorplateSystem&version=" + getVersionName() + "&msg=&macAddress=" + macAddress;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 使用Toast来显示异常信息
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(context, "网络或服务异常，无法进行更新", Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }.start();
            }

            @Override
            public void onResponse(Call call, Response response) {
                String date = null;
                try {
                    date = response.body().string();
                } catch (IOException e) {
                    // 使用Toast来显示异常信息
                    new Thread() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            Toast.makeText(context, "版本服务返回出错", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }.start();
                }
                System.out.println(date);
                String str = "";
                try {
                    //String转JSONObject
                    JSONObject result = new JSONObject(date);
                    //取数据
                    int code = result.getInt("code");
                    System.out.println(code);
                    if (code == 0) {
                        JSONObject data = result.getJSONObject("data");
                        String state = data.getString("isOld");
                        if (state.equals("YES")) {
                            String url = data.getString("url");
                            upURL = url;
                            // 使用Toast来显示异常信息

                            String[] split = upURL.split("/");
                            appName = split[split.length - 1];
                            System.out.println(appName);
                            vflag = true;


                            boolean fl = isAvilible(getApplicationContext(), "com.example.appupdatehelper");
                            System.out.println("有更新程序" + fl);
                            if (fl) openApp(getApplicationContext(), "com.example.appupdatehelper");
                            else {
                                sdkPermission();
                            }
                            new Thread() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    Toast.makeText(context, url, Toast.LENGTH_LONG).show();
                                    initTimer();
                                    Looper.loop();


                                }
                            }.start();
                        } else {
                            // 使用Toast来显示异常信息
                            new Thread() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    Toast.makeText(context, "当前是最新版本", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }
                            }.start();
                        }
                    } else if (code == 500) {
                        // 使用Toast来显示异常信息
                        new Thread() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                Toast.makeText(context, "应用版本信息获取失败，无法进行更新", Toast.LENGTH_LONG).show();
                                Looper.loop();
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    // 使用Toast来显示异常信息
                    new Thread() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            Toast.makeText(context, "版本服务信息出错", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }.start();
                }
            }
        });
        return vflag;
    }

    private String getVersionName() {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        String version = "";
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version = packInfo.versionName;
            System.out.println("version：" + version);
        } catch (PackageManager.NameNotFoundException e) {
            // 使用Toast来显示异常信息
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(context, "应用版本信息获取失败，无法进行更新", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }.start();
        }
        return version;
    }


    /**
     * 检测Android是否安装了某个程序
     *
     * @param context
     * @param packageName：程序包名
     * @return
     */
    private boolean isAvilible(Context context, String packageName) {
        String tag = "isAvilible";
        final PackageManager packageManager = context.getPackageManager();

        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            Log.i(tag, "i=" + i + ", " + pinfo.get(i).packageName);

            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    private boolean openApp(Context context, String packageName) {
        Intent i = context.getPackageManager().getLaunchIntentForPackage(packageName);
        startActivity(i);
        return true;
    }

    public void readbut(View view) {
        redButtonClick();
    }

    public void bluebut(View view) {
        blueButtonClick();
    }

    /**
     * 处理屏幕分享
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WebRTC.REQ_REMOTE_HELP && resultCode == RESULT_OK) {
            webRTC.setScreenCapturer(data);
        }
    }
}