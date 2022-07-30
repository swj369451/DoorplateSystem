package com.example.doorplatesystem.hardware;

import android.app.Activity;
import android.content.Context;

public class ClipBoardService {

    private Context contxt;
    private Activity activity;

    public Context getContxt() {
        return contxt;
    }

    public void setContxt(Context contxt) {
        this.contxt = (Activity) contxt;
    }

    public Activity getActivity() {
        return (Activity) activity;
    }

    public void setActivity(Activity activity) {
        this.activity = (Activity) activity;
    }

    public ClipBoardService(Context context, Activity activity) {
        this.setContxt(context);
        this.setActivity(activity);
    }


}