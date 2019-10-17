package social.media.chatbot;


/**
 * Created by beast on 14/4/17.
 */

public class ChatMessage {

    private String msgText;
    private String msgUser;
    private String msgID;



    public ChatMessage(String msgText, String msgUser, String id){
        this.msgText = msgText;
        this.msgUser = msgUser;
        this.msgID = id;

    }


    public ChatMessage(){

    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getMsgUser() {
        return msgUser;
    }

    public void setMsgUser(String msgUser) {
        this.msgUser = msgUser;
    }

    public String getMsgID() {
        return msgID;
    }
}
