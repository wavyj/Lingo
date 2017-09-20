package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.ImagePickerActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.MessagesActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Author;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.ImageMessage;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Message;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.MessagesDatabaseSQLHelper;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TimeUtil;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TranslationUtil;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.MessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;
import com.zhihu.matisse.MimeType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TimeUtil.getTimeAgo;

public class MessagingFragment extends Fragment implements MessageInput.AttachmentsListener,
        Dialog.OnClickListener, MessagesActivity.onReceivedUploadPath, TranslationUtil.onTranslateCompleteListener {

    public static final String TAG = "MessagingFragment";

    private static final int SPEECH_CODE = 0x0121;

    private MessageInput inputView;
    private MessagesList messagesList;
    private ArrayList<Message> loadedMessages;
    private MessagesListAdapter<Message> messagesListAdapter;
    private ImageLoader imageLoader;
    private GroupChannel groupChannel;

    public static MessagingFragment newInstance(String selection) {

        Bundle args = new Bundle();
        args.putString("SELECTED", selection);

        MessagingFragment fragment = new MessagingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.messages_fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String selection = getArguments().getString("SELECTED");
        getChannel(selection);

        PreferencesUtil.setLanguage(getActivity(), getResources().getConfiguration().locale.getLanguage());
    }

    private void getChannel(String url){
        GroupChannel.getChannel(url, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel channel, SendBirdException e) {
                groupChannel = channel;
                messagingSetup();
            }
        });
    }

    private void messagingSetup(){

        setTitle();

        // MessageInput Setup
        inputView = (MessageInput) getView().findViewById(R.id.input);
        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {

                groupChannel.sendUserMessage(String.valueOf(input), null, PreferencesUtil.getLanguage(getActivity()), new BaseChannel.SendUserMessageHandler() {
                    @Override
                    public void onSent(UserMessage userMessage, SendBirdException e) {
                        if (e != null){
                            // Error
                            e.printStackTrace();
                        }


                        // Update MessageListAdapter
                        Message m = new Message(userMessage);
                        messagesListAdapter.addToStart(m, true);

                        // Cache message
                        MessagesDatabaseSQLHelper.getInsance(getActivity()).insertMessage(m, groupChannel.getUrl());

                    }
                });

                return true;
            }

        });
        inputView.setAttachmentsListener(this);

        // Messaging Setup
        messagesList = (MessagesList) getView().findViewById(R.id.messagesList);
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                if (url != null && !url.equals("")) {
                    Picasso.with(getActivity()).load(url).into(imageView);
                }else {
                    Picasso.with(getActivity()).load(R.drawable.emptybox_icon).into(imageView);
                }
            }
        };

        messagesListAdapter = new MessagesListAdapter<Message>(SendBird.getCurrentUser().getUserId()
                , imageLoader);
        getMessages(groupChannel);
        messagesListAdapter.setDateHeadersFormatter(new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
                if (DateFormatter.isToday(date)) {
                    return getTimeAgo(getActivity(), date);
                } else if (DateFormatter.isYesterday(date)) {
                    return getString(R.string.date_header_yesterday);
                } else {
                    return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
                }
            }
        });
        messagesList.setAdapter(messagesListAdapter);

        // Receive messages
        SendBird.addChannelHandler("messagingHandler", new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (baseChannel != null){
                    if (baseMessage != null){
                        Message m = null;

                        // Check type of message
                        if (UserMessage.buildFromSerializedData(baseMessage.serialize()) instanceof UserMessage){
                            UserMessage msg = (UserMessage) UserMessage.buildFromSerializedData(baseMessage.serialize());
                            m = new Message(msg);

                            // Translate if not in user's language
                            if (!m.getLang().equals(PreferencesUtil.getLanguage(getActivity()))) {
                                new TranslationUtil(getActivity(), m, MessagingFragment.this).execute(msg.getMessage());
                            }

                        }else if (FileMessage.buildFromSerializedData(baseMessage.serialize()) instanceof FileMessage) {
                            FileMessage msg = (FileMessage) FileMessage.buildFromSerializedData(baseMessage.serialize());
                            m = new ImageMessage(msg);

                            // Update display & Cache message
                            MessagesDatabaseSQLHelper.getInsance(getActivity()).insertMessage(m, groupChannel.getUrl());
                            messagesListAdapter.addToStart(m, true);

                        }

                    }
                }
            }
        });
    }

    private void getMessages(final GroupChannel groupChannel){
        loadedMessages = new ArrayList<>();

        // Load Messages from SQL database
        MessagesDatabaseSQLHelper db = MessagesDatabaseSQLHelper.getInsance(getActivity());
        Cursor c = db.query(groupChannel.getUrl());
        while (c.moveToNext()){
            String id = c.getString(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_ID));
            String text = c.getString(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_TEXT));
            long time = c.getLong(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_TIME));
            String senderID = c.getString(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_SENDER));
            String type = c.getString(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_TYPE));

            Author a;
            if (senderID.equals(groupChannel.getMembers().get(0).getUserId())){
                a = new Author(groupChannel.getMembers().get(0));
            } else {
                a = new Author(groupChannel.getMembers().get(1));
            }
            Message m;

            if (type.equals("Text")) {
                m = new Message(id, a, text, new Date(time));

            }else {
                String imageUrl = c.getString(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_IMAGE));

                m = new ImageMessage(id, text, a, new Date(time), imageUrl);
            }

            loadedMessages.add(m);

        }
        c.close();

        long lastCachedMessage = Long.MAX_VALUE;

        // Add cached Messages
        if (loadedMessages != null && loadedMessages.size() > 1) {
            messagesListAdapter.addToEnd(loadedMessages, false);

            // Get the time of the last cached message
            lastCachedMessage = loadedMessages.get(0).getCreatedAt().getTime();
            Log.d(TAG, loadedMessages.get(0).getText());
        }

        BaseChannel.GetMessagesHandler messagesHandler = new BaseChannel.GetMessagesHandler() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                for (BaseMessage i: list){
                    if (i instanceof UserMessage){
                        UserMessage msg = (UserMessage) UserMessage.buildFromSerializedData(i.serialize());
                        Message m = new Message(msg);
                        //messages.add(m);

                        // Translate if not in user's language
                        if (!m.getLang().equals(PreferencesUtil.getLanguage(getActivity()))) {
                            new TranslationUtil(getActivity(), m, MessagingFragment.this).execute(msg.getMessage());
                        }

                    }else if (i instanceof FileMessage){
                        FileMessage msg = (FileMessage) FileMessage.buildFromSerializedData(i.serialize());
                        ImageMessage m = new ImageMessage(msg);
                        //messages.add(m);

                        loadedMessages.add(m);
                        messagesListAdapter.addToStart(m, true);

                        // Cache message into sql database
                        MessagesDatabaseSQLHelper.getInsance(getActivity()).insertMessage(m, groupChannel.getUrl());
                    }
                }

                // Set messages read
                groupChannel.markAsRead();
            }
        };

        if (c.getCount() == 0){
            groupChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, false, 60, false, BaseChannel.
                    MessageTypeFilter.ALL, null, messagesHandler);
        } else {
            // Get the latest messages that are not cached
            groupChannel.getNextMessagesByTimestamp(lastCachedMessage, false, 60, false, BaseChannel.
                    MessageTypeFilter.ALL, null, messagesHandler);
        }
    }

    private void setTitle(){

        groupChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }
                String title = "";
                String status = "";
                String state = "";

                for (Member i: groupChannel.getMembers()){
                    if (!i.getUserId().equals(SendBird.getCurrentUser().getUserId())){

                        title = i.getNickname();
                        state = i.getConnectionStatus().toString();
                        Log.d(TAG, state);
                        if (state.equals("OFFLINE")) {
                            status = getString(R.string.activeLabel) + " " + TimeUtil.getTimeAgo(getActivity(), new Date(i.getLastSeenAt()));
                        }else if (state.equals("ONLINE")){
                            status = getString(R.string.online_status);
                        }else {
                            status = getString(R.string.unavailable_status);
                        }

                        if (getActivity() instanceof MessagesActivity){
                            // Sets Toolbar title to other user's name
                            if (((MessagesActivity) getActivity()).getSupportActionBar() != null) {
                                ((MessagesActivity) getActivity()).mToolbar.setTitle(title);
                                ((MessagesActivity) getActivity()).getSupportActionBar().setSubtitle(status);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onAddAttachments() {
        new AlertDialog.Builder(getActivity(), R.style.dialog).setItems(R.array.othermessages, this).show();
    }

    @Override
    public void onClick(DialogInterface dialog, int selection) {
        switch (selection){
            case 0:
                // Image
                Intent imagePickerIntent = new Intent(getActivity(), ImagePickerActivity.class);
                getActivity().startActivityForResult(imagePickerIntent, 0x0111);
                break;
            case 1:
                // Voice
                Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.voice_prompt);

                try {
                    startActivityForResult(speechIntent, SPEECH_CODE);
                } catch (ActivityNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(getActivity(),getString(R.string.voiceunavailable), Toast.LENGTH_SHORT).show();
                }

                break;
            case 2:
                // Cancel
                dialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0111 && resultCode == Activity.RESULT_OK){

        }

        if (requestCode == SPEECH_CODE && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            inputView.getInputEditText().setText(result.get(0));
        }
    }

    // Firebase Storage Upload
    @Override
    public void onReceived(String url, int size) {
        sendImage(url, size);
    }

    // Image Message
    private void sendImage(String url, int size){
        groupChannel.sendFileMessage(url, getString(R.string.image_name), "text/uri-list", 0, "", new BaseChannel.SendFileMessageHandler() {
            @Override
            public void onSent(FileMessage fileMessage, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                    Log.d(TAG, String.valueOf(e.getCode()));
                }

                if (fileMessage != null) {
                    Log.d(TAG, fileMessage.getUrl());

                    ImageMessage m = new ImageMessage(fileMessage);
                    messagesListAdapter.addToStart(m, true);
                }

            }
        });
    }

    @Override
    public void translationComplete(Message m) {

        // Update Message text
        MessagesDatabaseSQLHelper.getInsance(getActivity()).insertMessage(m, groupChannel.getUrl());
        messagesListAdapter.addToStart(m, true);
        loadedMessages.add(m);
    }
}
