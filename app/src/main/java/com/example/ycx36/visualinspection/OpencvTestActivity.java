package com.example.ycx36.visualinspection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.opencv.core.CvType.CV_8UC1;


public class OpencvTestActivity extends AppCompatActivity {

    @BindView(R.id.select_btn) Button select_btn;
    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.tv1)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv_test);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.select_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_btn:
//                getStripePhoto(916, 1280, 8, Math.PI/2,-457);

                break;
        }
    }

    int count = -1;
//    int flag = 1;
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_VOLUME_DOWN: //音量减小键
//                if (flag == 1){
//                flag = 0;
//                }
//                return true;
//            case KeyEvent.KEYCODE_VOLUME_UP:  //音量增加键
//
//                return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }



    /**
     * 生成条纹图
     * row 行数 916
     * list 列数  1280
     * p 分母
     * phase 相位
     * RowStartinValue 行起始值   -457
     * *
     */
    public void getStripePhoto(int row, int list, int p, double phase, int RowStartinValue) {
        int[] stripePixel = getStripePixel(row, list, p, phase, RowStartinValue);
        Mat frameRf1 = new Mat(row, list, CV_8UC1, new Scalar(0));
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < list; ++j) {
                frameRf1.put(i, j, stripePixel[i * list + j]);
            }
        }
        final Bitmap bmp = Bitmap.createBitmap(frameRf1.width(), frameRf1.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameRf1, bmp);

        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                imageView.setImageBitmap(bmp);
                imageView.invalidate();
            }
        });
    }

    /**
     * 得到条纹图片的像素点
     * row 行数
     * list 列数
     * p 分母
     * phase 相位
     * RowStartinValue 行起始值
     * *
     */
    public int[] getStripePixel(int row, int list, int p, double phase, int RowStartinValue) {
        int[] sp = new int[row * list];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < list; j++) {
                sp[i * list + j] = (int) Math.round((255 * (1 - Math.cos((2 * Math.PI * (RowStartinValue + i) / p) + phase))) / 2);   //第一行的起始值
            }
        }
        return sp;
    }
}
