package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sendbird.android.SendBird;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ImageUploader{
    private static final String TAG = "ImageUploader";

    private Context mContext;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private UploadTask mUploadTask;
    private String mImageUrl;
    private onImageUploadedListener mUploadListener;
    private ProgressDialog mProgress;

    public interface onImageUploadedListener{
        void onUploadComplete(String imageUrl, int size, ProgressDialog progress);
    }

    public ImageUploader(Context context, onImageUploadedListener uploadedListener, Uri path){
        mContext = context;
        mUploadListener = uploadedListener;
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://dvp6-project.appspot.com/");
        uploadImage(path);
    }

    private void uploadImage(Uri path){
        // Show progress
        mProgress = new ProgressDialog(mContext, R.style.dialog);
        mProgress.setIndeterminate(true);
        mProgress.setMessage(mContext.getString(R.string.progress));
        mProgress.show();
        mProgress.setCanceledOnTouchOutside(false);

        final String imageName = SendBird.getCurrentUser().getUserId() + "/" + new Date().getTime();
        final StorageReference imageRef = mStorageRef.child("images/" + imageName);

        // Upload Image
        mUploadTask = imageRef.putFile(path);
        mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot snapshot) {
                Log.d(TAG, "Success");

                // Get download url
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mImageUrl = uri.toString();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Error
                        e.printStackTrace();
                    }
                });

                imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        int size = (int) storageMetadata.getSizeBytes();
                        mUploadListener.onUploadComplete(mImageUrl, size, mProgress);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

    }

}
