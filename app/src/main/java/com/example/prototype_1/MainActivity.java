package com.example.prototype_1;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;


public class MainActivity extends AppCompatActivity  implements  SensorEventListener, View.OnClickListener {


    float sensorX;   //x軸方向の加速度
    float sensorY;   //y軸方向の加速度
    float sensorZ;   //z軸方向の加速度
    Double compACC; //合成加速度

    int i;
    private TextView runtimerText;


    float Atotal;   //合成加速度の合計
    float Aave;     //合成加速度の平均
    float Adist;    //合成加速度の分散

    int flag;
    String act = "";

    int action_cnt = 0;

    int cnt = 0;  //ID
    int cursor = 0;
    int DIST_LIMIT = 80;
    Double distribute[] = new Double[DIST_LIMIT];

    private Runnable fw_runnalbe;  //ファイルのかきこみ
    private Handler mhandler = new Handler();
    private Chronometer chronometer;

    private Boolean firstRun = false;


    private Button startButton;
    private Thread thread;

    private final Handler handler = new Handler();
    private volatile boolean stopRun = false;


    private SimpleDateFormat dataFormat = new SimpleDateFormat("mm:ss.SS", Locale.JAPAN);

    String nowtime;
    long mmtime = 0;
    long diffTime = 0;
    public long startRunTime = 0;
    public long startWalkTime = 0;
    public long stopRunTime = 0;
    public long stopWalkTime = 0;
    public long nowRunTime = 0;
    public long nowWalkTime = 0;
    private Timer runTimer = null;
    private Timer walkTimer = null;
    private CountUpRunTimerTask countUpRunTimerTask = null;
    private CountUpWalkTimerTask countUpWalkTimerTask = null;
    private int runTimerState = 0;
    private int walkTimerState = 0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chronometer = findViewById(R.id.chronometer);


//        コメントアウト消すとボタンとタイマー機能
        runtimerText = findViewById(R.id.run_time);
        runtimerText.setText(dataFormat.format(0));

