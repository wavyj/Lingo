package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sendbird.android.SendBird;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUploader{
    private static final String TAG = "ImageUploader";

    private Context mContext;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private UploadTask mUploadTask;
    private Uri mImageUri;
    private onImageUploadedListener mUploadListener;
    private ProgressDialog mProgress;

    public interface onImageUploadedListener{
        void onUploadComplete(Uri imageUrl, int size, ProgressDialog progress);
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

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        String formatted = format.format(new Date());

        final String imageName = SendBird.getCurrentUser().getUserId() + "/" + formatted;
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
                        mUploadListener.onUploadComplete(uri, 0, mProgress);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Error
                        e.printStackTrace();
                    }
                });
            }
        });

    }

}
