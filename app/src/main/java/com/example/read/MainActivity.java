package com.example.read;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.ImageView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private VideoView video_view;
    private ImageView img;
    private File outputfile;
    private EditText Frame;
    private TextView txt;
    private Button Playbut,Convr;
    private static final String TAG = "hihihi";
    LineChartData lineChartData;
    LineChart lineChart;
    ArrayList<String> xData = new ArrayList<>();
    ArrayList<Entry> yData = new ArrayList<>();
    Integer[][] PICCONV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        video_view =(VideoView) findViewById(R.id.video_view2);
        img = (ImageView) findViewById(R.id.imageView);
        Frame = (EditText) findViewById(R.id.editTextNumber);
        txt = (TextView)findViewById(R.id.textView);
        Playbut = (Button)findViewById(R.id.button);
        Convr = (Button)findViewById(R.id.Convert);
        lineChart = findViewById(R.id.Linechart);

    }
        @Override
        protected void onActivityResult (int requestCode, int resaultCode, Intent data){
            super.onActivityResult(requestCode,resaultCode,data);
            if (resaultCode == Activity.RESULT_OK){
                Uri uri1 = data.getData();
                ArrayList<ArrayList<Integer>>Pix= new ArrayList<>();


                lineChartData = new LineChartData(lineChart,this);
                video_view.setVideoURI(uri1);
                video_view.setKeepScreenOn(true);
                MediaController controller = new MediaController(this);
                controller.setMediaPlayer(video_view);
                video_view.setMediaController(controller);

                Playbut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Long Time = Long.parseLong(Frame.getText().toString());
                            processAndShowGrayscaleVideo(uri1, Time);
                        }catch (Exception e){
                            Log.e(TAG, "Doesn't import Frame "+ e.toString() );
                        }
                    }
                });
                Convr.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                                long VDur = video_view.getDuration();
                                VDur = VDur/1000;
                                Log.d(TAG, "Convert: Video Duration " + VDur);
                                ArrayList rows = new ArrayList();
                                Pix.clear();
                                Integer[][] PICCONV = new Integer[(int)VDur][1000000];
                                for (int i = 0 ; i < VDur ;i++){
                                    Bitmap gryPic = processAndShowGrayscaleVideo(uri1,(long)i);
                                    int height =gryPic.getHeight();
                                    int width = gryPic.getWidth();
                                    //Pix.add(rows);
                                    //Log.d(TAG, "width height " +width + height);
                                    int z=0;
                                    for (int j = width/3; j < width*2/3; j++) {
                                        for (int k = height/4; k < height*3/4; k++){

                                                PICCONV[i][z]=gryPic.getPixel(j, k);
                                                z++;
                                                //Pix.get(i).add(gryPic.getPixel(j,k));

                                        }
                                    }
                                    Log.d(TAG, "done pic "+i+" ph");
                                    //Log.e(TAG, "PICCON.length : " + PICCONV.length);
                                    //Log.e(TAG, "PICCON.1: " + PICCONV[i][1]);

                                }

                                    for (int l = 0; l < PICCONV.length; l++) {
                                        //Log.d(TAG, "onClick: " + Pix.get(l).get(50));
                                        Integer DataX = PICCONV[l][0];
                                        Log.d(TAG, "PICCON.1: " + PICCONV[l][0]);
                                        //Log.d(TAG, "pix.2: " + Pix.get(6));
                                        yData.add(new Entry(DataX, l));
                                        String num = String.valueOf(l);
                                        xData.add(num);
                                    }
                                        lineChartData.initX(xData);
                                        lineChartData.initY(-20000000F, 0F);
                                        lineChartData.initDataSet(yData);
                                        Log.d(TAG,"Ydata length " + yData);
                                }catch (Exception e){
                                    Log.e(TAG,"All error " + e.toString());
                                }
                                //Log.d(TAG, "Array all " + Pix.get(0).get(23) + Pix.get(0));
                    }
                });
            }
        }
        public void read(View view) {
            try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video/*");
            startActivityForResult(intent, 1);
            Toast.makeText(MainActivity.this,"Done",Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(MainActivity.this,"Fail",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        public void clickRecord(View view){

            Uri uri1 = getOutputFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri1);
            startActivityForResult(intent, 1);
        }
        private Bitmap processAndShowGrayscaleVideo(Uri ur1,Long Time) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, ur1);

                String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long duration = Long.parseLong(durationString);
                duration = duration/1000;
                //duration = duration*100;
                Log.d(TAG, "processAndShowGrayscaleVideo: "+duration + " Time " + Time);
                //Log.d(TAG, "Bitmap second: " + Time);

                if (Time<duration) {
                Time= Time *1000000;
                Bitmap pic = retriever.getFrameAtTime(Time,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                Bitmap Gpic = convertToGrayScale(pic);
                img.setImageBitmap(Gpic);
                return Gpic;
                  }
                else {
                    Log.e(TAG, "Fail frame at time "+Time );
                    return null;
            }
        }
        public Bitmap convertToGrayScale(Bitmap originalBitmap) {
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                //Log.d(TAG,"Orignal" +"width: "+ width +"height: "+height);
                //Pix.add(originalBitmap.getPixel(width/2,height/2));
                Bitmap grayScaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                for (int i = width/3; i < width*2/3; i++) {
                    for (int j = height/4; j < height*3/4; j++){
                        int pixel = originalBitmap.getPixel(i, j);
                        int grayScale = (int) (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114);
                        grayScaleBitmap.setPixel(i, j, Color.rgb(grayScale, grayScale, grayScale));
                    }
                }
                    return grayScaleBitmap;
    }
        private Uri getOutputFile(){

            File medidaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES),"AnkaranaWork"
                    );
            if (!medidaStorageDir.exists()){
                if(!medidaStorageDir.mkdir()){
                    Log.d("AnkaranaWork","failed to create directory");
                    return null;
                }
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile = new File(medidaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
            return Uri.fromFile(mediaFile);
        }
    }