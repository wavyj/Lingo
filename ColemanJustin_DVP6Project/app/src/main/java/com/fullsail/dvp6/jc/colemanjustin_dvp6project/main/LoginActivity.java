package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.LoginFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.ConversationsFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.EmptyMessagesFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.SignupFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private static final int SEARCHCODE = 0x01010;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mNavigationTitles;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<byte[]> mConversations;
    private boolean isLoading = false;
    private Scene mScene;
    private Transition enterTransition;
    private Transition exitTransition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mConversations = new ArrayList<>();

        //Floating Action Button Setup
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(this);
        fab.hide();

        // Check if user is logged in
        String username = PreferencesUtil.getUserId(this);
        String displayName = PreferencesUtil.getDisplayName(this);
        if (!username.equals("") && !displayName.equals("")){
            loginUser(username, displayName);
            showHideFab();

            // Eventhandler setup
            handleEvents();

        }else {
            // Authentication Fragment
            getFragmentManager().beginTransaction().replace(R.id.content_frame,
                    LoginFragment.newInstance(), LoginFragment.TAG).commit();
        }

        SendBird.setAutoBackgroundDetection(true);
    }

    // LISTENERS
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendBird.removeAllChannelHandlers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_account:
                // To account activity
                break;
            case R.id.action_settings:
                // To settings activity
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
           loadConversations();
        }

        showHideFab();
    }


    // METHODS

    private void showToolbar(){
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Messages");
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        setSupportActionBar(mToolbar);
    }

    public void loginUser(String username, final String displayName){
        SendBird.connect(username, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();

                    PreferencesUtil.setConnected(LoginActivity.this, false);
                    return;
                }

                PreferencesUtil.setConnected(LoginActivity.this, true);
                updateUserInfo(displayName);
                updateUserToken();

                loadConversations();
            }
        });
    }
    private void updateUserInfo(String displayName){
        SendBird.updateCurrentUserInfo(displayName, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();

                }
            }
        });
    }

    private void updateUserToken(){
        SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), new SendBird.RegisterPushTokenWithStatusHandler() {
            @Override
            public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadConversations(){
        isLoading = true;

        // Load all conversations
        GroupChannelListQuery channelListQuery = GroupChannel.createMyGroupChannelListQuery();
        channelListQuery.setIncludeEmpty(true);
        channelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }


                mConversations.clear();
                // Add each channel to conversations arrayList
                for (GroupChannel i: list){
                    if (i.getMembers().size() > 1) {
                        mConversations.add(i.serialize());
                    }
                }

                showToolbar();
                if (mConversations.size() > 0) {
                    getFragmentManager().beginTransaction().replace(R.id.content_frame,
                            ConversationsFragment.newInstance(mConversations), ConversationsFragment.TAG).commit();
                } else {
                    getFragmentManager().beginTransaction().replace(R.id.content_frame,
                            EmptyMessagesFragment.newInstance(), EmptyMessagesFragment.TAG).commit();
                }
                isLoading = false;
            }
        });
    }

    private void handleEvents(){
        SendBird.addChannelHandler("channelhandler", new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                // Refresh
                loadConversations();
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                super.onChannelDeleted(channelUrl, channelType);
                // Refresh
                loadConversations();
            }
        });
    }

    public void toSignup(){
        SignupFragment signupFragment = SignupFragment.newInstance();

        Slide slide = new Slide(Gravity.RIGHT);
        slide.setDuration(250);
        signupFragment.setEnterTransition(slide);
        signupFragment.setSharedElementEnterTransition(slide);
        signupFragment.setAllowEnterTransitionOverlap(true);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, signupFragment).commit();
    }

    public void toLogin(){
        LoginFragment loginFragment = LoginFragment.newInstance();

        Slide slide = new Slide(Gravity.LEFT);
        slide.setDuration(150);
        loginFragment.setEnterTransition(slide);
        loginFragment.setSharedElementEnterTransition(slide);
        loginFragment.setAllowEnterTransitionOverlap(true);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, loginFragment).commit();
    }

    @Override
    public void onClick(View v) {
        // To Search activity
        Intent searchIntent = new Intent(this, SearchActivity.class);
        startActivityForResult(searchIntent, SEARCHCODE);

        showHideFab();
    }

    private void showHideFab(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        if (fab.isShown()){
            fab.hide();
        }else {
            fab.show();
        }
    }
}
