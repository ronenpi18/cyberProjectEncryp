package project.unitato.encrypchat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by denis on 2/12/14.
 */
public class Message {
    String content;
    String to;
    String from;
    String time;
    REQTYPE reqtype;
    MSGTYPE msgtype;



    public static enum REQTYPE{
        SENDMSG(0), GETMSG(1), REGISTER(2), GETPK(3);

        private int value;

        private REQTYPE(int value){
            this.value = value;
        }
    }


    public Message(){};

    public static Message fromJson(JSONObject jsonObject, String AES) throws Exception{
        Message message = new Message();
        message.content = Encrypter.AESDecrypt(AES, jsonObject.getString(""));
        return message;
    }



    public static enum MSGTYPE{
        TEXT(0), AES(1);

        private int value;

        private MSGTYPE(int value){
            this.value = value;
        }
    }

    public static String millis2time(long millis){
        String min, hour;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        min = Integer.toString(cal.get(Calendar.MINUTE));
        hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
        if(min.length() == 1)
            min = "0" + min;
        if(hour.length() == 1)
            hour = "0" + hour;
        return hour + ":" + min;
    }
}
