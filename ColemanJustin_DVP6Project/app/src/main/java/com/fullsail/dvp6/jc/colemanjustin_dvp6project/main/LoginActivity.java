package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
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
import com.sendbird.android.UserListQuery;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity implements LoginFragment.toSignUpListener,
        SignupFragment.toLoginListener{

    private static final String TAG = "LoginActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Check if user is logged in
        String username = PreferencesUtil.getUserId(this);
        String displayName = PreferencesUtil.getDisplayName(this);
        if (!username.equals("") && !displayName.equals("")){
            loginUser(username, displayName);

        }else {
            SendBird.connect("exampleID", new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    if (e != null){
                        e.printStackTrace();
                    }
                }
            });

            // Login Fragment
            getFragmentManager().beginTransaction().replace(R.id.content_frame,
                    LoginFragment.newInstance(this), LoginFragment.TAG).commit();
        }
    }

    // METHODS
    private void loginUser(String username, final String displayName){
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
                PreferencesUtil.updateDisplayName(displayName);
                PreferencesUtil.updateUserToken();

                Intent conversationsIntent = new Intent(LoginActivity.this, ConversationsActivity.class);
                startActivity(conversationsIntent);
                finish();
            }
        });
    }

    @Override
    public void toSignUp() {
        SignupFragment signupFragment = SignupFragment.newInstance(this);

        // Transition
        Slide slide = new Slide(Gravity.RIGHT);
        slide.setDuration(250);
        signupFragment.setEnterTransition(slide);
        signupFragment.setSharedElementEnterTransition(slide);
        signupFragment.setAllowEnterTransitionOverlap(true);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, signupFragment).commit();
    }

    @Override
    public void toLogin(){
        LoginFragment loginFragment = LoginFragment.newInstance(this);

        // Transition
        Slide slide = new Slide(Gravity.LEFT);
        slide.setDuration(150);
        loginFragment.setEnterTransition(slide);
        loginFragment.setSharedElementEnterTransition(slide);
        loginFragment.setAllowEnterTransitionOverlap(true);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, loginFragment).commit();
    }

}
