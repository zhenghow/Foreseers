package com.foreseers.chat.activity;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.foreseers.chat.R;
import com.foreseers.chat.adapter.WipeDayHistoryAdapter;
import com.foreseers.chat.bean.LoginBean;
import com.foreseers.chat.bean.WipeDayHistoryBean;
import com.foreseers.chat.decoration.GridSectionAverageGapItemDecoration;
import com.foreseers.chat.global.BaseActivity;
import com.foreseers.chat.util.GetLoginTokenUtil;
import com.foreseers.chat.util.Urls;
import com.foreseers.chat.view.widget.MyTitleBar;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WipeDayHistoryActivity extends BaseActivity {

    @BindView(R.id.my_titlebar)
    MyTitleBar myTitlebar;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    private String datetime;
    private List<WipeDayHistoryBean.DataBean> dataBeans = new ArrayList<>();
    private WipeDayHistoryAdapter wipeDayHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wipe_day_history);
        ButterKnife.bind(this);
        initView();
        initData();
        getDataFromHttp();
    }


    private void initView() {
        myTitlebar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        wipeDayHistoryAdapter = new WipeDayHistoryAdapter(this, dataBeans);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(staggeredGridLayoutManager);
        recyclerview.addItemDecoration(new GridSectionAverageGapItemDecoration(10, 10, 10, 10));
        recyclerview.setAdapter(wipeDayHistoryAdapter);

    }

    private void initData() {
        datetime = getIntent().getStringExtra("datetime");
    }

    private void getDataFromHttp() {
        OkGo.<String>post(Urls.Url_HistoryDay).tag(this)
                .params("userid", GetLoginTokenUtil.getUserId(this))
                .params("datetime", datetime)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        Gson gson = new Gson();
                        LoginBean loginBean = gson.fromJson(response.body(), LoginBean.class);
                        if (loginBean.getStatus().equals("success")) {
                            WipeDayHistoryBean wipeDayHistoryBean = gson.fromJson(response.body(), WipeDayHistoryBean.class);
                            dataBeans = wipeDayHistoryBean.getData();
                            getHandler().obtainMessage(DATASUCCESS).sendToTarget();
                        }
                    }
                });
    }

    private final int DATASUCCESS = 1;
    private final int DATAFELLED = 2;



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
        switch (msg.what) {
            case DATASUCCESS:
                if (recyclerview!=null){
                    wipeDayHistoryAdapter.setNewData(dataBeans);
                }
                break;
        }
    }
}