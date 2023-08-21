package com.example.read;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
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

        }

        @Override
        protected void onActivityResult (int requestCode, int resaultCode, Intent data){
            super.onActivityResult(requestCode,resaultCode,data);
            if (resaultCode == Activity.RESULT_OK){
                Uri uri1 = data.getData();
                ArrayList<ArrayList<Integer>>Pix= new ArrayList<>();


                video_view.setVideoURI(uri1);
                video_view.setKeepScreenOn(true);
                MediaController controller = new MediaController(this);
                controller.setMediaPlayer(video_view);
                video_view.setMediaController(controller);

                Playbut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long Time = Long.parseLong(Frame.getText().toString());
                        Time = Time*1000000;

                        processAndShowGrayscaleVideo(uri1, Time);
                        //Log.d(TAG, "onClick: Pix " + Pix.get(1));
                        //Toast.makeText(MainActivity.this,Pix.get(6),Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this,"playok", Toast.LENGTH_SHORT).show();
                    }
                });
                Convr.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {

                        try {
                                long VDur = video_view.getDuration();
                                VDur = VDur/100;
                                //Log.d(TAG, "Convert: Video Duration " + VDur);

                                for (int i = 0 ; i <= 1 ;i++){
                                    Bitmap gryPic = processAndShowGrayscaleVideo(uri1,(long)i*1000000);
                                    int height =gryPic.getHeight();
                                    int width = gryPic.getWidth();
                                    ArrayList rows = new ArrayList();
                                    Pix.add(rows);
                                    //Log.d(TAG, "width height " +width + height);
                                    for (int j = width/4; j < width*3/4; j++) {
                                        for (int k = height/3; k < height*2/3; k++){
                                            Pix.get(0).add(gryPic.getPixel(j,k));
                                        }
                                    }
                                    Log.d(TAG, "done pic "+i+" ph");
                                }





                                //Log.d(TAG, "Array all " + Pix.get(0).get(23) + Pix.get(0));
                            }catch (Exception e){
                                Log.e(TAG, "Convert But: Fail" + e.toString());
                            }

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


        //把影片其中一個PIXEL取出 做方波顯示
        private Bitmap processAndShowGrayscaleVideo(Uri ur1,Long Time) {


                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, ur1);

                String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long duration = Long.parseLong(durationString);
                //Log.d(TAG, "Convert: Video Duration " +duration);
                Bitmap pic = retriever.getFrameAtTime(Time,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                Bitmap Gpic =convertToGrayScale(pic);
                img.setImageBitmap(Gpic);

               // Log.d(TAG,"pix: " +Pix.get(16));


            return Gpic;
        }

        public Bitmap convertToGrayScale(Bitmap originalBitmap) {
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                //Log.d(TAG,"Orignal" +"width: "+ width +"height: "+height);
                //Pix.add(originalBitmap.getPixel(width/2,height/2));
                Bitmap grayScaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                for (int i = width/4; i < width*3/4; i++) {
                    for (int j = height/3; j < height*2/3; j++) {
                        int pixel = originalBitmap.getPixel(i, j);
                        int grayScale = (int) (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114);
                        grayScaleBitmap.setPixel(i, j, Color.rgb(grayScale, grayScale, grayScale));
                    }
                }
                    return grayScaleBitmap;
    }

       //public void DrawSqu(ArrayList){




        //}


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