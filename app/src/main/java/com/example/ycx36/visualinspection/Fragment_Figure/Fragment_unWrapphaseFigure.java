package com.example.ycx36.visualinspection.Fragment_Figure;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.FileNotFoundException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;

import static org.opencv.core.CvType.CV_8UC1;

public class Fragment_unWrapphaseFigure extends Fragment {

    @BindView(R.id.photo_drawee_view) PhotoDraweeView mPhotoDraweeView;
    @BindView(R.id.avi) AVLoadingIndicatorView avi;
    private View view;
    String[] paths;
    Bitmap bitmap1, bitmap2, bitmap3, bitmap4;
    private int sumOfAllPixels = (int) Double.POSITIVE_INFINITY;  //像素总和，初始值定义为无穷大
    int xwidth = 0, xhigh = 0; //四张图片中的像素点最少的图片的宽度和长度 ！！（xwidth是行  xhigh是列）
    private double min = Double.POSITIVE_INFINITY;  //调制度的最小值，初始值定义为无穷大 （不断比较最终将获取到最小值）
    private double max = 0;    //调制度的最大值 （不断比较最终将获取到最大值）
    private double min_UnWrapphase1 = Double.POSITIVE_INFINITY;
    private double max_UnWrapphase1 = 0;
    double[] UnWrapphase1;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.layout_unwrapphase, container, false);
            ButterKnife.bind(this, view);
            Fresco.initialize(Objects.requireNonNull(getActivity()));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    UnWrapphase1 = getUnWrapphase(bitmap1, bitmap2, bitmap3, bitmap4);
                    // temp=255*(1-(unwrapphi1-min(unwrapphi1(:)))/(max(unwrapphi1(:))-min(unwrapphi1(:))));
                    try{
                        //showWrapphaseFigure(xwidth, xhigh, Wrapphase1); //显示
                        showUnWrapphaseFigure(xwidth, xhigh, UnWrapphase1);
                    }catch (Exception e){
                        //此时导致的异常最有可能是 在未加载完成时强行关闭activity。
                    }
                }
            }).start();

        }
        return view;
    }

    /**
     * 显示解包裹相位图片并且自动保存图片到本地
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void showUnWrapphaseFigure(int x, int y, double[] unWrapphase1) {
        int[] d = getGaryCS2(unWrapphase1);   //主要耗时操作
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
    public double[] getUnWrapphase(Bitmap bitmap1, Bitmap bitmap2, Bitmap bitmap3, Bitmap bitmap4) {
        int[] k1 = GetGreyArray(bitmap1);
        int[] k2 = GetGreyArray(bitmap2);
        int[] k3 = GetGreyArray(bitmap3);
        int[] k4 = GetGreyArray(bitmap4);
        int leng = k1.length;
//        showResponse3(leng); //显示像素点总数在UI界面中；
        double[] Wrapphasex = new double[leng];
        double[] cosWrapphasex = new double[leng];
        double[] sinWrapphasex = new double[leng];
        double[] x = new double[leng];
        double[] y = new double[leng];
        double[] x2y2 = new double[leng];
        double[] unwrapphi1 = new double[xhigh * xwidth];
        try {
            for (int n = 0; n < leng; n++) {
//                Wrapphase[n] = 2 * Math.sqrt(Math.pow((k4[n] - k2[n]), 2) + Math.pow((k1[n] - k3[n]), 2)) / (k1[n] + k2[n] + k3[n] + k4[n]);
                Wrapphasex[n] = Math.atan2(k4[n] - k2[n], k1[n] - k3[n]);
                if (Wrapphasex[n] < 0) {
                    Wrapphasex[n] = Wrapphasex[n] + 2 * Math.PI;
                }
                cosWrapphasex[n] = Math.cos(Wrapphasex[n]); //cos(w)
                sinWrapphasex[n] = Math.sin(Wrapphasex[n]);  //sin(w)

                if (min > Wrapphasex[n]) {
                    min = Wrapphasex[n];
                }
                if (max < Wrapphasex[n]) {
                    max = Wrapphasex[n];
                }
            }

            //计算x矩阵和y矩阵
            for (int i = 0; i < xwidth; i++) {        //行
                for (int j = 0; j < xhigh; j++) { //列
                    x[xhigh * i + j] = j + 1;
                    y[xhigh * i + j] = i + 1;
                    //计算x^2+y^2,并存于x2y2数组。
                    x2y2[xhigh * i + j] = x[xhigh * i + j] * x[xhigh * i + j] + y[xhigh * i + j] * y[xhigh * i + j];
                }
            }
            //计算dct2(sin(wrapphase1)),结果输出到sinMat矩阵
            Mat mat1 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat sinMat = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            mat1.put(0, 0, sinWrapphasex);
            Core.dct(mat1, sinMat);

            //计算dct2(cos(wrapphase1)),结果输出到cosMat矩阵
            Mat mat11 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat cosMat = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            mat11.put(0, 0, cosWrapphasex);
            Core.dct(mat11, cosMat);

            //计算(x.*x+y.*y).*dct2(sin(wrapphase1)，结果存入sinMAT数组中
            //计算(x.*x+y.*y).*dct2(cos(wrapphase1)，结果存入cosMAT数组中
            double[] sinMAT = new double[xhigh * xwidth];
            double[] cosMAT = new double[xhigh * xwidth];
            for (int a = 0; a < xwidth; a++) {
                for (int b = 0; b < xhigh; b++) {
                    sinMAT[xhigh * a + b] = sinMat.get(a, b)[0] * x2y2[xhigh * a + b];
                    cosMAT[xhigh * a + b] = cosMat.get(a, b)[0] * x2y2[xhigh * a + b];
                }
            }

            //计算idct2((x.*x+y.*y).*dct2(sin(wrapphase1))),结果存入Mat_t1矩阵
            //计算idct2((x.*x+y.*y).*dct2(cos(wrapphase1))),结果存入Mat_t11矩阵
            Mat mat2 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat mat22 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat Mat_t1 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat Mat_t11 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            mat2.put(0, 0, sinMAT);
            mat22.put(0, 0, cosMAT);
            Core.idct(mat2, Mat_t1);
            Core.idct(mat22, Mat_t11);

            //计算cos(wrapphase1).*idct2((x.*x+y.*y).*dct2(sin(wrapphase1)))，结果存入xxx1数组中
            //计算sin(wrapphase1).*idct2((x.*x+y.*y).*dct2(cos(wrapphase1)))，结果存入xxx11数组中
            double[] xxx1 = new double[xhigh * xwidth];
            double[] xxx11 = new double[xhigh * xwidth];
            for (int a = 0; a < xwidth; a++) {
                for (int b = 0; b < xhigh; b++) {
                    xxx1[xhigh * a + b] = Mat_t1.get(a, b)[0] * cosWrapphasex[xhigh * a + b];
                    xxx11[xhigh * a + b] = Mat_t11.get(a, b)[0] * sinWrapphasex[xhigh * a + b];
                }
            }

            //计算dct2(xxx1),结果存入Mat_t2矩阵
            //计算dct2(xxx11),结果存入Mat_t22矩阵
            Mat mat3 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat mat33 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat Mat_t2 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat Mat_t22 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            mat3.put(0, 0, xxx1);
            mat33.put(0, 0, xxx11);
            Core.dct(mat3, Mat_t2);
            Core.dct(mat33, Mat_t22);

            //计算idct2(xxx1)/(x^2+y^2)，结果存入xxx2数组中
            //计算idct2(xxx11)/(x^2+y^2)，结果存入xxx22数组中
            double[] xxx2 = new double[xhigh * xwidth];
            double[] xxx22 = new double[xhigh * xwidth];
            for (int a = 0; a < xwidth; a++) {
                for (int b = 0; b < xhigh; b++) {
                    xxx2[xhigh * a + b] = Mat_t2.get(a, b)[0] / x2y2[xhigh * a + b];
                    xxx22[xhigh * a + b] = Mat_t22.get(a, b)[0] / x2y2[xhigh * a + b];
                }
            }

            //计算idct2(xxx2),结果存入Mat_t3矩阵
            //计算idct2(xxx22),结果存入Mat_t33矩阵
            Mat mat4 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat mat44 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat Mat_t3 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            Mat Mat_t33 = new Mat(xwidth, xhigh, CvType.CV_32FC1);
            mat4.put(0, 0, xxx2);
            mat44.put(0, 0, xxx22);
            Core.idct(mat4, Mat_t3);
            Core.idct(mat44, Mat_t33);

            //将dict2(xxx2)转为double[] 类型，结果存入xxx3数组中
            //将dict2(xxx22)转为double[] 类型，结果存入xxx33数组中
            //计算相位解包裹值
            double[] xxx3 = new double[xhigh * xwidth];
            double[] xxx33 = new double[xhigh * xwidth];
            for (int a = 0; a < xwidth; a++) {
                for (int b = 0; b < xhigh; b++) {
                    xxx3[xhigh * a + b] = Mat_t3.get(a, b)[0];
                    xxx33[xhigh * a + b] = Mat_t33.get(a, b)[0];
                    unwrapphi1[xhigh * a + b] = xxx3[xhigh * a + b] - xxx33[xhigh * a + b];
                    if (min_UnWrapphase1 > unwrapphi1[xhigh * a + b]) {
                        min_UnWrapphase1 = unwrapphi1[xhigh * a + b];
                    }
                    if (max_UnWrapphase1 < unwrapphi1[xhigh * a + b]) {
                        max_UnWrapphase1 = unwrapphi1[xhigh * a + b];
                    }

                }
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "图片格式错误", Toast.LENGTH_SHORT).show();
        }

        return unwrapphi1;
    }

    /**
     * 获取图片的灰度值
     * 返回一个数组
     * 能准确获取灰度值——不显示图片
     */
    @SuppressLint("SetTextI18n")
    public int[] GetGreyArray(Bitmap img) {
        int width = img.getWidth();            //获取位图的宽(列)
        int height = img.getHeight();        //获取位图的高（行）
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
    public int[] getGaryCS2(double[] UnWrapphase1) {
        int[] M = new int[xhigh * xwidth];
        double dd = max_UnWrapphase1 - min_UnWrapphase1;
        for (int q = 0; q < xhigh * xwidth; q++) {
            M[q] = getUint8(255 * (1 - (UnWrapphase1[q] - min_UnWrapphase1) / dd));
        }
        return M;
    }

    /**
     *用于获取uint8数据，大于255的数全部强制置为255，而小于255的部分则保持原样不变并四舍五入转化为整型。
     * */
    public int getUint8(double s){
        int result;
        if (s>=255){
            result = 255;
        }else result = (int)Math.round(s);
        return result;
    }
}
