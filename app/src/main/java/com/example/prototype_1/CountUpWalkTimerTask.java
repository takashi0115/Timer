package com.example.prototype_1;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import java.util.TimerTask;


public class CountUpWalkTimerTask extends TimerTask {

    private Handler handler;    //UIスレッドハンドラ
    private Context context;    //コンテキスト
    private MainActivity timeCtrl;

    //コンストラクタ
    public CountUpWalkTimerTask(Context con, MainActivity timerController) {
        this.handler = new Handler();
        this.context = con;
        this.timeCtrl = timerController;
    }


    //メインループ処理
    @Override
    public void run() {
        handler.post(new Runnable() {
            public void run() {
                String a = timeCtrl.getWalkDispString();
                TextView tv = (TextView) ((Activity) context).findViewById(R.id.walk_time);
                tv.setText(a);
            }
        });
    }
}