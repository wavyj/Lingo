package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.LoginActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "AuthenticationFragment";

    public static LoginFragment newInstance() {

        Bundle args = new Bundle();

        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
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

                    String username = usernameInput.getText().toString();
                    String displayname = displaynameInput.getText().toString();

                    displayname = displayname.replaceAll("\\s", "");

                    PreferencesUtil.setUserId(getActivity(), username);
                    PreferencesUtil.setDisplayName(getActivity(), displayname);

                    if (getActivity() instanceof LoginActivity){
                        ((LoginActivity) getActivity()).loginUser(username, displayname);
                    }
                }
                break;
            case R.id.toSignupLink:
                if (getActivity() instanceof LoginActivity){
                    ((LoginActivity) getActivity()).toSignup();
                }
        }
    }
}
