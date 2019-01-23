package chat.foreseers.com.foreseers.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import com.hyphenate.chat.EMClient;
import com.hyphenate.util.EasyUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import chat.foreseers.com.foreseers.R;

public class SplashActivity extends AppCompatActivity {
    @BindView(R.id.layout_Splash)
    LinearLayout layoutSplash;
    private String facebookid;
    private String huanXinId;
    private static final int sleepTime = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = View.inflate(this, R.layout.activity_splash, null);
        setContentView(view);
        ButterKnife.bind(this);

        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(1500);
        layoutSplash.startAnimation(animation);


        isFirst();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (EMClient.getInstance().isLoggedInBefore()) {
                    EMClient.getInstance().chatManager().loadAllConversations();
                    EMClient.getInstance().groupManager().loadAllGroups();
                    long start = System.currentTimeMillis();
                    long costTime = System.currentTimeMillis() - start;
                    if (sleepTime - costTime > 0) {
                        try {
                            Thread.sleep(sleepTime - costTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    startActivity(new Intent(SplashActivity.this, MainActivity.class));

                    finish();
                } else {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }


            }
        }).start();


    }

    private void isFirst() {
        if (isFirstStart(this)) {// 第一次打开——》登录

            startActivity(new Intent(SplashActivity.this, LoginActivity.class));


        } else {//不是第一次打开——》判断是否登陆过
            getHuanXinLogin();
            if (facebookid == null || huanXinId == "") {//没登陆过
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));

            } else {//登陆过
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

        }

    }

    /**
     * 判断第一次安装
     */

    public boolean isFirstStart(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                "SHARE_APP_TAG", 0);
        Boolean isFirst = preferences.getBoolean("FIRSTStart", true);
        if (isFirst) {// 第一次
            preferences.edit().putBoolean("FIRSTStart", false).commit();

            Log.i("GFA", "一次");
            return true;
        } else {
            Log.i("GFA", "N次");
            return false;
        }
    }

    /**
     * 获取环信登录id
     */

    public void getHuanXinLogin() {
        SharedPreferences userInfo = getSharedPreferences("loginToken", MODE_PRIVATE);

        huanXinId = userInfo.getString("huanXinId", "");


        Log.i("huanXinId", "isLogin: " + userInfo.getString("huanXinId", ""));
    }
}
