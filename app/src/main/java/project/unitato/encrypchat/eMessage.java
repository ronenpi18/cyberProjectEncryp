package project.unitato.encrypchat;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by denis on 6/9/14.
 */
public class eMessage {

    public static final String JSON_SERVER_REQTYPE = "reqtype";
    public static final String JSON_MSG_TYPE = "type";
    public static final String JSON_MSG_CONTENT = "content";
    public static final String JSON_MSG_FROM = "from";
    public static final String JSON_MSG_TO = "to";

    public static final int REQTYPE_SENDMSG = 0;
    public static final int REQTYPE_GETMSG = 1;
    public static final int REQTYPE_REGISTER = 2;
    public static final int REQTYPE_GETPK = 3;

    public static final int MSGTYPE_TEXT = 0;
    public static final int MSGTYPE_AES = 1;

    private JSONObject messageJson;


    public eMessage(String to, String from, String content, int type){
        try {
            messageJson.put(JSON_MSG_CONTENT, content);
            messageJson.put(JSON_MSG_FROM, from);
            messageJson.put(JSON_MSG_TO, to);
            messageJson.put(JSON_MSG_TYPE, type);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public eMessage(String json) throws JSONException{
        messageJson = new JSONObject(json);
    }

    public String getJson(){
        return messageJson.toString();
    }

    public String getContent() throws JSONException{
        return messageJson.getString(JSON_MSG_CONTENT);
    }


}
