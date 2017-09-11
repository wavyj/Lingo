package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.ImagePickerActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.MessagesActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Message;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TimeUtil;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TimeUtil.getTimeAgo;

public class MessagingFragment extends Fragment implements MessageInput.AttachmentsListener,
        Dialog.OnClickListener, MessagesActivity.onReceivedUploadPath {

    public static final String TAG = "MessagingFragment";

    private MessageInput inputView;
    private MessagesList messagesList;
    private MessagesListAdapter<Message> messagesListAdapter;
    private ImageLoader imageLoader;
    private GroupChannel groupChannel;
    private String imageUrl;

    public static MessagingFragment newInstance(byte[] selection) {

        Bundle args = new Bundle();
        args.putByteArray("SELECTED", selection);

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

        byte[] selection = getArguments().getByteArray("SELECTED");
        groupChannel = (GroupChannel) GroupChannel.buildFromSerializedData(selection);
        setTitle();

        // MessageInput Setup
        inputView = (MessageInput) getView().findViewById(R.id.input);
        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {

                groupChannel.sendUserMessage(String.valueOf(input), SendBird.getCurrentUser().getUserId(), new BaseChannel.SendUserMessageHandler() {
                    @Override
                    public void onSent(UserMessage userMessage, SendBirdException e) {
                        if (e != null){
                            // Error
                            e.printStackTrace();
                        }

                        // Update MessageListAdapter
                        messagesListAdapter.addToStart(new Message(userMessage), true);
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
        SendBird.addChannelHandler(groupChannel.getUrl(), new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (baseChannel != null){
                    if (baseMessage != null){
                        Message m = null;
                        // Check type of message
                        if (UserMessage.buildFromSerializedData(baseMessage.serialize()) instanceof UserMessage){
                            UserMessage msg = (UserMessage) UserMessage.buildFromSerializedData(baseMessage.serialize());
                            m = new Message(msg);
                        }else if (AdminMessage.buildFromSerializedData(baseMessage.serialize()) instanceof  AdminMessage){
                            AdminMessage msg = (AdminMessage) AdminMessage.buildFromSerializedData(baseMessage.serialize());
                            m = new Message(msg);
                        } else if (FileMessage.buildFromSerializedData(baseMessage.serialize()) instanceof FileMessage) {

                        }

                        // Update display
                        messagesListAdapter.addToStart(m, true);
                    }
                }
            }
        });

    }

    private void getMessages(final GroupChannel groupChannel){
        groupChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, false, 100, true, BaseChannel.
                MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                ArrayList<Message> messages = new ArrayList<Message>();
                for (BaseMessage i: list){
                    if (i instanceof UserMessage){
                        UserMessage msg = (UserMessage) UserMessage.buildFromSerializedData(i.serialize());
                        Message m = new Message(msg);
                        messages.add(m);

                    }else if (i instanceof AdminMessage){
                        /*AdminMessage msg = (AdminMessage) AdminMessage.buildFromSerializedData(i.serialize());
                        Message m = new Message(msg);
                        messages.add(m);*/

                    }else if (i instanceof FileMessage){

                    }
                }

                messagesListAdapter.addToEnd(messages, false);

                // Set messages read
                groupChannel.markAsRead();
            }
        });
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
                            status = "Active: " + TimeUtil.getTimeAgo(getActivity(), new Date(i.getLastSeenAt()));
                        }else if (state.equals("ONLINE")){
                            status = "Online";
                        }else {
                            status = "Unavailble";
                        }

                        if (getActivity() instanceof MessagesActivity){
                            // Sets Toolbar title to other user's name
                            ((MessagesActivity) getActivity()).mToolbar.setTitle(title);
                            ((MessagesActivity) getActivity()).getSupportActionBar().setSubtitle(status);
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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0111 && resultCode == Activity.RESULT_OK){

        }
    }


    // Firebase Storage Upload

    @Override
    public void onReceived(String url) {
        imageUrl = url;
    }
}
