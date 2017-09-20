package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.ConversationsFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.EmptyMessagesFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments.MessagingFragment;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.MessagesDatabaseSQLHelper;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConversationsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ConversationsActivity";
    private static final int SEARCHCODE = 0x01010;

    private ArrayList<String> mConversations;
    private Boolean returningResults = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        mConversations = new ArrayList<>();

        // Floating Action Button Setup
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(this);

        // Toolbar Setup
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.messages_title);
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        setSupportActionBar(mToolbar);

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
    protected void onResume() {
        super.onResume();
        handleEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.removeAllChannelHandlers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendBird.removeAllChannelHandlers();
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
            returningResults = true;
        } else{
            returningResults = false;
        }

        showHideFab();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Fragment Transactions after Activity has been restored preventing state loss
        if (returningResults){
            loadConversations();
        }
    }

    public void loadConversations(){

        List<String> ids = new ArrayList<>();
        ids.add(SendBird.getCurrentUser().getUserId());

        // Load all conversations
        GroupChannelListQuery channelListQuery = GroupChannel.createMyGroupChannelListQuery();
        channelListQuery.setIncludeEmpty(true);
        channelListQuery.setUserIdsIncludeFilter(ids, GroupChannelListQuery.QueryType.AND);
        channelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }


                mConversations.clear();
                // Add each channel to conversations arrayList
                if (list != null) {
                    for (GroupChannel i : list) {
                        if (i.getMembers().size() > 1) {
                            mConversations.add(i.getUrl());
                        }
                    }
                }

                // Conversations Fragment
                if (mConversations.size() > 0) {
                    MessagesDatabaseSQLHelper.getInsance(ConversationsActivity.this).clearAll();
                    getFragmentManager().beginTransaction().replace(R.id.content_frame,
                            ConversationsFragment.newInstance(mConversations), ConversationsFragment.TAG).commit();
                } else {
                    getFragmentManager().beginTransaction().replace(R.id.content_frame,
                            EmptyMessagesFragment.newInstance(), EmptyMessagesFragment.TAG).commit();
                }
            }
        });
    }

    private void handleEvents(){
        SendBird.addChannelHandler("channelhandler", new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                // Refresh
                //loadConversations();
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                super.onChannelDeleted(channelUrl, channelType);
                // Refresh
                //loadConversations();
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
