package social.media.chatbot;

import java.util.ArrayList;
import java.util.Comparator;

public class Global {
    public static ArrayList<ChatMessage> Array_chat = new ArrayList<ChatMessage>();

    public static class FishNameComparator implements Comparator<ChatMessage>
    {
        public int compare(ChatMessage left, ChatMessage right) {
            return left.getMsgID().compareTo(right.getMsgID());
        }
    }
}
