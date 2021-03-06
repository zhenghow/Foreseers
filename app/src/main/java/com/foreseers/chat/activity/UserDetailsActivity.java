package com.foreseers.chat.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.foreseers.chat.R;
import com.foreseers.chat.bean.ErrBean;
import com.foreseers.chat.bean.InquireFriendBean;
import com.foreseers.chat.bean.LoginBean;
import com.foreseers.chat.bean.UserInforBean;
import com.foreseers.chat.dialog.AddFriendDialog;
import com.foreseers.chat.dialog.AddFriendErrorDialog;
import com.foreseers.chat.dialog.BlackDialog;
import com.foreseers.chat.dialog.DelFriendDialog;
import com.foreseers.chat.dialog.NoFriendNumberDialog;
import com.foreseers.chat.dialog.WipeDialog;
import com.foreseers.chat.global.BaseActivity;
import com.foreseers.chat.global.MyApplication;
import com.foreseers.chat.util.HuanXinHelper;
import com.foreseers.chat.util.PreferenceManager;
import com.foreseers.chat.util.Urls;
import com.foreseers.chat.view.widget.MyTitleBar;
import com.google.gson.Gson;
import com.hmy.popwindow.PopItemAction;
import com.hmy.popwindow.PopWindow;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ms.banner.Banner;
import com.ms.banner.holder.BannerViewHolder;
import com.ms.banner.holder.HolderCreator;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 好友详情
 */
public class UserDetailsActivity extends BaseActivity {

    @BindView(R.id.my_titlebar) MyTitleBar myTitlebar;
    @BindView(R.id.text_user_details_name) TextView textUserDetailsName;
    @BindView(R.id.text_num) TextView textNum;
    @BindView(R.id.progress_text) TextView progressText;
    @BindView(R.id.text_sex) TextView textSex;
    @BindView(R.id.text_age) TextView textAge;
    @BindView(R.id.text_location) TextView textLocation;
    @BindView(R.id.chat_user_details) ImageView chatUserDetails;
    @BindView(R.id.img_add_friend) ImageView imgAddFriend;
    @BindView(R.id.layout_analyze_life_book) LinearLayout layoutAnalyzeLifeBook;

    @BindView(R.id.layout_wipe) LinearLayout layoutWipe;
    @BindView(R.id.text_ziwei) TextView textZiwei;
    @BindView(R.id.text_1) TextView text1;
    @BindView(R.id.text_2) TextView text2;
    @BindView(R.id.text_sign) TextView textSign;
    @BindView(R.id.text_desc) TextView textDesc;
    @BindView(R.id.img_ani) ImageView imgAni;
    @BindView(R.id.layout) LinearLayout layout;
    @BindView(R.id.banner) Banner banner;
    @BindView(R.id.img_vip) ImageView imgVip;
    @BindView(R.id.layout_lifebook) LinearLayout layoutLifebook;

    private NoFriendNumberDialog noFriendNumberDialog;
    private AddFriendDialog addFriendDialog;
    private Intent intent;
    private String userid;
    private Bundle bundle;
    private String username;
    private String sex;
    private int age;
    private int num;
    private int distance;
    private int userscore;
    private AddFriendErrorDialog addFriendErrorDialog;
    private UserInforBean userInforBean;
    private UserInforBean.DataBean dataBean;
    private String avatar;
    private List<String> imgList = new ArrayList<>();
    private int friend;
    private DelFriendDialog delFriendDialog;
    private String ziwei;
    private int thirthday;
    private int sevenday;
    private int lookhead;
    private String sign;
    private String desc;
    private AnimationDrawable animationDrawable;
    private WipeDialog wiped;
    private BlackDialog blackDialog;
    private int numuser;
    private int soundID;
    private SoundPool soundPool;
    private String lifeuserid;
    private int vip;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public void initViews() {
        setContentView(R.layout.activity_user_details);
        ButterKnife.bind(this);

        //设置自动轮播，默认为true
        banner.setAutoPlay(false);

        initSound();
    }

