package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;


public class EmptyMessagesFragment extends Fragment {
    public static final String TAG = "EmptyMessagesFragment";
    public static EmptyMessagesFragment newInstance() {

        Bundle args = new Bundle();

        EmptyMessagesFragment fragment = new EmptyMessagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.empty_messages_fragment_layout, container, false);
    }
}
