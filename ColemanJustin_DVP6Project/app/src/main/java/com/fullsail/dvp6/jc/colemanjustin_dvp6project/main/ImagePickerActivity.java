package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.ImagePickerFragment;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.security.Permission;

public class ImagePickerActivity extends AppCompatActivity {
    private static final String TAG = "ImagePickerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x0112);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && requestCode == 0x0112 && grantResults[0] == PackageManager.
                PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            setupImagePicker();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.permissiontitle);
            builder.setMessage(R.string.permissionmsg);
            builder.setNeutralButton(R.string.tryagain, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(ImagePickerActivity.this, new String[]{Manifest
                            .permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x0112);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onCancelled();
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0111 && resultCode == RESULT_CANCELED){
            setResult(RESULT_CANCELED);
            finish();
        }else if (requestCode == 0x0111 && resultCode == RESULT_OK){
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void setupImagePicker(){
        getFragmentManager().beginTransaction().replace(R.id.content_frame, ImagePickerFragment.
                newInstance()).commit();
    }

    public void onCancelled(){
        setResult(RESULT_CANCELED);
        finish();
    }
}