        runTimer = new Timer();
        walkTimer = new Timer();

//        startButton = findViewById(R.id.startButton);
//        startButton.setOnClickListener((View.OnClickListener) this);
//
//        Button stopButton = findViewById(R.id.stopButton);
//        stopButton.setOnClickListener((View.OnClickListener) this);


        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);    //システムセンサーサービスの取得
        Sensor acc_sensor;     //加速度センサーの登録
        if (sm != null) {
            acc_sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener((SensorEventListener) this, acc_sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onClick(View v) {
        Thread thread;
        if (v == startButton){
            stopRun = false;
            thread = new Thread((Runnable) this);
            thread.start();

            startRunTime = System.currentTimeMillis();

        }
        else{
            stopRun = true;
            runtimerText.setText(dataFormat.format(0));
        }
    }



    @Override
    protected void onResume() {
        super.onResume();


        final File log = getFilesDir(null);

        File file = new File(getFilesDir(), "/testapp");
        FileWriter fileWriter = null;

        //1行目の書き込み
        try {
            /*本物
            FileWriter fw = new FileWriter(getExternalFilesDir(null) + "/app.csv", true);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));*/

            fileWriter = new FileWriter(file, true);
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(fileWriter));

            printWriter.print("ID");
            printWriter.print(",");
            printWriter.print("Time");
            printWriter.print(",");
            printWriter.print("X");
            printWriter.print(",");
            printWriter.print("Y");
            printWriter.print(",");
            printWriter.print("Z");
            printWriter.print(",");
            printWriter.print("comp_ACC");
            printWriter.print(",");
            printWriter.print("Dist");
            printWriter.print(",");
            printWriter.print("Action");

            printWriter.println();
            printWriter.close();

        } catch (IOException e) {
            Log.d("MainActivity", e.toString());
        }


        //2行目以降の書き込み
        fw_runnalbe = new Runnable() {
            @Override
            public void run() {

                if (mmtime == 0) {
                    mmtime = System.currentTimeMillis();  //現時刻をミリ秒で取得
                } else {
                    mmtime += (long) 20;
                }

                final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:sss", Locale.JAPAN);
                nowtime = df.format(mmtime);  //System.currentTimeMillsで得た値を変換

                Log.d("ACC", nowtime + "x = " + sensorX + ", y = " + sensorY + ", z = " + sensorZ + "," + Adist);

                //消す
                File file = new File(getFilesDir(), "/testapp");
                FileWriter fileWriter = null;

                // ファイルの書き込み
                try {
                   /*本物
                    FileWriter fw = new FileWriter(log + "/app.csv", true);
                    PrintWriter pw = new PrintWriter(new BufferedWriter(fw));*/

                    fileWriter = new FileWriter(file, true);
                    PrintWriter printWriter = new PrintWriter(new BufferedWriter(fileWriter));

                    printWriter.print(cnt);
                    printWriter.print(",");
                    printWriter.print(nowtime);
                    printWriter.print(",");
                    printWriter.print(sensorX);
                    printWriter.print(",");
                    printWriter.print(sensorY);
                    printWriter.print(",");
                    printWriter.print(sensorZ);
                    printWriter.print(",");
                    printWriter.print(compACC);
                    printWriter.print(",");
                    printWriter.print(Adist);
                    printWriter.print(",");
                    printWriter.print(act);

                    printWriter.println();

                    printWriter.close();

                } catch (IOException e) {
                    Log.d("MainActivity", e.toString());
                }

                TextView action = findViewById(R.id.action);
                action.setText("行っている行動");
                TextView action1 = findViewById(R.id.action1);
                action1.setText("つまずき回数 : " + action_cnt);
                TextView action2 = findViewById(R.id.action2);
                action2.setText("走行時間");
                TextView action3 = findViewById(R.id.action3);
                action3.setText("歩行時間");



                if (cnt < DIST_LIMIT) {             //80個になるまで配列に格納

                    distribute[cnt] = compACC;

                } else {                           //81個目からは配列の先頭に戻って、順番に入れ替えていく

                    if (cursor < DIST_LIMIT) {
                        distribute[cursor] = compACC;
                        cursor++;

                    } else {                        //配列の入れ替えが80個終わったらまた先頭に戻る
                        cursor = 0;
                    }
                }
                if (size(distribute) < DIST_LIMIT) {  //データが80個溜まるまで計算しない

                    act_recognition();
                    cnt++;

                } else {                                   //100個のデータの分散を求める

                    Atotal = 0;
                    Adist = 0;
                    for (i = 0; i < DIST_LIMIT; i++) {
                        Atotal += distribute[i];
                    }

                    Aave = Atotal / DIST_LIMIT;

                    for (i = 0; i < DIST_LIMIT; i++) {
                        Adist += (distribute[i] - Aave) * (distribute[i] - Aave);
                    }

                    Adist = Adist / DIST_LIMIT;

                    act_recognition();
                    cnt++;

                }

                //10ms遅らせる
                mhandler.postDelayed(this, 20L);
            }
        };
        mhandler.post(fw_runnalbe);
    }


    private File getFilesDir(Object o) {
        return null;
    }


    //加速度の取得
    @Override
    public void onSensorChanged(SensorEvent event) {

        //センサの値が変化すると呼ばれる
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];

            //合成加速度
            compACC = Math.sqrt(sensorX * sensorX + sensorY * sensorY + sensorZ * sensorZ);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサの精度が変更されると呼ばれる
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sm != null) {
            sm.unregisterListener((SensorEventListener) this);
        }

        mhandler.removeCallbacks(fw_runnalbe); //Runnableの変数を()に入れた
    }


    public int size(Double[] array) {
        int count = 0;
        for (Double prop : array) {
            if (prop != null) {
                count++;
            }
        }
        return count;
    }

    public void act_recognition() {
        //行動の判別
        Thread thread;
        if ((compACC > 17 && compACC < 35) && (sensorX < 2 && sensorX > -8) && (sensorY > 11 && sensorY < 45) && (sensorZ > -6 && sensorZ < 3) && flag != 1)  {//つまずき
            flag = 1;
            action_cnt += 1;
            runTimerState = 0;

        } else if ((compACC > 17 && compACC < 35) && (sensorX < 2 && sensorX > -8) && (sensorY > 11 && sensorY < 45) && (sensorZ > -6 && sensorZ < 3)) {
            flag = 1;
            runTimerState = 0;

        } else if (0.6 < Adist && Adist < 14) {//歩く
            flag = 2;
            if(runTimer !=)
            runTimerState = 0;
            if(walkTimerState == 0){
                walkTimerState = 1;
                walkStart();
                countUpWalkTimerTask = new CountUpWalkTimerTask(this,this);
                walkTimer = new Timer(true);
                walkTimer.schedule(countUpWalkTimerTask, 0, 100);
            }

        } else if (25 < Adist) {
              if(runTimerState == 0){
                  runTimerState = 1;
                  runStart();
                  countUpRunTimerTask = new CountUpRunTimerTask(this,this);
                  runTimer = new Timer(true);
                  runTimer.schedule(countUpRunTimerTask, 0, 100);
              }
//            if (!firstRun && flag == 4 || flag == 2 || flag == 5) {
//                chronometer.setBase(SystemClock.elapsedRealtime());
//                chronometer.start();
//                firstRun = true;
//            }
//
//            if(action_cnt == 2 && diffTime < 15000){
//                TextView act1 = findViewById(R.id.act1);
//                act1.setText("原因 : 疲労 ");
//            }

            flag = 3;

        }else if(sensorZ < 4.62 && sensorY < 0.55 && sensorX < -9.5){//転倒
            flag = 4;


        } else if (0.5 > Adist) {//直立
            flag = 5;
        }


        if (flag == 1) {

            /*if(firstRun){
                chronometer.stop();
                firstRun = false;
            }*/

            act = "つまずき";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        } else if (flag == 2) {

//            if(firstRun){
//                chronometer.stop();
//                firstRun = false;
//            }

            act = "歩く";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        } else if (flag == 3) {
//            chronometer.start();

            /*if(action_cnt > 1 &&  diffTime < 10000){
                TextView act1 = findViewById(R.id.act1);
                act1.setText("原因 : 疲労 ");
            }*/

            act = "走る";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);


        } else if (flag == 4) {
//            if(firstRun){
//                chronometer.stop();
//                firstRun = false;
//            }


            act = "転倒";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        } else if (flag == 5) {
//            if(firstRun){
//                chronometer.stop();
//                firstRun = false;
//            }

            act = "直立";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        }
    }

