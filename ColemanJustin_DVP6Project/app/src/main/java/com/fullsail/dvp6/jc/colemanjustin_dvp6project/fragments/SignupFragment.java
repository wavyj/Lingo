package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import com.sendbird.android.UserListQuery;

import java.util.ArrayList;
import java.util.List;

public class SignupFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = "SignupFragment";

    private static toLoginListener mLoginListener;

    private String mSavedUsername;
    private String mSavedDisplayname;
    private ProgressDialog mProgress;

    public interface toLoginListener{
        void toLogin();
    }

    public static SignupFragment newInstance(toLoginListener loginListener) {

        Bundle args = new Bundle();

        mLoginListener = loginListener;

        SignupFragment fragment = new SignupFragment();
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

        if (savedInstanceState != null) {
            if (savedInstanceState.getString("username") != null) {
                mSavedUsername = savedInstanceState.getString("username");
            }

            if (savedInstanceState.getString("displayname") != null) {
                mSavedDisplayname = savedInstanceState.getString("displayname");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.signup_fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null){
            TextView v = (TextView) getView().findViewById(R.id.toLoginLink);
            v.setOnClickListener(this);
            Button connectBtn = (Button) getView().findViewById(R.id.connectBtn);
            connectBtn.setOnClickListener(this);
        }
    }

    private void loginUser(final String username, final String displayName){
        mProgress = new ProgressDialog(getActivity());
        mProgress.setIndeterminate(true);
        mProgress.setMessage(getString(R.string.authenticating));
        mProgress.show();

        List<String> ids = new ArrayList<>();
        ids.add(username);

        // Search for existing user
        UserListQuery userListQuery = SendBird.createUserListQuery(ids);
        userListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }

                // dismiss progress
                mProgress.cancel();

                if (list != null && list.size() > 0){
                    // Signup user
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
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.exists);
                    builder.setMessage(R.string.existsMsg);
                    builder.setNeutralButton(R.string.ok, null);
                    builder.show();
                }
            }
        });
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
                        String displayname = mSavedDisplayname.replaceAll("\\s", "");

                        PreferencesUtil.setUserId(getActivity(), mSavedUsername);
                        PreferencesUtil.setDisplayName(getActivity(), displayname);

                        loginUser(mSavedUsername, displayname);
                    }
                }
                break;
            case R.id.toLoginLink:
                mLoginListener.toLogin();
        }
    }
}
