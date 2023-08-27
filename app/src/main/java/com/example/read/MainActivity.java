package com.example.read;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
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
import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private VideoView video_view;
    private ImageView img;
    private File outputfile;
    private EditText Frame, ConSecond;
    private TextView txt;
    private Button Playbut, Convr;
    private static final String TAG = "hihihi";
    public long TimeL;
    public int a;
    LineChartData lineChartData;
    LineChart lineChart;
    ArrayList<String> xData = new ArrayList<>();
    ArrayList<Entry> yData = new ArrayList<>();
    Integer[][] PICCONV;
    //Integer[] Length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        video_view = (VideoView) findViewById(R.id.video_view2);
        img = (ImageView) findViewById(R.id.imageView);
        Frame = (EditText) findViewById(R.id.editTextNumber);
        txt = (TextView) findViewById(R.id.textView);
        Playbut = (Button) findViewById(R.id.button);
        Convr = (Button) findViewById(R.id.Convert);
        lineChart = findViewById(R.id.Linechart);
        ConSecond = findViewById(R.id.ConSec);

    }

    @Override
    protected void onActivityResult(int requestCode, int resaultCode, Intent data) {
        super.onActivityResult(requestCode, resaultCode, data);
        if (resaultCode == Activity.RESULT_OK) {
            Uri uri1 = data.getData();

            lineChartData = new LineChartData(lineChart, this);
            video_view.setVideoURI(uri1);
            video_view.setKeepScreenOn(true);
            MediaController controller = new MediaController(this);
            controller.setMediaPlayer(video_view);
            video_view.setMediaController(controller);

            Playbut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Float Time = Float.parseFloat(Frame.getText().toString());
                        Bitmap Pic = processAndShowGrayscaleVideo(uri1, Time);
                        img.setImageBitmap(Pic);
                        Toast.makeText(MainActivity.this, "V.du" + video_view.getDuration(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Doesn't import Frame ");
                    }
                }
            });
            Convr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        a = 0;
                        int Consec = Integer.parseInt(ConSecond.getText().toString());
                        long VDur = video_view.getDuration();
                        VDur = VDur / Consec; //3367/100=3
                        Log.d(TAG, "Convert: Video Duration " + VDur);
                        Integer[][] PICCONV = new Integer[(int) VDur][200000];//VDur =33
                        for (int i = 0; i < VDur; i++) {//VDur = 33
                            Bitmap gryPic = processAndShowGrayscaleVideo(uri1, (float) i);//VDur = 33
                            if (gryPic != null) {
                                int height = gryPic.getHeight();
                                int width = gryPic.getWidth();
                                int z = 0;
                                for (int j = width / 4; j < width * 3 / 4; j++) {
                                    for (int k = height / 3; k < height * 2 / 3; k++) {
                                        PICCONV[i][z] = gryPic.getPixel(j, k);
                                        z++;
                                    }
                                }
                                gryPic.recycle();
                            } else {
                                Log.e(TAG, "Frame miss ");
                                break;
                            }
                            Log.d(TAG, "done pic " + i + " ph");
                        }
                        try {
                            for (int i = 0; i < PICCONV.length; i++) {
                                if (PICCONV[i][0] != null) {
                                    a++;
                                    Log.d(TAG, "a" + a + " i " + i + " pix " + PICCONV[i][0]);
                                } else {
                                    break;
                                }
                            }
                            for (int l = 0; l < a; l++) {
                                Integer DataX = PICCONV[l][500];
                                yData.add(new Entry(l, DataX));
                                String num = String.valueOf(l);
                                xData.add(num);
                                lineChartData.initX(xData);
                                //Integer Max = xData.stream().min(Comparator.co);
                                lineChartData.initY(2000000F,0F);
                                lineChartData.initDataSet(yData);
                                //Log.d(TAG, "Ydata length " + yData + " Xdata length " + xData);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "TO draw error " + e.toString());
                        }
                    } catch (
                            Exception e) {
                        Log.e(TAG, "Convert error " + e.toString());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap processAndShowGrayscaleVideo(Uri ur1, Float Time) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            Time = Time * 1;
            retriever.setDataSource(this, ur1);
            Time = Time * 100000;//讀取影片時間(秒轉微秒)
            TimeL = Time.longValue();
            Bitmap pic = retriever.getFrameAtTime(TimeL, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (pic != null) {
                Bitmap Gpic = convertToGrayScale(pic);
                retriever.release();
                return Gpic;
            } else {
                //Log.d(TAG, "retriever = null" + pic);
            }
            return null;
        } catch (Exception e) {
            Log.d(TAG, "Convert : " + e.toString());
        }


        return null;
    }

    public Bitmap convertToGrayScale(Bitmap originalBitmap) {
        if (originalBitmap != null) {
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            Bitmap grayScaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int i = width / 4; i < width * 3 / 4; i++) {
                for (int j = height / 3; j < height * 2 / 3; j++) {
                    int pixel = originalBitmap.getPixel(i, j);
                    int grayScale = (int) (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114);
                    grayScaleBitmap.setPixel(i, j, Color.rgb(grayScale, grayScale, grayScale));
                }
            }
            return grayScaleBitmap;
        } else {
            return null;
        }
    }

    public void clickRecord(View view) {
        Uri uri1 = getOutputFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri1);
        startActivityForResult(intent, 1);
    }

    private Uri getOutputFile() {

        File medidaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "AnkaranaWork"
        );
        if (!medidaStorageDir.exists()) {
            if (!medidaStorageDir.mkdir()) {
                Log.d("AnkaranaWork", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(medidaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        return Uri.fromFile(mediaFile);
    }
}