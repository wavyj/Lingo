package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.LoginActivity;

public class SignupFragment extends Fragment {
    public static final String TAG = "SignupFragment";

    public static SignupFragment newInstance() {

        Bundle args = new Bundle();

        SignupFragment fragment = new SignupFragment();
        fragment.setArguments(args);
        return fragment;
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
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() instanceof LoginActivity){
                        ((LoginActivity) getActivity()).toLogin();
                    }
                }
            });
        }
    }
}
