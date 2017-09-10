package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.ConversationsActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.LoginActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "LoginFragment";

    private static toSignUpListener mSignUpListener;

    private String mSavedUsername;
    private String mSavedDisplayname;

    public interface toSignUpListener{
        void toSignUp();
    }

    public static LoginFragment newInstance(toSignUpListener signUpListener) {

        Bundle args = new Bundle();

        mSignUpListener = signUpListener;

        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSavedUsername != null){
            outState.putString("username", mSavedUsername);

            mSavedUsername = null;
        }

        if (mSavedDisplayname != null){
            outState.putString("displayname", mSavedDisplayname);

            mSavedDisplayname = null;
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState.getString("username") != null){
            mSavedUsername = savedInstanceState.getString("username");
        }

        if (savedInstanceState.getString("displayname") != null){
            mSavedDisplayname = savedInstanceState.getString("displayname");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView signupLink = (TextView) getView().findViewById(R.id.toSignupLink);
        signupLink.setOnClickListener(this);
        Button connectBtn = (Button) getView().findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connectBtn:
                if (getView() != null) {
                    // Login
                    EditText usernameInput = (EditText) getView().findViewById(R.id.usernameInput);
                    EditText displaynameInput = (EditText) getView().findViewById(R.id.displaynameInput);

                    mSavedUsername = usernameInput.getText().toString();
                    mSavedDisplayname = displaynameInput.getText().toString();

                    if (mSavedUsername.equals("") || mSavedUsername.toCharArray().length < 6){
                        usernameInput.setError("requires at least 6 characters");
                    } else if (mSavedDisplayname.equals("") || mSavedDisplayname.toCharArray().length < 6){
                        displaynameInput.setError("requires at least 6 characterst");
                    } else {
                        String displayName = mSavedDisplayname.replaceAll("\\s", "");

                        PreferencesUtil.setUserId(getActivity(), mSavedUsername);
                        PreferencesUtil.setDisplayName(getActivity(), displayName);

                        loginUser(mSavedUsername, displayName);
                    }
                }
                break;
            case R.id.toSignupLink:
                mSignUpListener.toSignUp();
        }
    }

    private void loginUser(String username, final String displayName){
        SendBird.connect(username, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();

                    PreferencesUtil.setConnected(getActivity(), false);
                    return;
                }

                PreferencesUtil.setConnected(getActivity(), true);
                PreferencesUtil.updateDisplayName(displayName);
                PreferencesUtil.updateUserToken();

                Intent conversationsIntent = new Intent(getActivity(), ConversationsActivity.class);
                startActivity(conversationsIntent);
                getActivity().finish();
            }
        });
    }
}
