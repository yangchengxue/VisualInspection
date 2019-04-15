package com.example.ycx36.visualinspection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Activity_ShowModul extends AppCompatActivity {

    @BindView(R.id.iv_showModul) ImageView iv_showModul;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__show_modul);
        ButterKnife.bind(this);
    }
}
