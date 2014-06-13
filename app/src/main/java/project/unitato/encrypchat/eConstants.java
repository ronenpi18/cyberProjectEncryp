package project.unitato.encrypchat;


import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class eConstants {
    public static final String SOCKET_HOST = "31.220.49.240";
    public static final int SOCKET_PORT = 47395;

    public static final String JSON_SERVER_REQTYPE = "reqtype";
    public static final String JSON_MSG_TYPE = "type";
    public static final String JSON_MSG_CONTENT = "content";
    public static final String JSON_MSG_FROM = "from";
    public static final String JSON_MSG_TO = "to";
    public static final String JSON_MSG_TIME = "time";

    public static final int REQTYPE_SENDMSG = 0;
    public static final int REQTYPE_GETMSG = 1;
    public static final int REQTYPE_REGISTER = 2;
    public static final int REQTYPE_GETPK = 3;

    public static final int MSGTYPE_TEXT = 0;
    public static final int MSGTYPE_AES = 1;

    public static final String PARAM2_ADDMSG = "addmsg";

    public static final String PREFS_PRIVATE_KEY = "private_key";
    public static final String PREFERENCES_FILE = "prefs";
    public static final String PREFS_PHONE_NUMEBR = "phone_number";
    public static final String PREFS_CHATS = "open_chats";
    public static final String PREFS_SOUND = "sound";
    public static final String PREFS_VIBRATION = "vibration";
    public static final String PREFS_SOCIAL_USERTAG_STARTER = "usertag_network_starter";
    public static final String PREFS_SOCIAL_HASHTAG_STARTER = "hashtag_network_starter";
    public static final String PREFS_SOCIAL_USERTAG_ENDER = "usertag_network_ender";
    public static final String PREFS_SOCIAL_HASHTAG_ENDER = "hashtag_network_ender";

    public static final String WEB_HOME = "http://encrypchat.bl.ee/";
    public static final String EXTRA_TARGET_NUMBER = "target_number";
    public static final String EXTRA_SELF_NUMBER = "number";


    public static final String USERTAG_FACEBOOK_STARTER = "https://www.facebook.com/";
    public static final String USERTAG_TWITTER_STARTER = "https://twitter.com/";
    public static final String USERTAG_YOUTUBE_STARTER = "https://www.youtube.com/user/";
    public static final String USERTAG_TUMBLR_STARTER = "http://";

    public static final String USERTAG_FACEBOOK_ENDER = "";
    public static final String USERTAG_TWITTER_ENDER = "";
    public static final String USERTAG_YOUTUBE_ENDER = "";
    public static final String USERTAG_TUMBLR_ENDER = ".tumblr.com";

    public static final String HASHTAG_FACEBOOK_STARTER = "https://www.facebook.com/hashtag/";
    public static final String HASHTAG_TWITTER_STARTER = "https://twitter.com/hashtag/";
    public static final String HASHTAG_GOOGLEPLUS_STARTER = "https://plus.google.com/explore/";
    public static final String HASHTAG_TUMBLR_STARTER = "https://www.tumblr.com/tagged/";

    public static final String HASHTAG_FACEBOOK_ENDER = "";
    public static final String HASHTAG_TWITTER_ENDER = "?src=hash";
    public static final String HASHTAG_GOOGLEPLUS_ENDER = "";
    public static final String HASHTAG_TUMBLR_ENDER = "";


    public static String getContactByNumber(String number)
    {
        Map<String, String> numbers = new HashMap<String, String>(){{ //TODO
            put("972524790022", "Roman");
            put("972508997057", "Denis");
            put("972526226472", "Sean");
            put("972508742277", "Noam");
            put("972542203329", "Valery");
            put("972504789654", "Mr. Debug");
            put("972506789654", "Mrs. Debug");
        }};
        if(numbers.containsKey(number))
            return numbers.get(number);
        else
            return number;
    }


    public static int getPpByNumber(String number){
        Map<String, Integer> pics = new HashMap<String, Integer>() {{ //TODO
            put("972524790022", R.drawable.pp_roman);
            put("972508997057", R.drawable.pp_denis);
            put("972526226472", R.drawable.pp_sean);
            put("972508742277", R.drawable.night_blur);
            put("972542203329", R.drawable.pp_valery);
            put("972504789654", R.drawable.pp_debug);
            put("972506789654", R.drawable.pp_debug);
        }};
        if(pics.containsKey(number))
            return pics.get(number);
        else
            return R.drawable.night_blur;
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






    public static String getNumberByName(String name){

        Map<String, String> people = new HashMap<String, String>(){{ //TODO
            put("Roman", "972524790022");
            put("Denis", "972508997057");
            put("Sean", "972526226472");
            put("Noam", "972508742277");
            put("Valery", "972542203329");
            put("Mr. Debug", "972504789654");
            put("Mrs. Debug", "972506789654");
        }};
        if(people.containsKey(name))
            return people.get(name);
        else
            return name;
    }
}
