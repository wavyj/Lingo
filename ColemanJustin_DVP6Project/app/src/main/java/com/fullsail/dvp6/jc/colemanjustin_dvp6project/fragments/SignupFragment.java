package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

public class SignupFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = "SignupFragment";

    private static toLoginListener mLoginListener;

    private String mSavedEmail;
    private String mSavedUsername;
    private ProgressDialog mProgress;
    private FirebaseAuth mAuth;

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

        if (mSavedEmail != null){
            outState.putString("email", mSavedEmail);

            mSavedEmail = null;
        }

        if (mSavedUsername != null){
            outState.putString("username", mSavedUsername);

            mSavedUsername = null;
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.getString("email") != null) {
                mSavedEmail = savedInstanceState.getString("email");
            }

            if (savedInstanceState.getString("username") != null) {
                mSavedUsername = savedInstanceState.getString("username");
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
            mAuth = FirebaseAuth.getInstance();
            TextView v = (TextView) getView().findViewById(R.id.toLoginLink);
            v.setOnClickListener(this);
            Button connectBtn = (Button) getView().findViewById(R.id.connectBtn);
            connectBtn.setOnClickListener(this);
        }
    }

    private void signupUser(final String email, final String username, final String password){
        mProgress = new ProgressDialog(getActivity(), R.style.dialog);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(getString(R.string.authenticating));
        mProgress.show();


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener
                (new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Set display name
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileChangeRequest = new
                                    UserProfileChangeRequest.Builder().setDisplayName(username).build();
                            user.updateProfile(profileChangeRequest);

                            // Signup user
                            SendBird.connect(username, new SendBird.ConnectHandler() {
                                @Override
                                public void onConnected(User user, SendBirdException e) {
                                    if (e != null) {
                                        // Error
                                        e.printStackTrace();

                                        PreferencesUtil.setConnected(getActivity(), false);
                                        return;
                                    }

                                    PreferencesUtil.setEmail(getActivity(), email);
                                    PreferencesUtil.setUsername(getActivity(), username);
                                    PreferencesUtil.setPassword(getActivity(), password);
                                    PreferencesUtil.setConnected(getActivity(), true);
                                    PreferencesUtil.updateUsername(username);
                                    PreferencesUtil.updateUserToken();

                                    // dismiss progress
                                    mProgress.cancel();

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

                // dismiss progress
                mProgress.cancel();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialog);
                builder.setTitle(R.string.exists);
                builder.setMessage(R.string.existsMsg);
                builder.setNeutralButton(R.string.ok, null);
                builder.show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connectBtn:
                if (getView() != null) {
                    // Login
                    EditText emailInput = (EditText) getView().findViewById(R.id.emailInput);
                    EditText usernameInput = (EditText) getView().findViewById(R.id.usernameInput);
                    EditText passwordInput = (EditText) getView().findViewById(R.id.passwordInput);

                    mSavedEmail = emailInput.getText().toString();
                    String password = passwordInput.getText().toString();
                    mSavedUsername = usernameInput.getText().toString();

                    if (mSavedEmail.equals("") || !Patterns.EMAIL_ADDRESS.matcher(mSavedEmail).matches()){
                        emailInput.setError(getString(R.string.emailError));
                    } else if (mSavedUsername.equals("") || mSavedUsername.toCharArray().length < 6){
                        usernameInput.setError(getString(R.string.usernameError));
                    } else if (password.equals("") || password.toCharArray().length < 6){
                        passwordInput.setError(getString(R.string.usernameError));
                    } else {
                        String username = mSavedUsername.replaceAll("\\s", "");
                        PreferencesUtil.setEmail(getActivity(), mSavedEmail);
                        PreferencesUtil.setUsername(getActivity(), username);

                        signupUser(mSavedEmail, username, password);
                    }
                }
                break;
            case R.id.toLoginLink:
                mLoginListener.toLogin();
        }
    }
}