    @Override
    public void initDatas() {
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        userid = bundle.getString("userid");
        numuser = bundle.getInt("numuser");
        position = bundle.getInt("position");
        if (numuser == 0) {
            layout.setVisibility(View.GONE);
        } else if (numuser == 1) {

            layout.setVisibility(View.VISIBLE);
        }

        refresh();

        imgAni.setBackgroundResource(R.drawable.start_show);
        animationDrawable = (AnimationDrawable) imgAni.getBackground();
    }

    @Override
    public void installListeners() {
        myTitlebar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lookhead==1){
                    intent =new Intent();
                    intent.putExtra("position",position);
                    intent.putExtra("userid",userid);
                    setResult(0x002,intent);
                }

                finish();
            }
        });
        myTitlebar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopupWind();
            }
        });

        banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                if (i == 0) {
                    if (lookhead == 1) {
                        layoutWipe.setVisibility(View.GONE);
                    } else if (lookhead == 0) {
                        layoutWipe.setVisibility(View.VISIBLE);
                    }
                } else {

                    layoutWipe.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private final int ANIMATION = 3;

    @OnClick({R.id.img_add_friend, R.id.layout_analyze_life_book, R.id.chat_user_details, R.id.layout_wipe, R.id.layout_lifebook})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_add_friend://添加好友

                if (friend == 1) {
                    HuanXinHelper.getInstance()
                            .isLoggedIn();
                    OkGo.<String>post(Urls.Url_UserFriend).tag(this)
                            .params("facebookid", PreferenceManager.getFaceBookId(this))
                            .params("friendid", userid)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    Gson gson = new Gson();

                                    InquireFriendBean inquireFriendBean = gson.fromJson(response.body(), InquireFriendBean.class);
                                    InquireFriendBean.DataBean inquireDataBean = inquireFriendBean.getData();
                                    if (inquireFriendBean.getStatus()
                                            .equals("success")) {
                                        switch (inquireDataBean.getStatus()) {
                                            case 0://可以添加好友
                                                addFriendDialog = new AddFriendDialog(UserDetailsActivity.this, R.style.MyDialog,
                                                                                      inquireDataBean.getName(), userid, inquireDataBean.getHead(),
                                                                                      inquireDataBean.getUserint(),
                                                                                      new AddFriendDialog.LeaveMyDialogListener() {

                                                                                          @Override
                                                                                          public void onClick(View view) {
                                                                                              addFriendDialog.dismiss();
                                                                                          }
                                                                                      });
                                                addFriendDialog.setCancelable(true);

                                                //修改弹窗位置
                                                changeDialogLocation(addFriendDialog);

                                                addFriendDialog.show();

                                                break;
                                            case 1://自己好友位已满
                                                noFriendNumberDialog = new NoFriendNumberDialog(UserDetailsActivity.this, R.style.MyDialog,
                                                                                                new NoFriendNumberDialog.LeaveMyDialogListener() {
                                                                                                    @Override
                                                                                                    public void onClick(View view) {
                                                                                                        noFriendNumberDialog.dismiss();
                                                                                                    }
                                                                                                });

                                                noFriendNumberDialog.setCancelable(true);

                                                //修改弹窗位置
                                                changeDialogLocation(noFriendNumberDialog);

                                                noFriendNumberDialog.show();
                                                break;

                                            case 2://目标好友位已满

                                                addFriendErrorDialog = new AddFriendErrorDialog(UserDetailsActivity.this, R.style.MyDialog,
                                                                                                new AddFriendErrorDialog.LeaveMyDialogListener() {
                                                                                                    @Override
                                                                                                    public void onClick(View view) {
                                                                                                        addFriendErrorDialog.dismiss();
                                                                                                    }
                                                                                                });
                                                //修改弹窗位置
                                                changeDialogLocation(addFriendErrorDialog);
                                                addFriendErrorDialog.show();

                                                break;
                                        }
                                    }
                                }
                            });
                } else if (friend == 0) {

                    delFriendDialog = new DelFriendDialog(UserDetailsActivity.this, R.style.MyDialog, new DelFriendDialog.LeaveMyDialogListener() {

                        @Override
                        public void onClick(View view) {

                            switch (view.getId()) {
                                case R.id.button_ok:
                                    delFriendDialog.dismiss();
                                    OkGo.<String>post(Urls.Url_DelFriend).tag(this)
                                            .params("userid", PreferenceManager.getUserId(UserDetailsActivity.this))
                                            .params("friendid", userid)
                                            .params("reation", 2)
                                            .execute(new StringCallback() {
                                                @Override
                                                public void onSuccess(Response<String> response) {
                                                    try {
                                                        EMClient.getInstance()
                                                                .contactManager()
                                                                .deleteContact(userid);
                                                    } catch (HyphenateException e) {
                                                        e.printStackTrace();
                                                    }
                                                    refresh();
                                                }
                                            });

                                    break;
                                case R.id.button_cancel:
                                    delFriendDialog.dismiss();
                                    break;
                            }

                            // delFriendDialog
                            // .dismiss();
                        }
                    });
                    delFriendDialog.setCancelable(true);

                    //修改弹窗位置
                    //changeDialogLocation(addFriendDialog);

                    delFriendDialog.show();
                }

                break;

            case R.id.layout_analyze_life_book://我與TA的詳細分析
                if (sevenday == 1) {
                    intent = new Intent(this, UserAnalyzeLifeBookActivity.class);
                    intent.putExtra("userid", userid);
                    startActivity(intent);
                }

                break;
            case R.id.layout_lifebook://TA的命书
                if (thirthday == 1) {
                    intent = new Intent(this, FortunetellingActivity.class);
                    intent.putExtra("name", username);
                    intent.putExtra("lifeuserid", lifeuserid);
                    startActivity(intent);
                }

                break;

            case R.id.chat_user_details://发消息
                intent = new Intent(this, ChatActivity.class);
                bundle = new Bundle();
                bundle.putString(EaseConstant.EXTRA_USER_ID, userid + "");
                bundle.putString("username", username);
                bundle.putString(EaseConstant.EXTRA_USER_AVATAR, avatar);
                intent.putExtras(bundle);

                startActivity(intent);
                break;

            case R.id.layout_wipe://擦照片
                //delFriendDialog.dismiss();
                wiped = new WipeDialog(UserDetailsActivity.this, R.style.MyDialog, new WipeDialog.LeaveMyDialogListener() {

                    @Override
                    public void onClick(View view) {

                        switch (view.getId()) {
                            case R.id.button_ok:
                                wiped.dismiss();
                                OkGo.<String>post(Urls.Url_Wipe).tag(this)
                                        .params("userid", PreferenceManager.getUserId(UserDetailsActivity.this))
                                        .params("caid", userid)
                                        .execute(new StringCallback() {
                                            @Override
                                            public void onSuccess(Response<String> response) {
                                                imgAni.setVisibility(View.VISIBLE);
                                                Gson gson = new Gson();
                                                LoginBean loginBean = gson.fromJson(response.body(), LoginBean.class);
                                                if (loginBean.getStatus()
                                                        .equals("success")) {
                                                    imgList.clear();
                                                    layoutWipe.setVisibility(View.GONE);
                                                    refresh();
                                                    imgAni.setVisibility(View.VISIBLE);
                                                    animationDrawable.start();
                                                    playSound();
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                Thread.sleep(12 * 100);
                                                                getHandler().obtainMessage(ANIMATION)
                                                                        .sendToTarget();
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }).start();
                                                } else if (loginBean.getStatus()
                                                        .equals("fail")) {

                                                    ErrBean errBean = gson.fromJson(response.body(), ErrBean.class);
                                                    if (errBean.getData()
                                                            .getErrCode() == 2000) {
                                                        Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.no_eraser)
                                                                      ,
                                                                       Toast
                                                                .LENGTH_LONG)
                                                                .show();
                                                    }
                                                }
                                            }
                                        });
                                break;
                            case R.id.button_cancel:
                                wiped.dismiss();
                                break;
                        }

                        //                                     delFriendDialog.dismiss();
                    }
                });
                wiped.setCancelable(true);

                //修改弹窗位置
                //                changeDialogLocation(addFriendDialog);

                wiped.show();

                break;
            default:
                break;
        }
    }

    @Override
    public void processHandlerMessage(Message msg) {
        switch (msg.what) {
            case DATASUCCESS:
                if (layoutWipe != null) {
                    switch (lookhead) {
                        case 0://不可以看（模糊头像）
                            layoutWipe.setVisibility(View.VISIBLE);
                            break;
                        case 1://
                            layoutWipe.setVisibility(View.GONE);
                            break;
                    }
                    //设置图片集合
                    //                    banner.update(imgList);
                    //设置图片加载器
                    banner.setPages(imgList, new HolderCreator<BannerViewHolder>() {
                        @Override
                        public BannerViewHolder createViewHolder() {
                            return new CustomViewHolder();
                        }
                    });
                    //banner设置方法全部调用完毕时最后调用
                    banner.start();
                    myTitlebar.setTitle(username);
                    textUserDetailsName.setText(username);
                    switch (sex) {
                        case "F":
                            textSex.setText(R.string.woman);
                            break;
                        case "M":
                            textSex.setText(R.string.man);
                            break;
                        default:
                            break;
                    }

                    switch (vip){
                        case 0:
                            imgVip.setVisibility(View.GONE);
                            break;
                        case 1:
                            imgVip.setVisibility(View.VISIBLE);
                            break;
                    }

                    switch (friend) {
                        case 0://是好友
                            imgAddFriend.setBackgroundResource(R.mipmap.icon_data_03);
                            break;
                        case 1:
                            imgAddFriend.setBackgroundResource(R.mipmap.icon_data_01);
                            break;
                    }
                    textAge.setText(age + "");
                    textNum.setText(num + "");
                    textZiwei.setText(ziwei);
                    textLocation.setText(distance + "km +");
                    progressText.setText(userscore + "");
                    textDesc.setText(desc);
                    if (sign != null) {
                        textSign.setText(sign);
                    }

                    if (sevenday == 0) {
                        text1.setText(R.string.life_book);
                        text1.getPaint()
                                .setAntiAlias(true);//抗锯齿
                        text1.getPaint()
                                .setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                    } else {
                        text1.getPaint()
                                .setFlags(0);
                    }
                    if (thirthday == 0) {
                        text2.setText(R.string.ta_life_book);
                        text2.getPaint()
                                .setAntiAlias(true);//抗锯齿
                        text2.getPaint()
                                .setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                    } else {
                        text2.getPaint()
                                .setFlags(0);
                    }

                    OkGo.<String>post(Urls.Url_UserLook).tag(this)
                            .params("userid", PreferenceManager.getUserId(this))
                            .params("lookid", userid)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {

                                }
                            });
                }

                break;
            case DATAFELLED:
                Toast.makeText(UserDetailsActivity.this, getActivity().getResources()
                        .getString(R.string.text_err), Toast.LENGTH_SHORT)
                        .show();
                break;
            case ANIMATION:
                imgAni.setVisibility(View.GONE);
                animationDrawable.stop();
                break;
        }
    }

    private void changeDialogLocation(Dialog dialog) {
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.x = 0; //x小于0左移，大于0右移
        p.y = -300; //y小于0上移，大于0下移
        dialogWindow.setAttributes(p);
    }

    public void refresh() {
        OkGo.<String>post(Urls.Url_AnalyzeLifeBookInfo).tag(this)
                .params("uid", PreferenceManager.getUserId(this))
                .params("userid", userid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Gson gson = new Gson();
                        LoginBean loginBean = gson.fromJson(response.body(), LoginBean.class);
                        if (loginBean.getStatus()
                                .equals("success")) {
                            userInforBean = gson.fromJson(response.body(), UserInforBean.class);

                            dataBean = userInforBean.getData();
                            username = dataBean.getName();
                            lifeuserid = dataBean.getLifeuserid() + "";
                            sex = dataBean.getSex();
                            age = dataBean.getAge();
                            num = dataBean.getNum();
                            distance = dataBean.getDistance();
                            userscore = dataBean.getUserscore();
                            avatar = dataBean.getHead();
                            friend = dataBean.getFriend();
                            ziwei = dataBean.getZiwei();
                            sevenday = dataBean.getSevenday();
                            thirthday = dataBean.getThirthday();
                            desc = dataBean.getUserdesc();
                            sign = dataBean.getObligate();
                            lookhead = dataBean.getLookhead();
                            vip = dataBean.getVip();

                            imgList.add(avatar);

                            for (int i = 0; i < dataBean.getImages()
                                    .size(); i++) {
                                imgList.add(dataBean.getImages()
                                                    .get(i));
                            }

                            getHandler().obtainMessage(DATASUCCESS)
                                    .sendToTarget();
                        } else if (loginBean.getStatus()
                                .equals("fail")) {
                            getHandler().obtainMessage(DATAFELLED)
                                    .sendToTarget();
                        }
                    }
                });
    }

    private void initPopupWind() {

        new PopWindow.Builder(this).setStyle(PopWindow.PopWindowStyle.PopUp)
                .addItemAction(new PopItemAction(getActivity().getResources().getString(R.string.foreseers_inform), PopItemAction.PopItemStyle
                        .Normal, new PopItemAction
                        .OnClickListener() {
                    @Override
                    public void onClick() {
                        intent = new Intent(UserDetailsActivity.this, ReportActivity.class);
                        startActivity(intent);
                    }
                }))
                .addItemAction(new PopItemAction(getActivity().getResources().getString(R.string.go_blacklist), PopItemAction.PopItemStyle.Normal, new
                        PopItemAction
                        .OnClickListener() {
                    @Override
                    public void onClick() {

                        blackDialog = new BlackDialog(UserDetailsActivity.this, R.style.MyDialog, new BlackDialog.LeaveMyDialogListener() {

                            @Override
                            public void onClick(View view) {

                                switch (view.getId()) {
                                    case R.id.button_ok:
                                        blackDialog.dismiss();

                                        OkGo.<String>post(Urls.Url_AddBlack).tag(this)
                                                .params("userid", EMClient.getInstance()
                                                        .getCurrentUser())
                                                .params("blackid", userid)
                                                .execute(new StringCallback() {
                                                    @Override
                                                    public void onSuccess(Response<String> response) {

                                                    }
                                                });

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    EMClient.getInstance()
                                                            .contactManager()
                                                            .addUserToBlackList(userid, true);
                                                    EMClient.getInstance()
                                                            .contactManager()
                                                            .deleteContact(userid);
                                                } catch (HyphenateException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                        finish();
                                        break;
                                    case R.id.button_cancel:
                                        blackDialog.dismiss();
                                        break;
                                }
                            }
                        });
                        blackDialog.setCancelable(true);
                        blackDialog.show();
                    }
                }))
                .addItemAction(new PopItemAction(getActivity().getResources().getString(R.string.cancel), PopItemAction.PopItemStyle.Cancel, new
                        PopItemAction
                        .OnClickListener() {
                    @Override
                    public void onClick() {

                    }
                }))
                .create()
                .show();
    }

    class CustomViewHolder implements BannerViewHolder<Object> {

        private ImageView mImageView;

        @Override
        public View createView(Context context) {
            // 返回mImageView页面布局
            mImageView = new ImageView(context);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mImageView.setBackgroundColor(Color.BLACK);
            mImageView.setLayoutParams(params);
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return mImageView;
        }

        @Override
        public void onBind(Context context, int position, Object data) {
            // 数据绑定
            Glide.with(context)
                    .load(data)
                    .into(mImageView);
        }
    }

    @SuppressLint("NewApi")
    private void initSound() {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(this, R.raw.eraser, 1);
    }

    private void playSound() {
        soundPool.play(soundID, 0.1f,      //左耳道音量【0~1】
                       0.5f,      //右耳道音量【0~1】
                       0,         //播放优先级【0表示最低优先级】
                       0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                       1          //播放速度【1是正常，范围从0~2】
                      );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();



    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (lookhead==1){
                intent =new Intent();
                intent.putExtra("position",position);
                intent.putExtra("userid",userid);
                setResult(0x002,intent);
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
