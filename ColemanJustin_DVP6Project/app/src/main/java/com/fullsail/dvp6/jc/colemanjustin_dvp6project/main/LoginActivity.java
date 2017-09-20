package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.LoginFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.SignupFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;


public class LoginActivity extends AppCompatActivity implements LoginFragment.toSignUpListener,
        SignupFragment.toLoginListener{

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        String email = PreferencesUtil.getEmail(this);
        String password = PreferencesUtil.getPassword(this);
        String username = PreferencesUtil.getUsername(this);
        if (!email.equals("") && !username.equals("") && !password.equals("")){
            loginUser(email, username, password);

        }else {
            // Login Fragment
            getFragmentManager().beginTransaction().replace(R.id.content_frame,
                    LoginFragment.newInstance(this), LoginFragment.TAG).commit();
        }
    }

    // METHODS
    private void loginUser(final String email, final String username, final String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    SendBird.connect(email, new SendBird.ConnectHandler() {
                        @Override
                        public void onConnected(User user, SendBirdException e) {
                            if (e != null){
                                // Error
                                e.printStackTrace();

                                PreferencesUtil.setConnected(LoginActivity.this, false);
                                return;
                            }

                            PreferencesUtil.setConnected(LoginActivity.this, true);
                            PreferencesUtil.updateUsername(username);
                            PreferencesUtil.updateUserToken();

                            Intent conversationsIntent = new Intent(LoginActivity.this, ConversationsActivity.class);
                            startActivity(conversationsIntent);
                            finish();
                        }
                    });
                }else {
                    Log.d(TAG, task.getException().getMessage());
                }
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
