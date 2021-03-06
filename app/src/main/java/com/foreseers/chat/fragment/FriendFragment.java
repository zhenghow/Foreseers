package com.foreseers.chat.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.foreseers.chat.R;
import com.foreseers.chat.activity.NewFriendsMsgActivity;
import com.foreseers.chat.activity.UserDetailsActivity;
import com.foreseers.chat.bean.FriendBean;
import com.foreseers.chat.bean.FriendNumBean;
import com.foreseers.chat.db.InviteMessgeDao;
import com.foreseers.chat.util.PreferenceManager;
import com.foreseers.chat.util.Urls;
import com.foreseers.chat.view.widget.MyTitleBar;
import com.google.gson.Gson;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseBaseFragment;
import com.hyphenate.easeui.ui.EaseContactListFragment;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.exceptions.HyphenateException;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.MODE_PRIVATE;

/**
 * 联系人
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends EaseBaseFragment {
    private static final String TAG = "EaseContactListFragment";
    protected List<EaseUser>   contactList = new ArrayList<EaseUser>();
    protected ListView listView;
    protected boolean hidden;
    protected ImageButton clearSearch;
    protected EditText query;
    protected Handler handler = new Handler();
    protected EaseUser toBeProcessUser;
    protected String toBeProcessUsername;
    protected EaseContactList contactListLayout;
    protected boolean isConflict;
    protected FrameLayout contentContainer;
    @BindView(R.id.my_titlebar) MyTitleBar myTitlebar;

    Unbinder unbinder;
    @BindView(R.id.text_num1) TextView textNum1;
    @BindView(R.id.text_num2) TextView textNum2;

    @BindView(R.id.search_clear) ImageButton searchClear;

    @BindView(R.id.layout_FriendFragment) LinearLayout layoutFriendFragment;

    private Map<String, EaseUser> contactsMap;
    private LinearLayout addFriend;
    private List<FriendBean.DataBean> dataBeans = new ArrayList<>();
    private Map<String, EaseUser> map = new HashMap<>();
    private EaseUser easeUser;
    private SharedPreferences sharedPreferences;
    private InviteMessgeDao inviteMessgeDao;
    private String userid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //to avoid crash when open app after long time stay in background after user logged into
        // another device
        if (savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false)) {
            return;
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initView() {

        //        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.item_chat, null);
        //        HeaderItemClickListener clickListener = new HeaderItemClickListener();

        //        addFriend = headerView.findViewById(R.id.item_layout_friend);
        //        addFriend.setOnClickListener(clickListener);
        contentContainer = (FrameLayout) getView().findViewById(R.id.content_container);

        contactListLayout = (EaseContactList) getView().findViewById(R.id.contact_list);
        listView = contactListLayout.getListView();
        //        listView.addHeaderView(headerView);
        //search
        query = (EditText) getView().findViewById(com.hyphenate.easeui.R.id.query);
        clearSearch = (ImageButton) getView().findViewById(com.hyphenate.easeui.R.id.search_clear);

        myTitlebar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 进入申请与通知页面
                FriendFragment.this.startActivityForResult(new Intent(getActivity(), NewFriendsMsgActivity.class),0x001);
            }
        });
        userid = PreferenceManager.getUserId(getActivity());
    }

    @Override
    protected void setUpView() {

        OkGo.<String>post(Urls.Url_GetFriendNum).tag(this)
                .params("userid", userid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        Gson gson = new Gson();
                        FriendNumBean friendNumBean = gson.fromJson(response.body(), FriendNumBean.class);
                        if (textNum2 != null) {
                            textNum2.setText(friendNumBean.getData().getFriendNums() + "");
                            textNum1.setText(friendNumBean.getData().getUsenums() + "/");
                        }
                    }
                });

        OkGo.<String>post(Urls.Url_GetFriend).tag(this)
                .params("userid", userid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Gson gson = new Gson();
                        FriendBean friendBean = gson.fromJson(response.body(), FriendBean.class);
                        if (friendBean.getStatus()
                                .equals("success")) {
                            dataBeans = friendBean.getData();
                            mHandler.obtainMessage(DATASUCCESS)
                                    .sendToTarget();

                            Log.i(TAG, "onSuccess: " + map);
                        }
                    }
                });

        EMClient.getInstance()
                .addConnectionListener(connectionListener);


        contactList.clear();
        getContactList();

        contactListLayout.init(contactList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EaseUser user = (EaseUser) listView.getItemAtPosition(position);
                if (user != null) {

                    String userid = user.getUsername();
                    String username = user.getNickname();

                    //                    // 进入用户详情页
                    Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userid", userid);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        if (listItemClickListener != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EaseUser user = (EaseUser) listView.getItemAtPosition(position);
                    listItemClickListener.onListItemClicked(user);
                }
            });
        }

        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactListLayout.filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText()
                        .clear();
                hideSoftKeyboard();
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0x001:
                setUpView();
                break;

            default:
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            refresh();
            setUpView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden) {
            refresh();
        }
    }

    /**
     * move user to blacklist
     */
    protected void moveToBlacklist(final String username) {
        final ProgressDialog pd = new ProgressDialog(getActivity());
        String st1 = getResources().getString(com.hyphenate.easeui.R.string.Is_moved_into_blacklist);
        final String st2 = getResources().getString(com.hyphenate.easeui.R.string.Move_into_blacklist_success);
        final String st3 = getResources().getString(com.hyphenate.easeui.R.string.Move_into_blacklist_failure);
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    //move to blacklist
                    EMClient.getInstance()
                            .contactManager()
                            .addUserToBlackList(username, false);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2, Toast.LENGTH_SHORT)
                                    .show();
                            refresh();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st3, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }
            }
        }).start();
    }

    // refresh ui
    public void refresh() {
        getContactList();
        contactListLayout.refresh();
        if (inviteMessgeDao == null) {
            inviteMessgeDao = new InviteMessgeDao(getActivity());
        }
        if (inviteMessgeDao.getUnreadMessagesCount() > 0) {
            myTitlebar.setRightImageResource(R.mipmap.icon_footer_profile_06);
        } else {
            myTitlebar.setRightImageResource(R.mipmap.icon_footer_profile_05);
        }
    }

    @Override
    public void onDestroy() {
        OkGo.getInstance()
                .cancelTag(this);
        EMClient.getInstance()
                .removeConnectionListener(connectionListener);

        super.onDestroy();
    }

    /**
     * get contact list and sort, will filter out users in blacklist
     */
    protected void getContactList() {
        contactList.clear();
        if (contactsMap == null) {
            return;
        }
        synchronized (this.contactsMap) {
            Iterator<Map.Entry<String, EaseUser>> iterator = contactsMap.entrySet()
                    .iterator();
            List<String> blackList = EMClient.getInstance()
                    .contactManager()
                    .getBlackListUsernames();
            while (iterator.hasNext()) {
                Map.Entry<String, EaseUser> entry = iterator.next();
                // to make it compatible with data in previous version, you can remove this check
                // if this is new app
                if (!entry.getKey()
                        .equals("item_new_friends") && !entry.getKey()
                        .equals("item_groups") && !entry.getKey()
                        .equals("item_chatroom") && !entry.getKey()
                        .equals("item_robots")) {
                    if (!blackList.contains(entry.getKey())) {
                        //filter out users in blacklist
                        EaseUser user = entry.getValue();
                        EaseCommonUtils.setUserInitialLetter(user);
                        Log.i(TAG, "user: " + user.getAvatar());
                        contactList.add(user);
                    }
                }
            }
        }

        // sorting
        Collections.sort(contactList, new Comparator<EaseUser>() {

            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if (lhs.getInitialLetter()
                        .equals(rhs.getInitialLetter())) {
                    return lhs.getNickname()
                            .compareTo(rhs.getNickname());
                } else {
                    if ("#".equals(lhs.getInitialLetter())) {
                        return 1;
                    } else if ("#".equals(rhs.getInitialLetter())) {
                        return -1;
                    }
                    return lhs.getInitialLetter()
                            .compareTo(rhs.getInitialLetter());
                }
            }
        });
    }

    protected EMConnectionListener connectionListener = new EMConnectionListener() {

        @Override
        public void onDisconnected(int error) {
            if (error == EMError.USER_REMOVED || error == EMError.USER_LOGIN_ANOTHER_DEVICE || error == EMError.SERVER_SERVICE_RESTRICTED) {
                isConflict = true;
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        onConnectionDisconnected();
                    }
                });
            }
        }

        @Override
        public void onConnected() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    onConnectionConnected();
                }
            });
        }
    };
    private EaseContactListFragment.EaseContactListItemClickListener listItemClickListener;

    protected void onConnectionDisconnected() {

    }

    protected void onConnectionConnected() {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        toBeProcessUser = (EaseUser) listView.getItemAtPosition(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
        toBeProcessUsername = toBeProcessUser.getUsername();
        getActivity().getMenuInflater()
                .inflate(R.menu.em_context_contact_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_contact) {
            try {
                // delete contact
                deleteContact(toBeProcessUser);
                // remove invitation message
                InviteMessgeDao dao = new InviteMessgeDao(getActivity());
                dao.deleteMessage(toBeProcessUser.getUsername());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (item.getItemId() == R.id.add_to_blacklist) {
            moveToBlacklist(toBeProcessUsername);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * delete contact
     *
     * @param tobeDeleteUser
     */
    public void deleteContact(final EaseUser tobeDeleteUser) {
        String st1 = getResources().getString(R.string.deleting);
        final String st2 = getResources().getString(R.string.Delete_failed);
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance()
                            .contactManager()
                            .deleteContact(tobeDeleteUser.getUsername());
                    // remove user from memory and database
                    //                    UserDao dao = new UserDao(getActivity());
                    //                    dao.deleteContact(tobeDeleteUser.getUsername());
                    //                    DemoHelper.getInstance().getContactList().remove(tobeDeleteUser.getUsername());
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            contactList.remove(tobeDeleteUser);
                            contactListLayout.refresh();
                        }
                    });
                } catch (final Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2 + e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * set contacts map, key is the hyphenate id
     *
     * @param contactsMap
     */
    public void setContactsMap(Map<String, EaseUser> contactsMap) {
        this.contactsMap = contactsMap;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface EaseContactListItemClickListener {
        /**
         * on click event for item in contact list
         *
         * @param user --the user of item
         */
        void onListItemClicked(EaseUser user);
    }

    /**
     * set contact list item click listener
     *
     * @param listItemClickListener
     */
    public void setContactListItemClickListener(EaseContactListFragment.EaseContactListItemClickListener listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
    }

    private final int DATASUCCESS = 1;
    private final int DATAFELLED = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATASUCCESS:
                    if (textNum1 != null) {
                        map.clear();
                        sharedPreferences = getActivity().getSharedPreferences("user", MODE_PRIVATE);
                        //                            sharedPreferences.edit().clear().commit();

                        for (int i = 0; i < dataBeans.size(); i++) {
                            easeUser = new EaseUser(dataBeans.get(i)
                                                            .getUserid() + "");
                            easeUser.setAvatar(dataBeans.get(i)
                                                       .getUserhead());
                            easeUser.setNickname(dataBeans.get(i)
                                                         .getUsername());

                            sharedPreferences.edit()
                                    .putString(dataBeans.get(i)
                                                       .getUserid() + "", dataBeans.get(i)
                                                       .getUsername() + "&" + dataBeans.get(i)
                                            .getUserhead())
                                    .commit();

                            map.put(dataBeans.get(i)
                                            .getUserid() + "", easeUser);
                            Log.i(TAG, "onSuccessmap: " + map);
                        }

                        setContactsMap(map);
                        refresh();
                    }

                    break;
                case DATAFELLED:

                    break;
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        OkGo.cancelTag(OkGo.getInstance()
                               .getOkHttpClient(), this);
    }
}
