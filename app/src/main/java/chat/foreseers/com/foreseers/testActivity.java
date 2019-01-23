package chat.foreseers.com.foreseers;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import chat.foreseers.com.foreseers.activity.UserDataActivity;
import chat.foreseers.com.foreseers.bean.LocationBean;
import chat.foreseers.com.foreseers.bean.LoginBean;
import chat.foreseers.com.foreseers.bean.UserDataBean;
import chat.foreseers.com.foreseers.util.GetLocation;
import chat.foreseers.com.foreseers.util.Urls;

public class testActivity extends AppCompatActivity {

    @BindView(R.id.tv_location_show)
    TextView tvLocationShow;
    @BindView(R.id.btn_show_location)
    Button btnShowLocation;
    @BindView(R.id.btn_isnewuser)
    Button btnIsnewuser;
    @BindView(R.id.btn_regestnewuser)
    Button btnRegestnewuser;
    @BindView(R.id.btn_location)
    Button btnLocation;
    private String latLongString;
    private String country;
    private String city;
    private String area;
    private String addr;
    private String addrs;
    private Intent intent;
    private String facebookId;
    private String facebookName;
    private LoginBean loginBean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        //获取权限
        initauthority();

        init();// 关联控件，初始化按钮、展示框
        tv_show.setText("地理位置");// 设置默认的位置展示，也就是没有点击按钮时的展示
        lastKnowLoc = "中国";
        // 点击按钮时去初始化一次当前位置
        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationInit();// 调用位置初始化方法
            }
        });


    }

    private void initauthority() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] mPermissionList = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, mPermissionList, 123);
        }
    }

    private LocationManager locationManager;// 位置服务
    private Location location;// 位置
    private Button btn_show;// 按钮
    private TextView tv_show;// 展示地理位置
    public static final int SHOW_LOCATION = 0;
    private String lastKnowLoc;

    //监听地理位置变化，地理位置变化时，能够重置location
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            tv_show.setText("更新失败失败");
        }

        @Override
        public void onLocationChanged(Location loc) {
//            if (loc != null) {
//                location = loc;
//                showLocation(location);
//            }
        }
    };


    //这个就是地理位置初始化，主要通过调用其他方法获取经纬度，并设置到location
    public void locationInit() {
        try {
            // 获取系统服务
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // 判断GPS是否可以获取地理位置，如果可以，展示位置，
            // 如果GPS不行，再判断network，还是获取不到，那就报错
            if (locationInitByGPS() || locationInitByNETWORK()) {
                // 上面两个只是获取经纬度的，获取经纬度location后，再去调用谷歌解析来获取地理位置名称
                Log.i("GPS", "locationInit: 经度" + location.getLatitude() + "维度" + location
                        .getLongitude());
                showLocation(location);
            } else {
                tv_show.setText("获取地理位置失败，上次的地理位置为：" + lastKnowLoc);
            }
        } catch (Exception e) {
            tv_show.setText("获取地理位置失败，上次的地理位置为：" + lastKnowLoc);
        }
    }

    // GPS去获取location经纬度
    public boolean locationInitByGPS() {
        // 没有GPS，直接返回
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                100000, 0, locationListener);
        location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            return true;//设置location成功，返回true
        } else {
            return false;
        }
    }

    // network去获取location经纬度
    public boolean locationInitByNETWORK() {
        // 没有NETWORK，直接返回
        if (!locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        location = locationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            return true;
        } else {
            return false;
        }
    }


    //控件初始化
    private void init() {
        btn_show = (Button) findViewById(R.id.btn_show_location);
        tv_show = (TextView) findViewById(R.id.tv_location_show);
    }


    //这是根据经纬度，向谷歌的API解析发网络请求，然后获取response，这里超时时间不要太短，否则来不及返回位置信息，直接失败了
    private void showLocation(final Location loc) {
        List<Address> addList = null;
        Geocoder ge = new Geocoder(getApplicationContext());
        try {
            addList = ge.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//            addList = ge.getFromLocation(22.3043710000, 114.1853080000, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addList != null && addList.size() > 0) {
            for (int i = 0; i < addList.size(); i++) {
                Address ad = addList.get(i);

                latLongString = ad.getAdminArea() + ad.getLocality() + ad.getLocale();
//                address.getAddressLine(0)+address.getAddressLine(1)+address.getAddressLine(2)
// +address.getFeatureName();


                Log.i("latLongString", "getAddressLine: " + ad.getAddressLine(0));
                Log.i("latLongString", "getSubAdminArea: " + ad.getSubAdminArea());
                Log.i("latLongString", "getPhone: " + ad.getPhone());
                Log.i("latLongString", "getMaxAddressLineIndex: " + ad.getMaxAddressLineIndex());
                Log.i("latLongString", "getUrl: " + ad.getUrl());
                Log.i("latLongString", "getCountryCode: " + ad.getCountryCode());
                Log.i("latLongString", "getLocale: " + ad.getLocale());

                Log.i("latLongString", "#######################: ");

                Log.i("latLongString", "getCountryName: " + ad.getCountryName());//国
                Log.i("latLongString", "getAdminArea: " + ad.getAdminArea());//省
                Log.i("latLongString", "getLocality: " + ad.getLocality());//市
                Log.i("latLongString", "getSubLocality: " + ad.getSubLocality());//区
                Log.i("latLongString", "getFeatureName: " + ad.getFeatureName());//街

                Log.i("latLongString", "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%: ");


                //国
                country = ad.getCountryName();
                //省
                city = ad.getAdminArea();
                //市
                area = ad.getLocality();
                //区
                addr = ad.getSubLocality();
                //街
                addrs = ad.getFeatureName();

            }
        }
        tv_show.setText(latLongString);
        UpLocation();
    }


    private void UpLocation() {
        //获取token

        SharedPreferences SharedPreferences = this.getSharedPreferences("loginToken", MODE_PRIVATE);
        String facebookid = SharedPreferences.getString("loginToken", null);
        Log.i("okgo", "facebookid: " + facebookid);
//        SharedPreferences.Editor editor = userInfo.edit();//获取Editor //得到Editor后，写入需要保存的数据
//        editor.get("token", facebookId);
//        editor.commit();//提交修改

        OkGo.<String>post(Urls.Url_UserLocation).tag(this)
                .params("facebookid", "46797950360650")
                .params("country", country)
                .params("city", city)
                .params("area", area)
                .params("addr", addr)
                .params("addrs", addrs)
                .params("lat", location.getLatitude())
                .params("lng", location.getLongitude())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                    }
                });
    }

   private long myfacebookId = 00;
    @OnClick({R.id.btn_isnewuser, R.id.btn_regestnewuser,R.id.btn_location})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_isnewuser:

                facebookId = "4679795036065"+myfacebookId;
                facebookName = "zheng3";
                OkGo.<String>post(Urls.Url_Login).tag(this)
                        .params("facebookid", facebookId)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                Gson gson = new Gson();
                                loginBean = gson.fromJson(response.body(), LoginBean.class);
                                if (loginBean.getStatus().equals("fail")){


                                    tv_show.setText("用户不存在"+myfacebookId);
                                }else {
                                    tv_show.setText("用户存在");
                                }

                            }
                        });
                break;
            case R.id.btn_regestnewuser:
                locationInit();
                OkGo.<String>post(Urls.Url_UserData).tag(this)
