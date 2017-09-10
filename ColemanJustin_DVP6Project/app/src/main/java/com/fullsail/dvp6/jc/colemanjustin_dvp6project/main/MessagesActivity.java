package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.MembersFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.MessagingFragment;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

public class MessagesActivity extends AppCompatActivity {

    private static final String TAG = "MessagesActivity";
    private static final String CHANNEL = "channel";
    private static final int SEARCHCODE = 0x01010;

    private GroupChannel groupChannel;

    public Toolbar mToolbar;

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
    public void onBackPressed() {
        super.onBackPressed();

        setResult(RESULT_OK);
        finish();
    }
}
