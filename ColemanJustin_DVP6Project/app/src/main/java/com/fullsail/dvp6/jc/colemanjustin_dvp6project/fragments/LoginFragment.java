package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;

import java.util.ArrayList;
import java.util.List;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "LoginFragment";

    private static toSignUpListener mSignUpListener;

    private FirebaseAuth mAuth;
    private String mSavedEmail;
    //private String mSavedDisplayname;
    private ProgressDialog mProgress;

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

        if (mSavedEmail != null){
            outState.putString("email", mSavedEmail);

            mSavedEmail = null;
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.getString("email") != null) {
                mSavedEmail = savedInstanceState.getString("email");
            }

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
        mAuth = FirebaseAuth.getInstance();
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
                    EditText usernameInput = (EditText) getView().findViewById(R.id.emailInput);
                    EditText passwordInput = (EditText) getView().findViewById(R.id.passwordInput);

                    mSavedEmail = usernameInput.getText().toString();
                    String password = passwordInput.getText().toString();

                    if (mSavedEmail.equals("") || mSavedEmail.toCharArray().length < 6){
                        usernameInput.setError(getString(R.string.emailError));
                    } else if (password.equals("") || password.toCharArray().length < 6){
                        passwordInput.setError(getString(R.string.usernameError));
                    } else {
                        password = password.replaceAll("\\s", "");

                        loginUser(mSavedEmail, password);
                    }
                }
                break;
            case R.id.toSignupLink:
                mSignUpListener.toSignUp();
        }
    }

    private void loginUser(final String email, final String password){
        mProgress = new ProgressDialog(getActivity(), R.style.dialog);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(getString(R.string.authenticating));
        mProgress.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    final String username = user.getDisplayName();
                    // Login user
                    SendBird.connect(username, new SendBird.ConnectHandler() {
                        @Override
                        public void onConnected(User user, SendBirdException e) {
                            if (e != null){
                                // Error
                                e.printStackTrace();

                                PreferencesUtil.setConnected(getActivity(), false);
                                return;
                            }

                            // Hide progress dialog
                            mProgress.cancel();

                            PreferencesUtil.setEmail(getActivity(), email);
                            PreferencesUtil.setUsername(getActivity(), username);
                            PreferencesUtil.setPassword(getActivity(), password);

                            PreferencesUtil.setConnected(getActivity(), true);
                            PreferencesUtil.updateUsername(username);
                            PreferencesUtil.updateUserToken();

                            Intent conversationsIntent = new Intent(getActivity(), ConversationsActivity.class);
                            startActivity(conversationsIntent);
                            getActivity().finish();
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();

                // Hide progress dialog
                mProgress.cancel();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialog);
                if (e.getMessage().contains("no user")) {
                    builder.setTitle(R.string.notfound);
                    builder.setMessage(R.string.notfoundMsg);
                }else if (e.getMessage().contains("password is invalid")){
                    builder.setTitle(R.string.incorrectpassword);
                    builder.setMessage(R.string.incorrectpasswordMsg);
                }
                builder.setNeutralButton(R.string.ok, null);
                builder.show();
            }
        });

    }
}
