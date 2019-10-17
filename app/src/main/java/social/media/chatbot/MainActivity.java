package social.media.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener {

    ListView chat_list_view;
    EditText editText;
    RelativeLayout addBtn;
    DatabaseReference ref;
    FirebaseRecyclerAdapter<ChatMessage,chat_rec> adapter;
    Boolean flagFab = true;

    private AIService aiService;
    chat_adapter chat_adapter;
    String TAG = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);


        chat_list_view = (ListView) findViewById(R.id.recyclerView);
        editText = (EditText)findViewById(R.id.editText);
        addBtn = (RelativeLayout)findViewById(R.id.addBtn);

//        chat_list_view.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
//        chat_list_view.setLayoutManager(linearLayoutManager);

        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);

        final AIConfiguration config = new AIConfiguration("d63222aec4c74e99864ea016459829d9",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System
                );

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();



        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString().trim();

                if (!message.equals("")) {

                    ChatMessage chatMessage = new ChatMessage(message, "user","0");
                    ref.child("chat").push().setValue(chatMessage);

                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest,Void,AIResponse>(){

                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(AIResponse response) {
                            if (response != null) {

                                Result result = response.getResult();
                                String reply = result.getFulfillment().getSpeech();
                                ChatMessage chatMessage = new ChatMessage(reply, "bot","0");
                                ref.child("chat").push().setValue(chatMessage);
                            }
                        }
                    }.execute(aiRequest);
                }
                else {
                    aiService.startListening();
                }

                editText.setText("");

            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = (ImageView)findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(),R.drawable.ic_send_white_24dp);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mic_white_24dp);


                if (s.toString().trim().length()!=0 && flagFab){
                    ImageViewAnimatedChange(MainActivity.this,fab_img,img);
                    flagFab=false;

                }
                else if (s.toString().trim().length()==0){
                    ImageViewAnimatedChange(MainActivity.this,fab_img,img1);
                    flagFab=true;

                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        FirebaseRecyclerOptions <ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(ref.child("chat"), new SnapshotParser<ChatMessage> (){
                            @NonNull
                            @Override
                            public ChatMessage parseSnapshot(@NonNull DataSnapshot snapshot) {

                                String msgText = "";
                                String msgjUser = "";
                                String id = "";

                                try {
                                    HashMap<String, Object> datamap = (HashMap<String, Object>) snapshot.getValue();
                                    for (String key : datamap.keySet()) {
                                        Object data = datamap.get(key);
                                        id = key;

                                        HashMap<String, Object> public_chat = (HashMap<String, Object>) data;
                                        msgText = public_chat.get("msgText").toString();
                                        msgjUser = public_chat.get("msgUser").toString();

                                    }
                                } catch (Exception E) {
                                    Log.e("ERROR", E.toString());
                                }
                                return new ChatMessage(msgText, msgjUser,id);
                            }

                        }).build();


        /*
        adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(options) {

            @NonNull
            @Override
            public chat_rec onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.msglist, parent, false);

                return new chat_rec(view);
            }

            @Override
            protected void onBindViewHolder(chat_rec chat_rec, int i, ChatMessage chatMessage) {
                if (chatMessage.getMsgUser().equals("user")) {

                    chat_rec.rightText.setText(chatMessage.getMsgText());
                    chat_rec.rightText.setVisibility(View.VISIBLE);
                    chat_rec.leftText.setVisibility(View.GONE);
                }
                else {

                    chat_rec.leftText.setText(chatMessage.getMsgText());
                    chat_rec.rightText.setVisibility(View.GONE);
                    chat_rec.leftText.setVisibility(View.VISIBLE);
                }

            }

//            @Override
//            protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {
//
//                if (model.getMsgUser().equals("user")) {
//
//
//                    viewHolder.rightText.setText(model.getMsgText());
//
//                    viewHolder.rightText.setVisibility(View.VISIBLE);
//                    viewHolder.leftText.setVisibility(View.GONE);
//                }
//                else {
//                    viewHolder.leftText.setText(model.getMsgText());
//
//                    viewHolder.rightText.setVisibility(View.GONE);
//                    viewHolder.leftText.setVisibility(View.VISIBLE);
//                }
//            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    chat_list_view.scrollToPosition(positionStart);

                }

            }
        });

         */



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("chat");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Global.Array_chat.clear();
                if (dataSnapshot.exists()){
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    ArrayList<ChatMessage> array_chat = new ArrayList<ChatMessage>();
                    for (String key : dataMap.keySet()){

                        Object data = dataMap.get(key);
                        String id = key;

                        try{
                            HashMap<String, Object> userData = (HashMap<String, Object>) data;
                            String msgText = userData.get("msgText").toString();
                            String msgUser = userData.get("msgUser").toString();


                            array_chat.add(new ChatMessage(msgText, msgUser,id));
                        }catch (Exception cce){
                            Log.e(TAG, cce.toString());
                        }

                    }
                    Global.Array_chat = array_chat;
                    Collections.sort(Global.Array_chat, new Global.FishNameComparator());
                    reset_list();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG,"Failed to rad value ", databaseError.toException());
            }
        });

//        chat_list_view.setAdapter(adapter);
    }

    private void reset_list() {
        chat_adapter = new chat_adapter(MainActivity.this,R.layout.msglist, Global.Array_chat);
        chat_list_view.setAdapter(chat_adapter);
        chat_list_view.setSelection(chat_adapter.getCount() - 1);

    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {

        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "user","0");
        ref.child("chat").push().setValue(chatMessage0);


        String reply = result.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply, "bot","1");
        ref.child("chat").push().setValue(chatMessage);

    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}
