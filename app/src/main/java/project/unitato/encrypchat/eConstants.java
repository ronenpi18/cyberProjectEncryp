package project.unitato.encrypchat;


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

    public static final String WEB_HOME = "http://encrypchat.bl.ee/";
    public static final String EXTRA_TARGET_NUMBER = "target_number";
    public static final String EXTRA_SELF_NUMBER = "number";

    public static final String PN_DENIS = "0508997057";
    public static final String PN_ROMAN = "";

    public static String getContactByNumber(String number)
    {
        Map<String, String> numbers = new HashMap<String, String>(){{ //TODO
            put("0524790022", "Roman");
            put("0508997057", "Denis");
            put("0526226472", "Sean");
            put("0508742277", "Noam");
            put(null, "Valery");
        }};
        if(numbers.containsKey(number))
            return numbers.get(number);
        else
            return null;
    }


    public static int getPpByNumber(String number){
        Map<String, Integer> pics = new HashMap<String, Integer>() {{ //TODO
            put("0524790022", R.drawable.pp_roman);
            put("0508997057", R.drawable.pp_denis);
            put("0526226472", R.drawable.pp_sean);
            put("0508742277", -1);
            put("", -1);
        }};
        if(pics.containsKey(number))
            return pics.get(number);
        else
            return -1;
    }


    public static String getNumberByName(String name){

        Map<String, String> people = new HashMap<String, String>(){{ //TODO
            put("Roman", "0524790022");
            put("Denis", "0508997057");
            put("Sean", "0526226472");
            put("Noam", "0508742277");
            put("Valery", "");
        }};
        if(people.containsKey(name))
            return people.get(name);
        else
            return null;
    }
}
