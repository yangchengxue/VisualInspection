package com.example.ycx36.visualinspection.Activity_Figure;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ycx36.visualinspection.Fragment_Figure.Fragment_MaskFigure;
import com.example.ycx36.visualinspection.Fragment_Figure.Fragment_ModulFigure;
import com.example.ycx36.visualinspection.Fragment_Figure.Fragment_WrapphaseFigure;
import com.example.ycx36.visualinspection.Fragment_Figure.Fragment_unWrapphaseFigure;
import com.example.ycx36.visualinspection.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import static org.opencv.core.CvType.CV_8UC1;

public class ActivityFigures extends AppCompatActivity {
    String uri1,uri2,uri3,uri4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_figures);
        Fresco.initialize(this);
        Bundle bundle = this.getIntent().getExtras();    //bundle取出之前存在intent的数据
        if (bundle != null){
            uri1 = bundle.getString("uri1");
            uri2 = bundle.getString("uri2");
            uri3 = bundle.getString("uri3");
            uri4 = bundle.getString("uri4");
        }
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("  调制度图  ", Fragment_ModulFigure.class)
                .add("   掩膜图   ", Fragment_MaskFigure.class)
                .add(" 包裹相位图 ", Fragment_WrapphaseFigure.class)
                .add("解包裹相位图", Fragment_unWrapphaseFigure.class)
                .create());

        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);
    }

    /**
     * 将数据传递到fragment
     *
     * */
    public String[] getTitles(){
        return new String[]{uri1,uri2,uri3,uri4};
    }

}
