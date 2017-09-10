package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersFragment extends Fragment {

    public static final String TAG = "MembersFragment";

    private static final String CHANNEL = "SELECTED";

    public static MembersFragment newInstance(String channel) {

        Bundle args = new Bundle();
        args.putCharSequence(CHANNEL, channel);

        MembersFragment fragment = new MembersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.member_fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String selection = getArguments().getString("SELECTED");
        GroupChannel.getChannel(selection, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                // Get members
                Member member1 = groupChannel.getMembers().get(0);
                Member member2 = groupChannel.getMembers().get(1);

                // Update display
                TextView userName1 = (TextView) getView().findViewById(R.id.user1Name);
                TextView userName2 = (TextView) getView().findViewById(R.id.user2Name);
                userName1.setText(member1.getNickname());
                userName2.setText(member2.getNickname());

                CircleImageView imageView1 = (CircleImageView) getView().findViewById(R.id.user1profileImg);
                CircleImageView imageView2 = (CircleImageView) getView().findViewById(R.id.user2profileImg);

                Picasso.with(getActivity()).load(member1.getProfileUrl()).into(imageView1);
                Picasso.with(getActivity()).load(member2.getProfileUrl()).into(imageView2);
            }
        });

    }
}
