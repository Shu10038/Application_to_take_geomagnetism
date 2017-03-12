package com.example.shuichiohsawa.magneticapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // ファイル名に挿入する日付を求める
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf_YMD = new SimpleDateFormat("yyyyMMdd");
    String strDate = sdf_YMD.format(cal.getTime());
    // 時刻の獲得．ファイルの書き込みの際に使う
    SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm:ss.SSS");
    String strTime = sdf_time.format(cal.getTime());
    // 間隔を空けてファイルに書き込む
    SimpleDateFormat sdf_filer = new SimpleDateFormat("SS");
    String filTime = sdf_filer.format(cal.getTime());

    // 機種固有のパス．Nexus5の場合は"内部ストレージの直下のようだ"
    String envPath = Environment.getExternalStorageDirectory().getPath();
    // 保存先とファイル名を指示する
    String filePath = envPath + "/sdcard/" + strDate + "_sensor_data.txt";
    // openFileOutputの宣言
    FileOutputStream fos = null;
    String fileString;
    String magString = "";

    float mVal;


    private String textData = "";
    private TextView varScan;


    // センサーマネージャー
    private SensorManager manager;

    // センサーイベントリスナー
    private SensorEventListener listener;

    // 加速度の値
    private float[] fAccell = null;

    // 地磁気の値
    private float[] fMagnetic = null;

    private static final int MATRIX_SIZE = 16;
    float[] inR = new float[MATRIX_SIZE];
    float[] outR = new float[MATRIX_SIZE];
    float[] I = new float[MATRIX_SIZE];

    //
    TextView textView1 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // センサーマネージャーを取得
        manager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // センサーのイベントリスナーを登録
        listener = new SensorEventListener() {

        String strTime = sdf_time.format(cal.getTime());
            // 値変更時
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {


                // センサーの種類で値を取得
                switch( sensorEvent.sensor.getType()) {

                    // 加速度
                    case Sensor.TYPE_ACCELEROMETER:
                        fAccell = sensorEvent.values.clone();
                        break;

                    // 地磁気
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        fMagnetic = sensorEvent.values.clone();

                        break;

                }

                // 両方確保した時点でチェック
                if(fAccell != null && fMagnetic != null){

                    // X，Y，Z軸方向の磁束密度を得る
                    magString = String.valueOf(fMagnetic[0]) + "," +
                                String.valueOf(fMagnetic[1]) + "," +
                                String.valueOf(fMagnetic[2])  ;

                    // 回転行列の計算
                    SensorManager.getRotationMatrix(inR, I, fAccell, fMagnetic);

                    // ワールド座標とデバイス座標のマッピングを変換する
                    SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);

                    // 姿勢の計算
                    float[] fAttitude = new float[3];
                    SensorManager.getOrientation(outR, fAttitude);

                    for (int i = 0; i < 3; i++) {
                        fAttitude[i] = (float)(fAttitude[i] * 180 / Math.PI);
                        fAttitude[i] = (fAttitude[i] < 0) ? fAttitude[i] + 360 : fAttitude[i];
                    }

                    // 出力内容を編集
                    String buf =
                            String.format( "方位角\n\t%f\n", fAttitude[0]) +
                            String.format( "前後の傾斜\n\t%f\n", fAttitude[1]) +
                            String.format( "左右の傾斜\n\t%f\n", fAttitude[2]);
                    textView1.setText( buf );


                    // 現在時刻を獲得
                    Calendar cal = Calendar.getInstance();
                    strTime = sdf_time.format(cal.getTime());


                    // 現在時刻を獲得
                    magString = strTime+ ","+magString + ","+
                            String.valueOf(fAttitude[0]) + ","+
                            String.valueOf(fAttitude[1])+ ","+
                            String.valueOf(fAttitude[2]) + "\n";


                    //　ファイルに書き込み
                    try{
                        fos = new FileOutputStream(filePath, true);
                        // ファイルの書き込み
                        fos.write(magString.getBytes());
                        // ファイルのクローズ
                        fos.close();
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }

                    //　変数の初期化
                    fAccell = null;
                    fMagnetic = null;

                }


            }

            //
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        // text
        textView1 = (TextView)findViewById(R.id.text1);

    }


    @Override
    protected void onResume() {
        super.onResume();

        strTime = sdf_time.format(cal.getTime());

        // リスナー設定：加速度
        manager.registerListener (
                listener,
                manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        // リスナー設定：地磁気
        manager.registerListener (
                listener,
                manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }


    @Override
    protected void onPause() {
        super.onPause();

        // リスナー解除
        manager.unregisterListener(listener);
    }

}
/*
参考にしたサイト
http://urx.blue/Ce0T
http://urx.blue/Ce0S
http://urx.blue/Ce0R
 */