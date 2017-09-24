package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.databases.ImagesDatabaseSQLHelper;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.ConversationsActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.MessagesActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Dialog;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.DialogsUtil;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TimeUtil.getTimeAgo;

public class ConversationsFragment extends Fragment implements DialogsListAdapter.OnDialogViewClickListener {

    public static final String TAG = "MessengerFragment";
    private static final int MESSAGING = 0x01001;

    private DialogsList dialogsList;
    private DialogsListAdapter dialogsListAdapter;
    private ImageLoader imageLoader;
    private ArrayList<GroupChannel> mChannels;
    private boolean isLast = false;
    private ArrayList<Dialog> dialogs;
    private ImageView mImageView;

    public static ConversationsFragment newInstance(ArrayList<String> channels) {

        Bundle args = new Bundle();
        args.putStringArrayList("CHANNELS", channels);

        ConversationsFragment fragment = new ConversationsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.conversations_fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dialogsList = (DialogsList) getView().findViewById(R.id.dialogsList);

        ArrayList<String> channelsdata = getArguments().getStringArrayList("CHANNELS");
        if (channelsdata == null){
            if (getActivity() instanceof  ConversationsActivity){
                ((ConversationsActivity) getActivity()).loadConversations();
                return;
            }
        }

        mChannels = new ArrayList<>();

        for (int i = 0; i < channelsdata.size(); i++){
            if (i == channelsdata.size() - 1){
                isLast = true;
            }
            loadChannel(channelsdata.get(i));
        }
    }

    private void dialogSetup(){
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, final String url) {
                if (url != null && !url.equals("")) {
                    mImageView = imageView;

                    // Check if image is already saved an load from cache
                    Bitmap img = ConversationsFragment.this.loadImage(url);

                    if (img != null){
                        imageView.setImageBitmap(img);
                    } else {

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

                                mImageView.setImageBitmap(bitmap);
                            }
                        }.execute();
                    }
                }else {
                    Picasso.with(getActivity()).load(R.drawable.emptybox_icon).into(imageView);
                }
            }
        };
        dialogsListAdapter = new DialogsListAdapter<>(imageLoader);

        // Create Dialog from each channel
        dialogs = DialogsUtil.getDialogs(mChannels, getActivity());

        // Set adapter items
        dialogsListAdapter.setItems(dialogs);
        dialogsListAdapter.setOnDialogViewClickListener(this);
        dialogsListAdapter.setDatesFormatter(new DateFormatter.Formatter() {
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
        dialogsList.setAdapter(dialogsListAdapter);
    }

    @Override
    public void onDialogViewClick(View view, IDialog dialog) {

        Dialog current = null;

        for (Dialog i: dialogs){
            if (i.getId().equals(dialog.getId())){
                current = i;
            }
        }

        // To MessagesActivity
        Intent messageIntent = new Intent(getActivity(), MessagesActivity.class);
        messageIntent.putExtra("channel", current.getGroupChannel().serialize());
        if (getActivity() instanceof ConversationsActivity){
            ((ConversationsActivity) getActivity()).eventHandlerAttach(false);
        }
        getActivity().startActivityForResult(messageIntent, MESSAGING);

    }

    private void loadChannel(String url){
        GroupChannel.getChannel(url, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }

                mChannels.add(groupChannel);

                if (isLast){
                    dialogSetup();
                }
            }
        });
    }

    public Bitmap loadImage(String url){
        Cursor c = ImagesDatabaseSQLHelper.getInstance(getActivity()).getImage(url);

        if (c != null && c.getCount() != 0){
            c.moveToFirst();
            byte[] img = c.getBlob(c.getColumnIndex(ImagesDatabaseSQLHelper.COLUMN_IMAGE));
            return BitmapFactory.decodeByteArray(img, 0, img.length);
        }else {
            return null;
        }
    }

}
