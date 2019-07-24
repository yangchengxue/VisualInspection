package com.example.ycx36.visualinspection;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ycx36.visualinspection.Activity_Figure.ActivityFigures;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.wang.avi.AVLoadingIndicatorView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.opencv.core.CvType.CV_8UC1;

public class Activity_Camera extends AppCompatActivity implements SurfaceHolder.Callback,CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.id_graph_sfv) SurfaceView mPrevice;
    @BindView(R.id.drawerlayout) FlowingDrawer mDrawer;
    @BindView(R.id.iv_StripePhoto) ImageView iv_StripePhoto;
    @BindView(R.id.id_reset_camera) ImageButton id_reset_camera; //重置照相机
    @BindView(R.id.ck_continuous) CheckBox ck_continuous;
    @BindView(R.id.avi) AVLoadingIndicatorView avi;
    @BindView(R.id.input_P) EditText input_P;

    private int flag1 = 1;  //音量增加键标志  （为1时可监听按下，为0时不可监听按下）
    private int flag2 = 1;  //音量减小键标志  （为1时可监听按下，为0时不可监听按下）
    private int P = 16;  //默认的P值 （用于计算生成条纹）
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private Bitmap bitmap = null;
    private int cameraPosition = 1; // 0代表前置摄像头，1代表后置摄像头
    private Bitmap bm = null;
    private Camera.Parameters parameters;

    private int y = 0,x = 0;   //手机的分辨率  x:高  y:宽

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity__camera);
        ButterKnife.bind(this);
        mDrawer.openMenu(); //默认打开抽屉

        try{
            initCmaeraFacing();
        }catch (Exception e){
            Toast.makeText(Activity_Camera.this,"相机不可用。",Toast.LENGTH_SHORT).show();
        }

        mHolder = mPrevice.getHolder();
        mHolder.addCallback(this);
        //判断内存卡是否存在的
        checkSoftStage();
        ck_continuous.setOnCheckedChangeListener(this);
        avi.bringToFront();

        //获取手机分辨率
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        y = metrics.widthPixels;
        x = metrics.heightPixels;

    }

    //回调拍照声音的，想要静音拍照的同学，你们懂的！切记不要做坏事儿哦
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };

    /**
     * 相机拍照后对照片的回调
     */
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File tempFile = new File("/sdcard/" + System.currentTimeMillis() + ".png");
            try {
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(data);
                fos.close();
                bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                if (cameraPosition == 1) {
                    matrix.postRotate(90);
                } else {
                    matrix.postRotate(270);
                }
                bitmap = Bitmap.createBitmap(bm, 0, 0, 890, 1024, matrix, true);
                Log.d("asfqbb",""+  bitmap.getWidth() + "       "+bitmap.getHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 检测手机是否存在SD卡,网络连接是否打开
     */
    private void checkSoftStage() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  //判断是否存在SD卡
        } else {
            new AlertDialog.Builder(this).setMessage("检测到手机没有存储卡！请插入手机存储卡再开启本应用。")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
    }
    /**
     * 保存本地照片
     *
     * 路径形式1：/storage/emulated/0/images/1538111284963.png      （新建的文件夹）
     * 路径形式2：/storage/emulated/0/DCIM/Camera/1538111284963.png  (本地图库)
     *
     * @param bmp
     * @return
     */
    public static File saveImage(Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "BarImages"); //保存至自定义的文件夹
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";  //文件名字
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    /**
     * 点击事件
     **/
    public void startCamera() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(shutterCallback, null, mPictureCallback);
                }
            }
        });
    }
    //相机的启动和Activity的生命周期进行绑定
    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera();
            parameters = mCamera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            mCamera.setParameters(parameters);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            if (mHolder != null) {
                try{setStartPrevicw(mCamera, mHolder);}catch (Exception e){
                    Toast.makeText(Activity_Camera.this,"相机不可用。",Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
    //相机的启动和Activity的生命周期进行绑定
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
    /**
     * 打开摄像头
     *
     * @return
     */
    public Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 开始预览相机内容
     */
    private void setStartPrevicw(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            //系统预览角度的调整
            camera.setDisplayOrientation(90);
            //打开摄像头
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            //释放摄像头
            releaseCamera();
        }
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    //在开始的时候创建画面显示的东西
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPrevicw(mCamera, mHolder);
    }
    //当屏幕发生变化时候要做的事儿
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        setStartPrevicw(mCamera, mHolder);
    }
    //当界面销毁的时候要处理的事儿
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    /**初始化为前置摄像头*/
    public void initCmaeraFacing() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                //代表摄像头的方位，CAMERA_FACING_FRONT前置 CAMERA_FACING_BACK后置
//                mCamera.stopPreview();//停掉原来摄像头的预览
//                mCamera.release();//释放资源
                mCamera = null;//取消原来摄像头
                mCamera = Camera.open(i);//打开当前选中的摄像头
                try {
                    mCamera.setPreviewDisplay(mHolder);//通过surfaceview显示取景画面
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setStartPrevicw(mCamera, mHolder);
                cameraPosition = 0;
                break;
            }
        }
    }


    //控件点击事件
    @OnClick({R.id.bt_getP, R.id.RL_1, R.id.RL_2, R.id.RL_3, R.id.RL_4, R.id.RL_5, R.id.RL_6, R.id.RL_7, R.id.id_reset_camera})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_getP:
                String s = input_P.getText().toString();
                if (!s.equals("")) {
                    P = Integer.parseInt(s);  //P等于输入值
                    Toast.makeText(Activity_Camera.this, "P值已更改为：" + P, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Activity_Camera.this, "输入不能为空。", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.RL_1:
//                iv_StripePhoto.setImageBitmap(getStripePhoto(1920,1080,16,0,-457));  //本手机屏幕的像素为1080*1920
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        avi.show();
                        final Bitmap bitmap = getStripePhoto(x,y,P,0,-457);
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                avi.hide();
                                iv_StripePhoto.setImageBitmap(bitmap);
                                Toast.makeText(Activity_Camera.this, "已生成0π条纹", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                break;
            case R.id.RL_2:
//                iv_StripePhoto.setImageBitmap(getStripePhoto(1920,1080,16,Math.PI/2,-457));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        avi.show();
                        final Bitmap bitmap = getStripePhoto(x,y,P,Math.PI/2,-457);
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                avi.hide();
                                iv_StripePhoto.setImageBitmap(bitmap);
                                Toast.makeText(Activity_Camera.this, "已生成1/2π条纹", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                break;
            case R.id.RL_3:
//                iv_StripePhoto.setImageBitmap(getStripePhoto(1920,1080,16,Math.PI,-457));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        avi.show();
                        final Bitmap bitmap = getStripePhoto(x,y,P,Math.PI,-457);
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                avi.hide();
                                iv_StripePhoto.setImageBitmap(bitmap);
                                Toast.makeText(Activity_Camera.this, "已生成π条纹", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                break;
            case R.id.RL_4:
//                iv_StripePhoto.setImageBitmap(getStripePhoto(1920,1080,16,Math.PI*3/2,-457));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        avi.show();
                        final Bitmap bitmap = getStripePhoto(x,y,P,Math.PI*3/2,-457);
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                avi.hide();
                                iv_StripePhoto.setImageBitmap(bitmap);
                                Toast.makeText(Activity_Camera.this, "已生成3π/2条纹", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                break;
            case R.id.RL_5:
                iv_StripePhoto.setImageBitmap(null);
                break;
            case R.id.RL_6: //切换摄像头
                //切换前后摄像头
                int cameraCount = 0;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
                for (int i = 0; i < cameraCount; i++) {
                    Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
                    if (cameraPosition == 1) {
                        //现在是后置，变更为前置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            //代表摄像头的方位，CAMERA_FACING_FRONT前置 CAMERA_FACING_BACK后置
                            mCamera.stopPreview();//停掉原来摄像头的预览
                            mCamera.release();//释放资源
                            mCamera = null;//取消原来摄像头
                            mCamera = Camera.open(i);//打开当前选中的摄像头
                            try {
                                mCamera.setPreviewDisplay(mHolder);//通过surfaceview显示取景画面
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            setStartPrevicw(mCamera, mHolder);
                            cameraPosition = 0;
                            break;
                        }
                    } else {
                        //现在是前置， 变更为后置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            //代表摄像头的方位，CAMERA_FACING_FRONT前置 CAMERA_FACING_BACK后置
                            mCamera.stopPreview();//停掉原来摄像头的预览
                            mCamera.release();//释放资源
                            mCamera = null;//取消原来摄像头
                            mCamera = Camera.open(i);//打开当前选中的摄像头
                            try {
                                mCamera.setPreviewDisplay(mHolder);//通过surfaceview显示取景画面
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            setStartPrevicw(mCamera, mHolder);
                            cameraPosition = 1;
                            break;
                        }
                    }
                }
                break;
            case R.id.RL_7: //四连拍模式

                break;
            case R.id.id_reset_camera:
                mCamera.startPreview();
                bm.recycle();
                bitmap.recycle();
                id_reset_camera.setVisibility(View.GONE);
                break;
        }
    }

    /**监听手机音量键
     *
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN: //音量减小键
                if (flag2 == 1) {
                    flag2 = 0;
                    if (bitmap != null){
                        updatePhotoMedia(saveImage(bitmap),Activity_Camera.this);
                        //保存后重启相机
                        mCamera.startPreview();
                        bm.recycle();
                        bitmap.recycle();
                        id_reset_camera.setVisibility(View.GONE);
                    }else{
                        Toast.makeText(Activity_Camera.this,"音量上键为拍照，音量下键为保存，请先拍照。",Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:  //音量增加键
                //单拍
                if (flag1 == 1 && !ck_continuous.isChecked()){
                    flag1 = 0;
                    flag2 = 1;
                    startCamera();//开始拍照
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                                flag1 = 1;
                                runOnUiThread(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        iv_StripePhoto.setImageBitmap(null);
                                        id_reset_camera.setVisibility(View.VISIBLE);
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                if (ck_continuous.isChecked()) { //如果开启连拍模式
                    TakeAndSave();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    Bitmap bitmap1,bitmap2,bitmap3,bitmap4;
    Uri uri1,uri2,uri3,uri4;
    /**拍照并保存本地
     *四连拍
     * */
    public void TakeAndSave(){
        startCamera();//开始拍照------第一张
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000); //线程睡眠某段时间后保存照片并重启相机
                    uri1 = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
                    mCamera.startPreview();
                    bitmap2 = getStripePhoto(x,y,P,Math.PI/2,-457);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_StripePhoto.setImageBitmap(bitmap2);
                        }
                    });
                    startCamera();//开始拍照 ------第二张
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000); //线程睡眠某段时间后保存照片并重启相机
//                                bitmap2 = bitmap;
                                uri2 = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
                                mCamera.startPreview();
                                bitmap3 = getStripePhoto(x,y,P,Math.PI,-457);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        iv_StripePhoto.setImageBitmap(bitmap3);
                                    }
                                });
                                startCamera();//开始拍照------第三张
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(2000); //线程睡眠某段时间后保存照片并重启相机
//                                            bitmap3 = bitmap;
                                            uri3 = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
                                            mCamera.startPreview();
                                            bitmap4 = getStripePhoto(x,y,P,Math.PI*3/2,-457);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    iv_StripePhoto.setImageBitmap(bitmap4);
                                                }
                                            });
                                            startCamera();//开始拍照------第四张
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Thread.sleep(2000); //线程睡眠某段时间后保存照片并重启相机
//                                                        bitmap4 = bitmap;
                                                        uri4 = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
                                                        mCamera.startPreview();
                                                        runOnUiThread(new Runnable() {
                                                            @SuppressLint("SetTextI18n")
                                                            @Override
                                                            public void run() {
                                                                bitmap1.recycle();
                                                                bitmap2.recycle();
                                                                bitmap3.recycle();
                                                                bitmap4.recycle();
                                                                bm.recycle();
                                                                bitmap.recycle();
                                                                finish();
                                                                Intent intentx = new Intent(Activity_Camera.this, ActivityFigures.class);
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("uri1", uri1.toString());  //写入数据
                                                                bundle.putString("uri2", uri2.toString());  //写入数据
                                                                bundle.putString("uri3", uri3.toString());  //写入数据
                                                                bundle.putString("uri4", uri4.toString());  //写入数据
                                                                intentx.putExtras(bundle);
                                                                startActivity(intentx);
                                                            }
                                                        });
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     *
     * 更新图库
     * 解决图库更新延迟问题*/
    private static void updatePhotoMedia(File file ,Context context){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        context.sendBroadcast(intent);
    }

    /**
     * 生成条纹图
     * row 行数 916
     * list 列数  1280
     * p 分母
     * phase 相位
     * RowStartinValue 行起始值   -457
     * *
     */
    public Bitmap getStripePhoto(int row, int list, int p, double phase, int RowStartinValue) {
        int[] stripePixel = getStripePixel(row, list, p, phase, RowStartinValue);
        Mat frameRf1 = new Mat(row, list, CV_8UC1, new Scalar(0));
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < list; ++j) {
                frameRf1.put(i, j, stripePixel[i * list + j]);
            }
        }
        final Bitmap bmp = Bitmap.createBitmap(frameRf1.width(), frameRf1.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameRf1, bmp);
        return bmp;
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


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == ck_continuous && isChecked) {
            ck_continuous.setChecked(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    avi.show();
                    bitmap1 = getStripePhoto(x,y,P,0,-457);
                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            avi.hide();
                            iv_StripePhoto.setImageBitmap(bitmap1);
                            Toast.makeText(Activity_Camera.this, "已切换至四连拍模式", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }
        if (buttonView == ck_continuous && !isChecked) {
            ck_continuous.setChecked(false);
            iv_StripePhoto.setImageBitmap(null);
            Toast.makeText(this, "已关闭四连拍模式", Toast.LENGTH_SHORT).show();
        }
    }
}
