package com.wudongdong.studybutterknife;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wudongdong.butterknife.ButterKnife;
import com.wudongdong.butterknife.Unbinder;
import com.wudongdong.butterknife_annotations.BindView;


public class MainActivity extends AppCompatActivity {

    /*@BindView(R.id.butter_knife_tv)
    TextView mButterKnifeTv;
    private Unbinder mUnbinder;*/

    private Unbinder mUnbinder;

    @BindView(R.id.butter_knife_tv)
    TextView mButterKnifeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUnbinder = ButterKnife.bind(this);

        /*mUnbinder = ButterKnife.bind(this);

        mButterKnifeTv.setText("我是绑定后，设置的butterKnifeTv");*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
