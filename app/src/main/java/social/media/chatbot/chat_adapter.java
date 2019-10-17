package social.media.chatbot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;


public class chat_adapter extends ArrayAdapter<ChatMessage> {

    ArrayList<ChatMessage> array_chat = new ArrayList<>();
    char member;
    CheckBox hire;
    public chat_adapter(Context context, int textViewResourceId, ArrayList<ChatMessage> objects) {
        super(context, textViewResourceId, objects);
        array_chat = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String chat_sender = array_chat.get(position).getMsgUser();
        if (chat_sender.equals("user") ){
            v = inflater.inflate(R.layout.msglist, null);
            TextView rightText = v.findViewById(R.id.rightText);
            TextView leftText = v.findViewById(R.id.leftText);
            rightText.setText(array_chat.get(position).getMsgText());
            rightText.setVisibility(View.VISIBLE);
            leftText.setVisibility(View.GONE);

        } else {
            v = inflater.inflate(R.layout.msglist, null);
            TextView rightText = v.findViewById(R.id.rightText);
            TextView leftText = v.findViewById(R.id.leftText);
            leftText.setText(array_chat.get(position).getMsgText());
            rightText.setVisibility(View.GONE);
            leftText.setVisibility(View.VISIBLE);
        }

        return v;
    }
}
