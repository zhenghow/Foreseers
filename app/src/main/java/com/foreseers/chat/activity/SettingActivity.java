package com.foreseers.chat.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.foreseers.chat.R;
import com.foreseers.chat.global.BaseActivity;
import com.foreseers.chat.view.widget.MyTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.my_titlebar)
    MyTitleBar myTitlebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        myTitlebar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public AppCompatActivity getActivity() {
        return null;
    }

    @Override
    public void initViews() {

    }

    @Override
    public void initDatas() {

    }

    @Override
    public void installListeners() {

    }

    @Override
    public void processHandlerMessage(Message msg) {

    }
}
