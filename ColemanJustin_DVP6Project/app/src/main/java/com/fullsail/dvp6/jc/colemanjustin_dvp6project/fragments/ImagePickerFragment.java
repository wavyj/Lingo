package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.ImagePickerActivity;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

public class ImagePickerFragment extends Fragment {

    public static ImagePickerFragment newInstance() {

        Bundle args = new Bundle();

        ImagePickerFragment fragment = new ImagePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.imagepicker_fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Matisse.from(getActivity()).choose(MimeType.of(MimeType.GIF, MimeType.PNG, MimeType.JPEG))
                .countable(true)
                .maxSelectable(3)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .spanCount(3)
                .imageEngine(new PicassoEngine())
                .forResult(0x0111);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0111 && resultCode == Activity.RESULT_CANCELED){
            if (getActivity() instanceof ImagePickerActivity){
                ((ImagePickerActivity) getActivity()).onCancelled();
            }
        }
    }
}
