package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class DialogsUtil {
    private DialogsUtil() {
        throw new AssertionError();
    }

    private static final String TAG = "DialogsFixtures";

    public static ArrayList<Dialog> getDialogs(ArrayList<GroupChannel> groupChannels) {

        ArrayList<Dialog> chats = new ArrayList<>();
        Message m = null;
        for (GroupChannel i: groupChannels) {
            if (i.getLastMessage() != null) {
                if (UserMessage.buildFromSerializedData(i.getLastMessage().serialize()) instanceof UserMessage) {
                    UserMessage msg = (UserMessage) UserMessage.buildFromSerializedData(i.getLastMessage().serialize());
                    m = new Message(msg);

                } else if (AdminMessage.buildFromSerializedData(i.getLastMessage().serialize()) instanceof AdminMessage) {
                    AdminMessage msg = (AdminMessage) AdminMessage.buildFromSerializedData(i.getLastMessage().serialize());
                    m = new Message(msg);

                } else if (FileMessage.buildFromSerializedData(i.getLastMessage().serialize()) instanceof FileMessage) {

                }
                chats.add(getDialog(i, new Date(i.getLastMessage().getCreatedAt()), m));
            }else {
                // Create Message when none exist
                m = new Message("new", new Author(SendBird.getCurrentUser()), "New Conversation", new Date());
                chats.add(getDialog(i, new Date(), m));
            }
        }

        return chats;
    }

    private static Dialog getDialog(GroupChannel groupChannel, Date lastMessageCreatedAt,
                                    Message message) {
        ArrayList<Author> users = getUsers(groupChannel);
        Author other = null;
        if (users.get(0).getId().equals(SendBird.getCurrentUser().getUserId())){
            other = users.get(1);
        }else {
            other = users.get(0);
        }
        return new Dialog(
                other.getId(), other.getName(), other.getAvatar(), users,
                message, groupChannel.getUnreadMessageCount(), groupChannel);
    }

    private static ArrayList<Author> getUsers(GroupChannel groupChannel) {
        ArrayList<Author> users = new ArrayList<>();
        List<Member> members = groupChannel.getMembers();
        for (Member i: members){
            users.add(new Author(i));
        }
        return users;
    }
}
