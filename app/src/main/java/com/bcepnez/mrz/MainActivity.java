package com.bcepnez.mrz;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SurfaceView cameraview;
    TextView text;
    TextView text2;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RequestCameraPermissionID:{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Button b1 = (Button)findViewById(R.id.button2);
//        b1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(MainActivity.this,MyCameraActivity.class);
//                startActivity(i);
//            }
//        });

        cameraview = (SurfaceView) findViewById(R.id.surface_view);
        text = (TextView) findViewById(R.id.text_view);
        text2 = (TextView) findViewById(R.id.text_view1);

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detect dependencier are not available");
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 720)
                    .setRequestedFps(1.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            cameraview.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });


            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size()!=0){

                        text.post(new Runnable() {
                            int index=0;
                            String[] arr = new String[45];

                            @Override
                            public void run() {

                                StringBuilder stringBuilder = new StringBuilder();
                                StringBuilder stringBuilder2 = new StringBuilder();
                                for (int i = 0;i<items.size();++i){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder2.append(items.size());
                                    stringBuilder2.append(" ");
                                    stringBuilder.append("\n");

                                    if(item.getValue().startsWith("P")&&
                                            item.getValue().endsWith("<")&&
                                            item.getValue().trim().length()>30) {
                                        arr[index] = item.getValue();
                                        index++;
                                        for (int j =0;j<index;j++){
                                            stringBuilder2.append("Found : ");
                                            stringBuilder2.append(arr[j]);
//                                            stringBuilder2.append(item.getValue());
                                            stringBuilder2.append(" index : ");
                                            stringBuilder2.append(arr[j].length());
                                            stringBuilder2.append("\n");
                                        }

                                    }
                                }
                                text.setText(stringBuilder.toString());
                                text2.setText(stringBuilder2.toString());
                            }

                        });
                    }
                }
            });
        }
    }
}
