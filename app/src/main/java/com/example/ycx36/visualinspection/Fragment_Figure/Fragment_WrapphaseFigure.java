package com.example.ycx36.visualinspection.Fragment_Figure;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ycx36.visualinspection.Activity_Figure.ActivityFigures;
import com.example.ycx36.visualinspection.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.wang.avi.AVLoadingIndicatorView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.FileNotFoundException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.photodraweeview.PhotoDraweeView;

import static org.opencv.core.CvType.CV_8UC1;

public class Fragment_WrapphaseFigure extends Fragment {
    private View view;
    private int sumOfAllPixels = (int) Double.POSITIVE_INFINITY;  //像素总和，初始值定义为无穷大
    private int xwidth = 0, xhigh = 0; //四张图片中的像素点最少的图片的宽度和长度
    private double min = Double.POSITIVE_INFINITY;  //调制度的最小值，初始值定义为无穷大 （不断比较最终将获取到最小值）
    private double max = 0;    //调制度的最大值 （不断比较最终将获取到最大值）
    Bitmap bitmap1, bitmap2, bitmap3, bitmap4;
    double[] Wrapphase1;
    String[] paths;

    @BindView(R.id.photo_drawee_view) PhotoDraweeView mPhotoDraweeView;
    @BindView(R.id.avi) AVLoadingIndicatorView avi;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.layout_wrapphase, container, false);
            ButterKnife.bind(this, view);
            Fresco.initialize(Objects.requireNonNull(getActivity()));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Wrapphase1 = getWrapphase(bitmap1, bitmap2, bitmap3, bitmap4);
                    try{
                    showWrapphaseFigure(xwidth, xhigh, Wrapphase1); //显示
                    }catch (Exception e){
                        //此时导致的异常最有可能是 在未加载完成时强行关闭activity。
                    }
                }
            }).start();

        }
        return view;
    }

    /**
     * 得到宿主activity传递过来的图片路径数据
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        paths = ((ActivityFigures) activity).getTitles();//通过强转成宿主activity，就可以获取到传递过来的数据
        if (paths[0] != null) {
            bitmap1 = decodeUriAsBitmap(Uri.parse(paths[0]));
        }
        if (paths[1] != null) {
            bitmap2 = decodeUriAsBitmap(Uri.parse(paths[1]));
        }
        if (paths[2] != null) {
            bitmap3 = decodeUriAsBitmap(Uri.parse(paths[2]));
        }
        if (paths[3] != null) {
            bitmap4 = decodeUriAsBitmap(Uri.parse(paths[3]));
        }
    }

    /**
     * @param uri：图片的本地url地址
     * @return Bitmap；
     */
    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(Objects.requireNonNull(getActivity()).getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }


    /**
     * 计算包裹相位数据
     * 返回一个双精度数组
     */
    public double[] getWrapphase(Bitmap bitmap1, Bitmap bitmap2, Bitmap bitmap3, Bitmap bitmap4) {
        int[] k1 = GetGreyArray(bitmap1);
        int[] k2 = GetGreyArray(bitmap2);
        int[] k3 = GetGreyArray(bitmap3);
        int[] k4 = GetGreyArray(bitmap4);
        int leng = k1.length;
//        showResponse3(leng); //显示像素点总数在UI界面中；
        double[] Wrapphase = new double[leng];
        try {
            for (int n = 0; n < leng; n++) {
//                Wrapphase[n] = 2 * Math.sqrt(Math.pow((k4[n] - k2[n]), 2) + Math.pow((k1[n] - k3[n]), 2)) / (k1[n] + k2[n] + k3[n] + k4[n]);
                Wrapphase[n] = Math.atan2(k4[n] - k2[n], k1[n] - k3[n]);
                if (Wrapphase[n] < 0) {
                    Wrapphase[n] = Wrapphase[n] + 2 * Math.PI;
                }
                if (min > Wrapphase[n]) {
                    min = Wrapphase[n];
                }
                if (max < Wrapphase[n]) {
                    max = Wrapphase[n];
                }
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "图片格式错误", Toast.LENGTH_SHORT).show();
        }


        return Wrapphase;
    }

    /**
     * 显示包裹相位图片并且自动保存图片到本地
     */
    public void showWrapphaseFigure(int x, int y, double[] Wrapphase1) {
        double[] d = getGaryCS2(Wrapphase1);   //主要耗时操作
        Mat frameRf1 = new Mat(x, y, CV_8UC1, new Scalar(0));
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                frameRf1.put(i, j, d[i * y + j]);
            }
        }
        final Bitmap bmp = Bitmap.createBitmap(frameRf1.width(), frameRf1.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(frameRf1, bmp);

        //将bitmap转为uri，并自动保存至本地
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(Objects.requireNonNull(getActivity()).getContentResolver(), bmp, null,null));
            final PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setUri(uri);
            controller.setOldController(mPhotoDraweeView.getController());
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null || mPhotoDraweeView == null) {
                        return;
                    }
                    mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
            getActivity().runOnUiThread(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    avi.hide();
                    mPhotoDraweeView.setController(controller.build());
                }
            });

    }


    /**
     * 获取图片的灰度值
     * 返回一个数组
     * 能准确获取灰度值——不显示图片
     */
    @SuppressLint("SetTextI18n")
    public int[] GetGreyArray(Bitmap img) {
        int width = img.getWidth();            //获取位图的宽
        int height = img.getHeight();        //获取位图的高
        if (sumOfAllPixels > width * height) {
            sumOfAllPixels = width * height;
            xwidth = height;
            xhigh = width;
        }

        int[] pixels = new int[width * height];    //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) Math.round((double) red * 0.29900 + (double) green * 0.58700 + (double) blue * 0.11400);
//                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;

            }
        }
        return pixels;
    }

    /**
     * 返回一个灰度级双精度数组（算法优化，速度提高）
     */
    public double[] getGaryCS2(double[] Modul) {
        double[] M = new double[xhigh * xwidth];
        for (int q = 0; q < xhigh * xwidth; q++) {
            M[q] = getUint8(Modul[q]*255/2/Math.PI);
        }
        return M;
    }

    /**
     *用于获取uint8数据，大于255的数全部强制置为255，而小于255的部分则保持原样不变并四舍五入转化为整型。
     * */
    public int getUint8(double s){
        int result;
        if (s>255){
            result = 255;
        }else result = (int)Math.round(s);
        return result;
    }

    /**用于获取灰度级别，相当于Matlab的imshow函数（但是算法计算慢）*/
    public double[] getGaryCS(double[] Modul) {
        double range = max - min; //最高减去最低
        double series = range / 255;
        return GetGrayLevel(Modul, series, min, max);
    }

    /**
     * 返回一个灰度级双精度数组（算法慢，耗时间长）
     */
    public double[] GetGrayLevel(double[] Modul, double series, double low, double high) {
        double[] M = Modul;
        for (int q = 0; q < xhigh * xwidth; q++) {
            for (int w = 1; w < 256; w++) {
                if ((M[q] - low) / series == w)
                    M[q] = w;
                else if ((M[q] - low) / series > w && (M[q] - 0.4) / series < w + 1)
                    M[q] = w;
                else if ((M[q] - low) / series < 1 || M[q] == low)
                    M[q] = 0;
                else if (M[q] == high)
                    M[q] = 255;
            }
        }
        return M;
    }
}
