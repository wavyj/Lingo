package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.MembersFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.MessagingFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.ImageUploader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;
import com.zhihu.matisse.Matisse;

import java.util.List;

public class MessagesActivity extends AppCompatActivity implements ImageUploader.onImageUploadedListener {

    private static final String TAG = "MessagesActivity";
    private static final String CHANNEL = "channel";
    private static final int SEARCHCODE = 0x01010;

    private GroupChannel groupChannel;
    private onReceivedUploadPath mOnReceivedPath;
    private StorageReference mStorageRef;

    public Toolbar mToolbar;

    @Override
    public void onUploadComplete(String imageUrl) {
        mOnReceivedPath.onReceived(imageUrl);
    }

    public interface onReceivedUploadPath{
        void onReceived(String url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Toolbar Setup
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_icon);
        mToolbar.setTitle("Messages");
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mToolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        // Receive Intent
        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(CHANNEL)){

            groupChannel = (GroupChannel) GroupChannel.buildFromSerializedData(receivedIntent.getByteArrayExtra(CHANNEL));
            mToolbar.setTitle("");
            // Messages Fragment
            getFragmentManager().beginTransaction().replace(R.id.content_frame, MessagingFragment.
                    newInstance(groupChannel.serialize()), MessagingFragment.TAG).commit();
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_members:
                // To members fragment
                getFragmentManager().beginTransaction().replace(R.id.content_frame, MembersFragment.
                        newInstance(groupChannel.getUrl()), MembersFragment.TAG).addToBackStack(MembersFragment.TAG).commit();
                break;
            case R.id.action_search:
                // Search messages
                break;
            case R.id.action_delete:
                // Delete conversation
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete Conversation?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        groupChannel.leave(new GroupChannel.GroupChannelLeaveHandler() {
                            @Override
                            public void onResult(SendBirdException e) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    }
                });
                builder.setNeutralButton("No", null);
                builder.show();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0111 && resultCode == RESULT_OK){
            // Handle selected images
            List<Uri> selected = Matisse.obtainResult(data);
            //MessagingFragment messagingFragment = (MessagingFragment) getFragmentManager().
                    //findFragmentByTag(MessagingFragment.TAG);

            ImageUploader imageUploader = new ImageUploader(this, selected.get(0));

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setResult(RESULT_OK);
        finish();
    }
}
