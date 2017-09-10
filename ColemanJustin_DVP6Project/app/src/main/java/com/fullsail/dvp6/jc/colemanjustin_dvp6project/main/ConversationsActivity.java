package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.ConversationsFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.EmptyMessagesFragment;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import java.util.ArrayList;
import java.util.List;

public class ConversationsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ConversationsActivity";
    private static final int SEARCHCODE = 0x01010;

    private ArrayList<byte[]> mConversations;
    private Boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        mConversations = new ArrayList<>();

        // Floating Action Button Setup
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(this);

        // Toolbar Setup
        showToolbar();

        loadConversations();

        handleEvents();

        SendBird.setAutoBackgroundDetection(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_account:
                // To account activity
                break;
            case R.id.action_settings:
                // To settings activity
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            loadConversations();
        }

        showHideFab();
    }

    private void showToolbar(){
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Messages");
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        setSupportActionBar(mToolbar);
    }

    public void loadConversations(){
        isLoading = true;

        // Load all conversations
        GroupChannelListQuery channelListQuery = GroupChannel.createMyGroupChannelListQuery();
        channelListQuery.setIncludeEmpty(true);
        channelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }


                mConversations.clear();
                // Add each channel to conversations arrayList
                for (GroupChannel i: list){
                    if (i.getMembers().size() > 1) {
                        mConversations.add(i.serialize());
                    }
                }

                // Conversations Fragment
                if (mConversations.size() > 0) {
                    getFragmentManager().beginTransaction().replace(R.id.content_frame,
                            ConversationsFragment.newInstance(mConversations), ConversationsFragment.TAG).commit();
                } else {
                    getFragmentManager().beginTransaction().replace(R.id.content_frame,
                            EmptyMessagesFragment.newInstance(), EmptyMessagesFragment.TAG).commit();
                }
                isLoading = false;
            }
        });
    }

    private void handleEvents(){
        SendBird.addChannelHandler("channelhandler", new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                // Refresh
                if (!isLoading){
                    loadConversations();
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                super.onChannelDeleted(channelUrl, channelType);
                // Refresh
                if (!isLoading) {
                    loadConversations();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        // To Search activity
        Intent searchIntent = new Intent(this, SearchActivity.class);
        startActivityForResult(searchIntent, SEARCHCODE);

        showHideFab();
    }

    private void showHideFab(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        if (fab.isShown()){
            fab.hide();
        }else {
            fab.show();
        }
    }
}
