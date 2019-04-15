package com.example.ycx36.visualinspection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ycx36.visualinspection.Activity_Figure.ActivityFigures;
import com.example.ycx36.visualinspection.util.Transformation;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.opencv.core.CvType.CV_8UC1;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.imageView1) ImageView imageView1;
    @BindView(R.id.imageView2) ImageView imageView2;
    @BindView(R.id.imageView3) ImageView imageView3;
    @BindView(R.id.imageView4) ImageView imageView4;
    private int flag1 = 0, flag2 = 0, flag3 = 0, flag4 = 0;
    Bitmap bitmap1, bitmap2, bitmap3, bitmap4;
    private Uri uri1,uri2,uri3,uri4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        staticLoadCVLibraries();
        ButterKnife.bind(this);
        requestPermissions();

//        double[] value = new double[]{1,2,3,4,5,6,7,8,9};
//        Mat mat1 = new Mat(3,3, CvType.CV_32FC1);
//        Mat mat2 = new Mat(3,3, CvType.CV_32FC1);
//        mat1.put(0,0,value);
//        Core.idct(mat1,mat2);
//        double[] bb = mat1.get(1,1);
//        Log.d("xxxxx.......1","  "+bb[0]);
//        Log.d("xxxxx.......2","  "+mat2.get(0,1)[0]);


    }


    //控件点击事件
    @SuppressLint("SetTextI18n")
    @OnClick({R.id.add_photo1, R.id.add_photo2, R.id.add_photo3, R.id.add_photo4, R.id.rgb2greybtn, R.id.bt2, R.id.showPhoto})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_photo1:
                Intent intentToPickPic1 = new Intent(Intent.ACTION_PICK, null);
                intentToPickPic1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/png");
                startActivityForResult(intentToPickPic1, 1);
                break;
            case R.id.add_photo2:
                Intent intentToPickPic2 = new Intent(Intent.ACTION_PICK, null);
                intentToPickPic2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/png");
                startActivityForResult(intentToPickPic2, 2);
                break;
            case R.id.add_photo3:
                Intent intentToPickPic3 = new Intent(Intent.ACTION_PICK, null);
                intentToPickPic3.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/png");
                startActivityForResult(intentToPickPic3, 3);
                break;
            case R.id.add_photo4:
                Intent intentToPickPic4 = new Intent(Intent.ACTION_PICK, null);
                intentToPickPic4.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/png");
                startActivityForResult(intentToPickPic4, 4);
                break;
            case R.id.rgb2greybtn:  //获取调制度（此按钮暂时不用）
                if (flag1 == 1 && flag2 == 1 && flag3 == 1 && flag4 == 1) {
                    //
                } else {
                    Toast.makeText(MainActivity.this, "请添加图片", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt2:  //打开摄像头
                Intent intent = new Intent(MainActivity.this, Activity_Camera.class);
                startActivity(intent);
                break;
            case R.id.showPhoto:  //查看结果图
                if (flag1 == 1 && flag2 == 1 && flag3 == 1 && flag4 == 1) {
                    Intent intentx = new Intent(MainActivity.this, ActivityFigures.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("uri1", uri1.toString());  //写入数据
                    bundle.putString("uri2", uri2.toString());  //写入数据
                    bundle.putString("uri3", uri3.toString());  //写入数据
                    bundle.putString("uri4", uri4.toString());  //写入数据
                    intentx.putExtras(bundle);
                    startActivity(intentx);
                } else {
                    Toast.makeText(MainActivity.this, "请添加图片", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }



    /**
     * 当点击某张照片完成时会回调到onActivityResult 在这里处理照片
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    try {
                        //该uri是上一个Activity返回的
                        uri1 = data.getData();
                        if (uri1 != null) {
                            bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri1);
                            imageView1.setImageURI(uri1);
                            flag1 = 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        //该uri是上一个Activity返回的
                        uri2 = data.getData();
                        if (uri2 != null) {
                            bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri2);
                            imageView2.setImageURI(uri2);
                            flag2 = 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    try {
                        //该uri是上一个Activity返回的
                        uri3 = data.getData();
                        if (uri3 != null) {
                            bitmap3 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri3);
                            imageView3.setImageURI(uri3);
                            flag3 = 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    try {
                        //该uri是上一个Activity返回的
                        uri4 = data.getData();
                        if (uri4 != null) {
                            bitmap4 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri4);
                            imageView4.setImageURI(uri4);
                            flag4 = 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    /**
     * 保存调制度数组至本地
     */
    public void saveModul(double[] numberArray) {
        JSONArray numbers = new JSONArray();
        for (double number : numberArray) {
            try {
                numbers.put(number);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String jsonString = numbers.toString();
        FileOutputStream fileOut = null;
        OutputStreamWriter outStream = null;
        try {
            fileOut = new FileOutputStream("/storage/emulated/0/modulationDepth.txt", false);
            outStream = new OutputStreamWriter(fileOut);
            outStream.write(jsonString);
        } catch (Exception e) {
        } finally {
            try {
                if (null != outStream)
                    outStream.close();
                if (null != fileOut)
                    fileOut.close();
            } catch (Exception e) {
            }
        }
    }

    private void requestPermissions(){

        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }

        if (!permissionList.isEmpty()){  //申请的集合不为空时，表示有需要申请的权限
            ActivityCompat.requestPermissions(this,permissionList.toArray(new String[permissionList.size()]),1);
        }else { //所有的权限都已经授权过了

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1://获取多个权限
                if (grantResults.length > 0){ //安全写法，如果小于0，肯定会出错了
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED){ //如果权限被拒绝
                            String s = permissions[i];
                            Toast.makeText(this,s+"权限被拒绝了",Toast.LENGTH_SHORT).show();
                        }else{ //授权成功了
                            //do Something
                        }
                    }
                }
                break;
            default:
                break;
        }
    }



    //OpenCV库静态加载并初始化
    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
    }

}