//                .isSpliceUrl(true)
                        .params("username", facebookName)
                        .params("date", "2002-01-16")
                        .params("time", "16:00:00")
                        .params("gender","F")
                        .params("facebookid", facebookId)
                        .params("zone",  "UTC-02:00")
                        .params("country", country)
                        .params("city", city)
                        .params("area", area)
                        .params("addr", addr)
                        .params("addrs", addrs)
                        .params("lat", location.getLatitude())
                        .params("lng", location.getLongitude())
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                Log.e("OkGo", "Success: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" );
                              Gson  gson = new Gson();
                                UserDataBean   userDataBean = gson.fromJson(response.body(),UserDataBean.class);
                                if (userDataBean.getStatus().equals("success")){
                                    myfacebookId=myfacebookId+1;
                                    Toast.makeText(testActivity.this,"发送成功" ,Toast.LENGTH_LONG).show();
                                }else if (userDataBean.getStatus().equals("fail")){
                                    Toast.makeText(testActivity.this,"发送失败" ,Toast.LENGTH_LONG).show();
                                }

                            }

                            @Override
                            public void onError(Response<String> response) {
                                super.onError(response);
                                Log.e("OkGo", "onError:############################### "+response.getException() );
//                        mHandler.obtainMessage(DATAFELLED).sendToTarget();
                            }
                        });
                break;
            case R.id.btn_location:
                GetLocation location=new GetLocation();
                LocationBean locationBean=location.getLocation(testActivity.this);
                tv_show.setText(locationBean.getAddrs());
                Log.i("location", "onViewClicked:"+locationBean.getCountry());
                break;
        }
    }
}
