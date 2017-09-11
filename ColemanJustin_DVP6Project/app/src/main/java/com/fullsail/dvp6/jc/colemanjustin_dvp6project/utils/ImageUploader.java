package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
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

    public interface onImageUploadedListener{
        void onUploadComplete(String imageUrl);
    }

    public ImageUploader(Context context, Uri path){
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        signInAnon(path);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://dvp6-project.appspot.com/");
    }

    private void uploadImage(Uri path){
        String imageName = SendBird.getCurrentUser().getUserId() + "/" + path.getLastPathSegment();
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
                        Log.d(TAG, mImageUrl);
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

    public UploadTask getUploadTask(){
        return mUploadTask;
    }

    public String getImageUrl(){
        return mImageUrl;
    }

    private void signInAnon(final Uri path){
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    uploadImage(path);
                }
            }
        });
    }
}
