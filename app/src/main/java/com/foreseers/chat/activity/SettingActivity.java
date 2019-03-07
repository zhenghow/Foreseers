package com.foreseers.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.foreseers.chat.R;
import com.foreseers.chat.global.BaseActivity;
import com.foreseers.chat.view.widget.MyTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.my_titlebar)
    MyTitleBar myTitlebar;
    @BindView(R.id.layout_black)
    LinearLayout layoutBlack;
    @BindView(R.id.layout_about)
    LinearLayout layoutAbout;
    @BindView(R.id.button_out)
    Button buttonOut;
    private Intent intent;

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
        return this;
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

    @OnClick({R.id.layout_black, R.id.layout_about, R.id.button_out, R.id.layout_opinion})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_black:
                intent = new Intent(this, BlackListActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_about://关于我们
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);

                break;
            case R.id.layout_opinion://意见与反馈
                intent = new Intent(this, OpinionActivity.class);
                startActivity(intent);
                break;
            case R.id.button_out:
                break;
        }
    }
}
