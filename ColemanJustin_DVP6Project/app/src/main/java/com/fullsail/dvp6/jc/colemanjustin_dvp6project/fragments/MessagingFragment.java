package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.databases.ImagesDatabaseSQLHelper;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.ImagePickerActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.MessagesActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Author;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.ImageAnalyzeUtil;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.ImageMessage;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Message;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.databases.MessagesDatabaseSQLHelper;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.SmartReplyUtil;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TimeUtil;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TranslationUtil;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TimeUtil.getTimeAgo;

public class MessagingFragment extends Fragment implements MessageInput.AttachmentsListener,
        Dialog.OnClickListener, MessagesActivity.onReceivedUploadPath,
        TranslationUtil.onTranslateCompleteListener, ImageAnalyzeUtil.onDetectComplete,
        SmartReplyUtil.onSuggestionsCompleteListener{

    public static final String TAG = "MessagingFragment";

    private static final int SPEECH_CODE = 0x0121;

    private MessageInput inputView;
    private MessagesList messagesList;
    private ArrayList<Message> loadedMessages;
    private MessagesListAdapter<Message> messagesListAdapter;
    private ImageLoader imageLoader;
    private GroupChannel groupChannel;
    private Uri mImageUri;
    private ArrayList<Message> mTextMessages;
    private long mLastCachedTime = Long.MAX_VALUE;
    private String mSuggestion;
    private FloatingActionButton mFab;

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

        loadedMessages = new ArrayList<>();
        mTextMessages = new ArrayList<>();

        mFab = (FloatingActionButton) getView().findViewById(R.id.suggestionBtn);
        mFab.hide();

        PreferencesUtil.setLanguage(getActivity(), Locale.getDefault().getLanguage());
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
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputView.getInputEditText().setText(mSuggestion);
                showSuggestion();
            }
        });
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

                        // Hide suggestion Fab
                        if (mFab != null && mFab.isShown()){
                            showSuggestion();
                        }

                        // Update MessageListAdapter
                        Message m = new Message(userMessage);
                        loadedMessages.add(m);

                        translateMessages();
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
            public void loadImage(final ImageView imageView, final String url) {
                if (url != null && !url.equals("")) {
                    // Check if image is already saved an load from cache
                    Cursor c = ImagesDatabaseSQLHelper.getInstance(getActivity()).getImage(url);

                    if (c != null && c.getCount() != 0){
                        c.moveToFirst();
                        byte[] img = c.getBlob(c.getColumnIndex(ImagesDatabaseSQLHelper.COLUMN_IMAGE));
                        Bitmap bmp = BitmapFactory.decodeByteArray(img, 0, img.length);
                        imageView.setImageBitmap(bmp);
                    }else {
                        // Download from url if not cached
                            new AsyncTask<Void, Void, Bitmap>(){
                                @Override
                                protected Bitmap doInBackground(Void... params) {
                                    // Load into image view
                                    try {
                                        Bitmap bmp = Picasso.with(getActivity()).load(url).get();

                                        // Create byte array
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                                        byte[] imageBytes = baos.toByteArray();

                                        // Cache image
                                        ImagesDatabaseSQLHelper.getInstance(getActivity()).insertImage(url, imageBytes);
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Bitmap bitmap) {
                                    super.onPostExecute(bitmap);

                                    imageView.setImageBitmap(bitmap);
                                }
                            }.execute();
                    }
                }else {
                    Picasso.with(getActivity()).load(R.drawable.emptybox_icon).into(imageView);
                }
            }
        };

        messagesListAdapter = new MessagesListAdapter<Message>(SendBird.getCurrentUser().getUserId()
                , imageLoader);
        messagesListAdapter.setOnMessageViewClickListener(new MessagesListAdapter.OnMessageViewClickListener<Message>() {
            @Override
            public void onMessageViewClick(View view, Message message) {
                // Show Message Text
                if (message instanceof ImageMessage && !message.getText().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialog);
                    builder.setTitle(R.string.textDetect);
                    builder.setMessage(message.getText());
                    builder.setPositiveButton(R.string.ok, null);
                    builder.show();
                }
            }
        });

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
                loadedMessages.clear();
                if (baseChannel != null){
                    if (baseMessage != null){
                        Message m = null;
                        ImageMessage img = null;

                        // Check type of message
                        if (UserMessage.buildFromSerializedData(baseMessage.serialize()) instanceof UserMessage){
                            UserMessage msg = (UserMessage) UserMessage.buildFromSerializedData(baseMessage.serialize());
                            m = new Message(msg);

                            // Suggest Reply
                            new SmartReplyUtil(getActivity(), m.getText(), MessagingFragment.this);

                            loadedMessages.add(m);

                        }else if (FileMessage.buildFromSerializedData(baseMessage.serialize()) instanceof FileMessage) {
                            FileMessage msg = (FileMessage) FileMessage.buildFromSerializedData(baseMessage.serialize());
                            img = new ImageMessage(msg);

                            loadedMessages.add(img);

                        }

                        translateMessages();

                    }
                }
            }
        });
    }

    private void getMessages(final GroupChannel groupChannel){
        loadedMessages.clear();

        // Load Messages from SQL database
        MessagesDatabaseSQLHelper db = MessagesDatabaseSQLHelper.getInsance(getActivity());
        Cursor c = db.query(groupChannel.getUrl());
        Log.d(TAG, String.valueOf(c.getCount()));

        while (c.moveToNext()){
            String id = c.getString(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_ID));
            String text = c.getString(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_TEXT));
            Log.d(TAG, text);
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
            ImageMessage img;

            if (type.equals("Text")) {
                m = new Message(id, a, text, new Date(time));
                loadedMessages.add(m);

            }else {
                String imageUrl = c.getString(c.getColumnIndex(MessagesDatabaseSQLHelper.COLUMN_IMAGE));

                img = new ImageMessage(id, text, a, new Date(time), imageUrl);
                loadedMessages.add(img);
            }

            // Get the time of the last cached message
            mLastCachedTime = loadedMessages.get(0).getCreatedAt().getTime();
            translateMessages();

        }
        c.close();

        mLastCachedTime = Long.MAX_VALUE;

        BaseChannel.GetMessagesHandler messagesHandler = new BaseChannel.GetMessagesHandler() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                for(int i = 0; i < list.size(); i++){

                    if (list.get(i) instanceof  UserMessage){
                        UserMessage msg = (UserMessage) UserMessage.buildFromSerializedData(list.get(i).serialize());
                        Message m = new Message(msg);

                        loadedMessages.add(m);

                    }else if (list.get(i) instanceof FileMessage){
                        FileMessage msg = (FileMessage) FileMessage.buildFromSerializedData(list.get(i).serialize());
                        ImageMessage m = new ImageMessage(msg);

                        loadedMessages.add(m);

                    }
                }

                translateMessages();

                // Set messages read
                groupChannel.markAsRead();
            }
        };


        if (messagesListAdapter.getItemCount() == 0){
            groupChannel.getPreviousMessagesByTimestamp(mLastCachedTime, false, 60, false, BaseChannel.
                    MessageTypeFilter.ALL, null, messagesHandler);
        } else {
            // Get the latest messages that are not cached
            groupChannel.getNextMessagesByTimestamp(mLastCachedTime, false, 60, false, BaseChannel.
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
            // Hide suggestion Fab
            if (mFab != null && mFab.isShown()){
                showSuggestion();
            }
            inputView.getInputEditText().setText(result.get(0));
        }
    }

    // Firebase Storage Upload
    @Override
    public void onReceived(Uri uri, int size, ProgressDialog progress) {
        // Hide suggestion Fab
        if (mFab != null && mFab.isShown()){
            showSuggestion();
        }
        sendImage(uri, size, progress);
    }

    // Image Message
    private void sendImage(Uri uri, int size, final ProgressDialog progress){
        mImageUri = uri;
        if (mImageUri == null){
            progress.cancel();
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.dialog);
            dialog.setTitle(R.string.imageerrortitle);
            dialog.setMessage(R.string.imageerror);
            dialog.setPositiveButton(R.string.ok, null);
            dialog.show();
            return;
        }
        groupChannel.sendFileMessage(mImageUri.toString(), mImageUri.getLastPathSegment(), "text/uri-list", 0, "",
                new BaseChannel.SendFileMessageHandler() {
            @Override
            public void onSent(FileMessage fileMessage, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                    progress.cancel();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.dialog);
                    dialog.setTitle(R.string.imageerrortitle);
                    dialog.setMessage(R.string.imageerror);
                    dialog.setPositiveButton(R.string.ok, null);
                    dialog.show();
                    return;
                }

                if (fileMessage != null) {
                    progress.cancel();

                    ImageMessage m = new ImageMessage(fileMessage);

                    // Detect Text in Image
                    ImageAnalyzeUtil.setup(getActivity(), mImageUri.toString(), MessagingFragment.this, m);
                    messagesListAdapter.addToStart(m, true);
                }

            }
        });
    }

    @Override
    public void detectionComplete(String text, ImageMessage m) {
        // Cache ImageMessage
        try {
            long result = MessagesDatabaseSQLHelper.getInsance(getActivity()).insertImage(m, groupChannel.getUrl());
        } catch (SQLiteConstraintException e){
            int result = MessagesDatabaseSQLHelper.getInsance(getActivity()).updateImage(m, groupChannel.getUrl());
        } catch (SQLiteException e){
            e.printStackTrace();
        }
    }

    // Translation
    private void translateMessages(){
        boolean isLast = false;
        mTextMessages = new ArrayList<>();

      // Add each message that needs to be translated to a separate arraylist
        for (Message m: loadedMessages) {
            if (m.getLang()!= null && !m.getLang().equals(PreferencesUtil.getLanguage(getActivity()))) {
                mTextMessages.add(m);
            }
        }

        // Display other messages if none need to be translated
        if (mTextMessages.size() == 0){
            displayLoadedMessages();
        }

        // Translate each message
        for(int i = 0; i < mTextMessages.size(); i++){
            if (i == mTextMessages.size() - 1){
                isLast = true;
            }

            new TranslationUtil(getActivity(), loadedMessages.get(i), isLast, i, MessagingFragment.
                    this).execute(loadedMessages.get(i).getText());
        }
    }

    private void displayLoadedMessages(){
        // After all messages have been checked
        // Cache each message and display them
        for (Message message: loadedMessages){

            if (message instanceof ImageMessage) {

                if (message.getText().equals("") || message.getText().equals("Image")) {
                    // Detect Text in Image
                    ImageAnalyzeUtil.setup(getActivity(), ((ImageMessage) message).
                            getImageUrl(), MessagingFragment.this, ((ImageMessage) message));
                }
            }else {
                if (!MessagesDatabaseSQLHelper.getInsance(getActivity()).checkMessage(message, groupChannel.getUrl())){
                    // Insert if message does not exist in database
                    try {
                        long result = MessagesDatabaseSQLHelper.getInsance(getActivity()).insertMessage(message, groupChannel.getUrl());
                    } catch (SQLiteException e){
                        e.printStackTrace();
                    }
                }
            }

            messagesListAdapter.addToStart(message, true);
        }

        loadedMessages.clear();
    }

    @Override
    public void translationComplete(Message m, int i, boolean b) {
        if (mTextMessages.size() > 0) {
            Message msg = mTextMessages.get(i);
            msg = m;

            if (b){
                displayLoadedMessages();
            }
        }
    }

    @Override
    public void onSuggestionsComplete(String suggestion) {
        // Update Display
        if (!suggestion.equals("")){
            mSuggestion = suggestion;
            showSuggestion();
        }
    }

    private void showSuggestion(){
        if (mFab != null){
            if (mFab.isShown()){
                mFab.hide();
                mSuggestion = null;
            }else {
                mFab.show();
            }
        }
    }
}