//    @Override
//    public void run() {
//        int period = 10;
//
//        while (!stopRun) {
//            // sleep: period msec
//            try {
//                Thread.sleep(period);
//            }
//            catch (InterruptedException e) {
//                e.printStackTrace();
//                stopRun = true;
//            }
//
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    long endTime = System.currentTimeMillis();
//                    // カウント時間 = 経過時間 - 開始時間
//                    long diffTime = (endTime - startRunTime);
//
//                    timerText.setText(dataFormat.format(diffTime));
//
//                }
//            });
//        }
//    }
    public void runStart() {
        startRunTime = System.currentTimeMillis();
    }

    public void runStop() {
        stopRunTime += System.currentTimeMillis() - startRunTime;
    }

    public void runRestart() {
        startRunTime = System.currentTimeMillis();
    }
    public void runreset() {
        stopRunTime = 0;
    }
    public void walkStart() {
        startWalkTime = System.currentTimeMillis();
    }

    public void walkStop() {
        stopWalkTime += System.currentTimeMillis() - startWalkTime;
    }

    public void walkRestart() {
        startWalkTime = System.currentTimeMillis();
    }
    public void walkReset() {
        stopWalkTime = 0;
    }

    public String getRunDispString() {
        nowRunTime =  System.currentTimeMillis() - (startRunTime - stopRunTime);
        long temp = nowRunTime;
        return dataFormat.format(nowRunTime);
    }
    public String getWalkDispString() {
        nowWalkTime =  System.currentTimeMillis() - (startWalkTime - stopWalkTime);
        long temp = nowWalkTime;
        return dataFormat.format(nowWalkTime);
    }

    /*   public void act_recognition() { //ver.歩くをカウント
           //行動の判別
           if (2 > sensorY && 2 > sensorZ) {
               flag = 1;
               //TextView action1 = findViewById(R.id.action1);
               //action1.setText("転倒回数 : " +  action_cnt);
               //action_cnt += 1;

           } else if(0.6 < Adist && Adist < 14 && flag != 2) {
               flag = 2;
               action_cnt += 1;
           }else if(0.6 < Adist && Adist < 14 ){
               flag = 2;
           }else if(25 < Adist){
               flag = 3;
           }else if(0.5 > Adist){
               flag = 4;
           }


           if (flag == 1) {
               act = "転倒";
               TextView act0 = findViewById(R.id.act0);
               act0.setText(act);

           } else if( flag == 2){

               act = "歩く";
               TextView act0 = findViewById(R.id.act0);
               act0.setText(act);

           } else if( flag == 3){

               act = "走る";
               TextView act0 = findViewById(R.id.act0);
               act0.setText(act);

           } else if (flag == 4){
               act = "直立";
               TextView act0 = findViewById(R.id.act0);
               act0.setText(act);

           }
       }*/

   /* public void act_recognition() {
        //行動の判別

        if (sensorZ < 4.62 && sensorY < 0.55 && sensorX < -9.5) { //転倒
            flag = 1;

        } else if (0.6 < Adist && Adist < 14) { //歩行
            flag = 2;

        } else if (25 < Adist) { //走行
            flag = 3;

            startRunTime = System.currentTimeMillis();

            for(int i = 0; i < 100000; i++){
                result +=1;
            }

            chronometer.start();

        }else if( (compACC > 17 && compACC < 45) && (sensorX < 2 && sensorX > -8) && (sensorY > 11 && sensorY < 45) && (sensorZ > -6 && sensorZ < 3) && flag != 4) {//つまずき
            flag = 4;
            action_cnt += 1;

        }else if( (compACC > 17 && compACC < 45) && (sensorX < 2 && sensorX > -8) && (sensorY > 11 && sensorY < 45) && (sensorZ > -6 && sensorZ < 3)){
            flag =4;
        }else if (0.5 > Adist) {
            flag = 5;
        }


        if (flag == 1) {
            act = "転倒";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        } else if (flag == 2) {

            act = "歩く";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        } else if (flag == 3) {

            act = "走る";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        } else if (flag == 4) {
            act = "つまずき";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        } else if (flag == 5) {
            act = "直立";
            TextView act0 = findViewById(R.id.act0);
            act0.setText(act);

        }
    }*/
}

