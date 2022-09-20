package com.example.ex.DashBoard;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.utils.Easing;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Picture;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ex.MainActivity.MainActivity;
import com.example.ex.R;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import at.grabner.circleprogress.CircleProgressView;
import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;

public class DashBoard extends AppCompatActivity {

    private static final int MAX_X_VALUE = 4;
    private static final float MAX_Y_VALUE = 4;
    private static final float MIN_Y_VALUE = 1;
    private static final String SET_LABEL = "dash";
    public PieChart pieChart;
    String TAG = "DashBoard_Activity";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        //-------------------
        Toolbar toolbar = findViewById(R.id.DashBoard_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false); // title 가시 여부
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //-------------------

        PieView pieView = findViewById(R.id.pieView);
        pieView.setPercentageBackgroundColor(R.color.purple_200);

        pieView.setPieInnerPadding(30);

        // Update the visibility of the widget text
        pieView.setInnerTextVisibility(View.VISIBLE); // 안에 텍스트 보여주기
        pieView.setPercentage(33);
        pieView.setInnerText("속도"); // 안에 텍스트 변경
        pieView.setPercentageTextSize(35); //안에 텍스트 사이즈



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 뒤로가기 버튼 만들고 누르면 작동하는 함수..
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    } // 뒤로가기 버튼 만들고 누르면 작동하는 함수

    @Override
    public void onResume(){
        super.onResume();

    }

    public void onDestroy(){
        super.onDestroy();
    }
}