package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.ConversationsActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.LoginActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.MessagesActivity;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Dialog;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.DialogsUtil;
import com.sendbird.android.GroupChannel;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.TimeUtil.getTimeAgo;

public class ConversationsFragment extends Fragment implements DialogsListAdapter.OnDialogViewClickListener {

    public static final String TAG = "MessengerFragment";
    private static final int MESSAGING = 0x01000010;

    private DialogsList dialogsList;
    private DialogsListAdapter dialogsListAdapter;
    private ImageLoader imageLoader;

    private ArrayList<Dialog> dialogs;

    public static ConversationsFragment newInstance(ArrayList<byte[]> channels) {

        Bundle args = new Bundle();
        args.putSerializable("CHANNELS", channels);

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

        ArrayList<byte[]> channelsdata = (ArrayList<byte[]>) getArguments().getSerializable("CHANNELS");
        ArrayList<GroupChannel> channels = new ArrayList<>();
        for (byte[] i: channelsdata){
            channels.add((GroupChannel) GroupChannel.buildFromSerializedData(i));
        }

        dialogsList = (DialogsList) getView().findViewById(R.id.dialogsList);
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
        dialogsListAdapter = new DialogsListAdapter<>(imageLoader);

        // Create Dialog from each channel
        dialogs = DialogsUtil.getDialogs(channels);

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
        startActivityForResult(messageIntent, MESSAGING);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (getActivity() instanceof LoginActivity){
                ((ConversationsActivity) getActivity()).loadConversations();
            }
        }
    }
}
