package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.app.Notification;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.UserMessage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private MaterialSearchView searchView;
    private ArrayList<User> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final ListView listView = (ListView) findViewById(R.id.usersListView);

        // Toolbar Setup
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // MaterialSearchView Setup
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                // Search
                UserListQuery userListQuery = SendBird.createUserListQuery();
                userListQuery.setLimit(10);
                userListQuery.next(new UserListQuery.UserListQueryResultHandler() {
                    @Override
                    public void onResult(List<User> list, SendBirdException e) {
                        if (e != null){
                            // Error
                            e.printStackTrace();

                        }

                        // Convert to arraylist
                        ArrayList<User> userList = new ArrayList<User>();
                        for (User i: list){
                            // Only get users other than the current user
                            if (!i.getUserId().equals(SendBird.getCurrentUser().getUserId()) &&
                                    !i.getUserId().equals("bot")) {
                                if (i.getNickname().contains(query)) {
                                    userList.add(i);
                                }
                            }
                        }

                        mUsers = userList;
                        checkEmpty(query);


                        if (mUsers != null && mUsers.size() > 0) {
                            // ListView setup
                            QueryAdapter queryAdapter = new QueryAdapter(SearchActivity.this, userList);
                            listView.setAdapter(queryAdapter);
                            listView.setOnItemClickListener(userClicked);
                        }
                    }
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Do nothing
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                // Do nothing
            }

            @Override
            public void onSearchViewClosed() {
                // Do nothing
            }
        });

        searchView.setVoiceSearch(false);
        searchView.setCloseIcon(getDrawable(R.drawable.close_icon));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    AdapterView.OnItemClickListener userClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final User selected = mUsers.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this, R.style.dialog);
            builder.setTitle("Start Conversation with " + selected.getNickname() + "?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    createChat(selected);
                }
            });
            builder.setNeutralButton("NO", null);
            builder.show();
        }
    };

    private void createChat(User otherUser){
        User currentUser = SendBird.getCurrentUser();
        final List<User> users = new ArrayList<>();
        users.add(currentUser);
        users.add(otherUser);

        // Create Conversation
        GroupChannel.createChannel(users, true, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                    finish();
                }

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void checkEmpty(String query){
        if (mUsers == null || mUsers.size() == 0 ){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog);
            builder.setTitle("No Results");
            builder.setMessage("\"" + query + "\"" + " Not Found");
            builder.setNeutralButton("OK", null);
            builder.show();
        }
    }
}
