package project.unitato.encrypchat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivitySwipe extends FragmentActivity {
    EditText msgEt;
    ChatList adapter;
    Button sendBtn;
    Socket echoSocket;
    boolean needNewAes = false;
    boolean activityKilled = false;
    String nextMsg = "";
    String partnerPk = "";
    JSONObject nextMsgJson;
    boolean sendMsg = false;
    boolean activityActive = true;
    String targetNumber;
    String sourceNumber;
    int targetDrawable;
    int sourceDrawable;
    ArrayList<String> web;
    ArrayList<Integer> imageId;
    ArrayList<String> times;
    Encrypter encrypter;
    String currentMsgsPrefs = "";
    Vibrator vibrator;
    String HASHTAG_STARTER, HASHTAG_ENDER, USERTAG_STARTER, USERTAG_ENDER;
    ViewPager viewPager;
    ArrayList<String> numbers;
    CollectionPagerAdapter collectionPagerAdapter;
    SharedPreferences prefs;
    final int MAX_LOAD = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_swipe);
        prefs = getSharedPreferences(eConstants.PREFERENCES_FILE, 0);
        sourceNumber = prefs.getString(eConstants.PREFS_PHONE_NUMEBR, "");
        sourceDrawable = eConstants.getPpByNumber(sourceNumber);
        targetNumber = getIntent().getExtras().get(eConstants.EXTRA_TARGET_NUMBER).toString();
        setTitle(eConstants.getContactByNumber(targetNumber));
        targetDrawable = eConstants.getPpByNumber(targetNumber);
        if(prefs.getString(targetNumber + "AES", "").equals(""))
        {
            encrypter = new Encrypter();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(targetNumber + "AES", encrypter.getAesKey());
            editor.commit();
            needNewAes = true;

        }else{
            encrypter = new Encrypter(prefs.getString(targetNumber + "AES", ""));
        }
        viewPager = (ViewPager) findViewById(R.id.pager);
        msgEt = (EditText) findViewById(R.id.msg_et);
        sendBtn = (Button) findViewById(R.id.button_sendMsg);
        numbers = new ArrayList<String>();
        String numsStr = prefs.getString(eConstants.PREFS_CHATS, "");
        String number = "";
        for(int i = 0; i < numsStr.length(); i++)
            if(numsStr.charAt(i) == ';')
            {
                numbers.add(eConstants.getContactByNumber(number));
                number = "";
            }else
                number += numsStr.charAt(i);
        adapter = new ChatList(this, web, imageId, times, Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf"), 20);
        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager(), adapter);
        viewPager.setAdapter(collectionPagerAdapter);
        viewPager.setCurrentItem(numbers.indexOf(targetNumber));
        viewPager.setOffscreenPageLimit(0);
        msgEt.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf"));
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        nextMsgJson = new JSONObject();
        try {
            nextMsgJson.put(eConstants.JSON_MSG_FROM, sourceNumber);
            nextMsgJson.put(eConstants.JSON_MSG_TO, targetNumber);
            nextMsgJson.put(eConstants.JSON_MSG_TIME, 0);
            nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETMSG);
        }catch (Exception e){
            e.printStackTrace();
        }




        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                targetNumber = numbers.get(position);
                targetDrawable = eConstants.getPpByNumber(targetNumber);
                setTitle(eConstants.getContactByNumber(targetNumber));
                encrypter.setAESKey(prefs.getString(targetNumber + "AES", ""));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }


    @Override
    protected void onDestroy() {
        activityKilled = true;
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        activityActive = true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        activityActive = false;
    }


    private void sendMsg(){
        String message = msgEt.getText().toString();
        if(message.length() == 0)
            return;
        imageId.add(sourceDrawable);
        Calendar calendar = Calendar.getInstance();
        times.add(millis2time(calendar.getTimeInMillis()));
        adapter.notifyDataSetChanged();
        if(message.length() < 6 || !message.substring(0, 6).equals("<html>")) {
            message = message.replaceAll("\n", "<br>");
            boolean encountered = false;
            for (int i = 0; i < message.length(); i++) {
                if (message.charAt(i) == '*') {
                    if (encountered)
                        message = message.replaceFirst("\\*", "</b>");
                    else
                        message = message.replaceFirst("\\*", "<b>");
                    encountered = !encountered;
                } else if (message.charAt(i) == '@') {
                    if (i == 0 || message.charAt(i - 1) == ' ') {
                        int j;
                        for (j = i; j < message.length() && message.charAt(j) != ' '; j++) ;
                        String facebookUser = message.substring(i + 1, j);
                        String replacement = "<a href=\"" + USERTAG_STARTER + facebookUser + USERTAG_ENDER + "\">@" + facebookUser + "</a>";
                        message = message.substring(0, i) + replacement + message.substring(i + facebookUser.length() + 1);
                        i += replacement.length();
                    } else if (i > 0 && message.charAt(i - 1) != ' ') {
                        int j1, j2;
                        boolean encounteredDot = false;
                        for (j1 = i; j1 >= 0 && message.charAt(j1) != ' '; j1--) ;
                        for (j2 = i; j2 < message.length() && message.charAt(j2) != ' '; j2++)
                            if (message.charAt(j2) == '.')
                                encounteredDot = true;
                        if (encounteredDot) {
                            j1++;
                            String email = message.substring(j1, j2);
                            String replacement = "<a href=\"mailto:" + email + "\">" + email + "</a>";
                            message = message.substring(0, j1) + replacement + message.substring(j2);
                            i = j1 + replacement.length();
                        } else if (message.substring(i + 1, j2).toLowerCase().equals("gmail")) {
                            j1++;
                            String email = message.substring(j1, j2) + ".com";
                            String replacement = "<a href=\"mailto:" + email + "\">" + email + "</a>";
                            message = message.substring(0, j1) + replacement + message.substring(j2);
                            i = j1 + replacement.length();
                        }
                    }
                } else if (message.charAt(i) == '#' && (i == 0 || message.charAt(i - 1) == ' ')) {
                    int j;
                    for (j = i; j < message.length() && message.charAt(j) != ' '; j++) ;
                    String hashtag = message.substring(i + 1, j);
                    String replacement = "<a href=\"" + HASHTAG_STARTER + hashtag + HASHTAG_ENDER + "\">#" + hashtag + "</a>";
                    message = message.substring(0, i) + replacement + message.substring(i + hashtag.length() + 1);
                    i += replacement.length();
                } else if ((i < message.length() - 7 && message.substring(i, i + 7).equals("http://")) || (i < message.length() - 8 && message.substring(i, i + 8).equals("https://"))) {
                    int startIndx = i;
                    int endIndx;
                    for (endIndx = startIndx; endIndx < message.length() && message.charAt(endIndx) != ' '; endIndx++)
                        ;
                    String url = message.substring(startIndx, endIndx);
                    String replacement = "<a href=\"" + url + "\">" + url + "</a>";
                    message = message.substring(0, i) + replacement + message.substring(i + url.length());
                    i += replacement.length();
                } else if (i < message.length() - 3 && message.charAt(i) == '.' && message.charAt(i + 1) != ' ') {
                    if (message.substring(i, i + 4).equals(".com")
                            || message.substring(i, i + 3).equals(".co")
                            || message.substring(i, i + 4).equals(".org")
                            || message.substring(i, i + 4).equals(".net")
                            || message.substring(i, i + 4).equals(".gov")) {
                        int j1, j2;
                        for (j1 = i; j1 >= 0 && message.charAt(j1) != ' '; j1--) ;
                        for (j2 = i; j2 < message.length() && message.charAt(j2) != ' '; j2++) ;
                        j1++;
                        String url2replace = message.substring(j1, j2);
                        String replacement = "<a href=\"http://" + url2replace + "\">" + url2replace + "</a>";
                        message = message.substring(0, j1) + replacement + message.substring(j2);
                        i = j1 + replacement.length();
                        Log.i("", "");
                    }
                }
            }
        }
        web.add(message);
        String encryptedMessage = encrypter.AESEncrypt(message);
        nextMsg = message;
        try {
            nextMsgJson.put(eConstants.JSON_MSG_CONTENT, encryptedMessage);
            nextMsgJson.put(eConstants.JSON_MSG_TYPE, eConstants.MSGTYPE_TEXT);
            nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_SENDMSG);
            nextMsgJson.put(eConstants.JSON_MSG_TIME, calendar.getTimeInMillis());
            sendMsg = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        msgEt.setText("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_showaes:
                AlertDialog.Builder aesDialogBuilder = new AlertDialog.Builder(ChatActivitySwipe.this);
                aesDialogBuilder.setTitle("AES Key");
                if(partnerPk.equals(""))
                    aesDialogBuilder.setMessage("Conversation AES: " + encrypter.getAesKey());
                else
                    aesDialogBuilder.setMessage("Conversation AES:\n" + encrypter.getAesKey() + "\n\n\nPartner public RSA:\n" + partnerPk);
                aesDialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                aesDialogBuilder.setNegativeButton("Regenerate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        needNewAes = true;
                    }
                });
                AlertDialog aesDialog = aesDialogBuilder.create();
                aesDialog.show();
                break;

            case R.id.action_delete_history:
                currentMsgsPrefs = "";
                getSharedPreferences(eConstants.PREFERENCES_FILE, 0).edit().putString(targetNumber + "MSGS", "").commit();
                web.clear();
                times.clear();
                imageId.clear();
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_help:
                Intent i2 = new Intent(ChatActivitySwipe.this, HelpActivity.class);
                startActivity(i2);
                break;
        }
        return super.onOptionsItemSelected(item);
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






    public class CollectionPagerAdapter extends FragmentStatePagerAdapter {



        public CollectionPagerAdapter(FragmentManager fm, ChatList chatAdapter) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ObjectFragment();
            Bundle args = new Bundle();
            args.putString(ObjectFragment.ARG_NUMBER, numbers.get(i));
            //args.putStringArrayList();
            Log.d("TAG", "getItem");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            ObjectFragment f = (ObjectFragment) object;
            if(f != null)
                f.update();
            Log.d("TAG", "getItemPosition");
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return numbers.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position);
        }
    }

    public class ObjectFragment extends Fragment{


        ArrayList<String> currentWeb;
        ArrayList<String> currentTimes;
        ArrayList<Integer> currentImageId;
        ChatList currentAdapter;
        
        public static final String ARG_NUMBER = "phone_number";
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            String currentNumber = eConstants.getNumberByName(args.getString(ARG_NUMBER));
            currentWeb = new ArrayList<String>();
            currentImageId = new ArrayList<Integer>();
            currentTimes = new ArrayList<String>();
            currentAdapter = new ChatList(ChatActivitySwipe.this, currentWeb, currentImageId, currentTimes, Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf"), 25);
            currentMsgsPrefs = prefs.getString(currentNumber + "MSGS", "");
            for(int i = currentMsgsPrefs.length()-1, total = 0; i >0 && total < MAX_LOAD;)
            {
                total++;
                String currentJson = "";
                int j;
                for(j = i; currentMsgsPrefs.charAt(j) != '{'; j--);
                currentJson = currentMsgsPrefs.substring(j,++i);
                try {
                    JSONObject msgJson = new JSONObject(currentJson);
                    currentWeb.add(msgJson.getString(eConstants.JSON_MSG_CONTENT));
                    currentTimes.add(millis2time(msgJson.getLong(eConstants.JSON_MSG_TIME)));
                    if(msgJson.getString(eConstants.JSON_MSG_FROM).equals(currentNumber))
                        currentImageId.add(targetDrawable);
                    else
                        currentImageId.add(sourceDrawable);
                }catch (JSONException e){
                    e.printStackTrace();
                    Log.e("JSON", "Err reading json in msgs");
                }
                i = j-1;
            }
            if(!currentWeb.isEmpty()) {
                Collections.reverse(currentWeb);
                Collections.reverse(currentImageId);
                Collections.reverse(currentTimes);
                currentAdapter.notifyDataSetChanged();
            } View rootView = inflater.inflate(R.layout.fragment_chat_swipe, container, false);
            ListView listView = (ListView) rootView.findViewById(R.id.list);
            listView.setAdapter(currentAdapter);
            encrypter.setAESKey(prefs.getString(currentNumber + "AES", ""));
            return rootView;
        }



        public void update(){

        }
    }





    private class mTask2 extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... data) {
            establishConnection();
            SharedPreferences prefs = getSharedPreferences(eConstants.PREFERENCES_FILE, 0);
            SharedPreferences.Editor editor = prefs.edit();
            ArrayList<String> allnums = new ArrayList<String>();
            String allnumsStr = prefs.getString(eConstants.PREFS_CHATS, "");
            String number = "";
            for(int i = 0; i < allnumsStr.length(); i++) {
                if (allnumsStr.charAt(i) == ';') {
                    allnums.add(ChatsActivity.filterNumber(number));
                    number = "";
                } else
                    number += allnumsStr.charAt(i);
            }


            int x = 0;
            while(x < 1) {
                if(activityKilled)
                    try {
                        echoSocket.close();
                        break;
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                try {
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                    out.println(nextMsgJson.toString());
                    if(sendMsg || nextMsgJson.getInt(eConstants.JSON_SERVER_REQTYPE) != eConstants.REQTYPE_GETMSG)
                    {                               //ChatActivity-only
                        nextMsgJson.put(eConstants.JSON_MSG_CONTENT, encrypter.AESDecrypt(nextMsgJson.getString(eConstants.JSON_MSG_CONTENT)));
                        currentMsgsPrefs += nextMsgJson.toString();
                        editor.putString(targetNumber + "MSGS", currentMsgsPrefs);
                        editor.commit();
                        sendMsg = false;
                        nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETMSG);
                    }

                    String response = in.readLine();
                    if(!response.equals("0") && response.charAt(0) == '{') {    //NEW MSG RECEIVED
                        JSONObject msg = new JSONObject(response);
                        String from = msg.getString(eConstants.JSON_MSG_FROM);
                        int messageType = msg.getInt(eConstants.JSON_MSG_TYPE);
                        String message = msg.getString(eConstants.JSON_MSG_CONTENT);
                        long time_t = msg.getLong(eConstants.JSON_MSG_TIME);
                        switch (messageType)
                        {
                            case eConstants.MSGTYPE_TEXT:
                                if(prefs.getBoolean(eConstants.PREFS_SOUND, true))
                                    playSound();
                                if(prefs.getBoolean(eConstants.PREFS_VIBRATION, true))
                                    vibrator.vibrate(20);
                                if(from.equals(targetNumber)) {
                                    String decryptedMsg = encrypter.AESDecrypt(message);
                                    String[] params1 = {decryptedMsg, millis2time(time_t)};
                                    publishProgress(params1);
                                    msg.put(eConstants.JSON_MSG_CONTENT, decryptedMsg);
                                    currentMsgsPrefs += msg.toString();
                                    editor.putString(from + "MSGS", currentMsgsPrefs);
                                    editor.commit();
                                    if (!activityActive)
                                        showNotification(from, decryptedMsg);
                                }else{
                                    if(!allnums.contains(from))
                                    {
                                        allnums.add(from);
                                        allnumsStr += from + ";";
                                        editor.putString(eConstants.PREFS_CHATS, allnumsStr);
                                        editor.commit();
                                    }
                                    String decryptedMsg = Encrypter.AESDecrypt(prefs.getString(from+"AES",""),message);
                                    msg.put(eConstants.JSON_MSG_CONTENT, decryptedMsg);
                                    String msgsPrefs = prefs.getString(from + "MSGS", "");
                                    msgsPrefs += msg.toString();
                                    editor.putString(from + "MSGS", msgsPrefs);
                                    editor.commit();
                                    if (!activityActive)
                                        showNotification(from, decryptedMsg);
                                }
                                break;
                            case eConstants.MSGTYPE_AES:
                                if(from.equals(targetNumber)) {
                                    String privateKey = prefs.getString(eConstants.PREFS_PRIVATE_KEY, "");
                                    String aes = Encrypter.RSADecrypt(message, privateKey);
                                    if (aes.length() == 16) {
                                        encrypter.setAESKey(aes);
                                        editor.putString(from + "AES", aes);
                                        editor.commit();
                                        String[] params2 = {"TOAST", "AES key updated by partner " + from};
                                        publishProgress(params2);
                                    }
                                }else{
                                    String privateKey = prefs.getString(eConstants.PREFS_PRIVATE_KEY, "");
                                    String aes = Encrypter.RSADecrypt(message, privateKey);
                                    if (aes.length() == 16) {
                                        editor.putString(from + "AES", aes);
                                        editor.commit();
                                    }
                                }
                                break;
                        }
                    }
                    if(needNewAes){
                        needNewAes = false;
                        String[] progressParams = {"DIALOG","Generating a new AES key..."};
                        publishProgress(progressParams);
                        encrypter = new Encrypter();
                        progressParams[0] = "AES";
                        progressParams[1] = "Saving new AES key...";
                        publishProgress(progressParams);
                        editor.putString(targetNumber + "AES", encrypter.getAesKey());
                        editor.commit();
                        progressParams[1] = "Getting partner's public RSA key...";
                        publishProgress(progressParams);
                        nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETPK);
                        out.println(nextMsgJson.toString());
                        String pk = in.readLine();
                        partnerPk = pk;
                        progressParams[1] = "Encrypting AES key...";
                        publishProgress(progressParams);
                        String encryptedAes = Encrypter.RSAEncrypt(encrypter.getAesKey(), pk);

                        progressParams[1] = "Sending encrypted AES key...";
                        publishProgress(progressParams);
                        nextMsgJson.put(eConstants.JSON_MSG_CONTENT, encryptedAes);
                        nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_SENDMSG);
                        nextMsgJson.put(eConstants.JSON_MSG_TYPE, eConstants.MSGTYPE_AES);
                        out.println(nextMsgJson.toString());
                        in.readLine();

                        progressParams[1] = "Done. Flushing...";
                        publishProgress(progressParams);
                        nextMsgJson.put(eConstants.JSON_MSG_CONTENT, "");
                        nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETMSG);
                        nextMsgJson.put(eConstants.JSON_MSG_TYPE, eConstants.MSGTYPE_TEXT);

                        publishProgress("DONE");
                    }
                    Thread.sleep(150);
                } catch (IOException e){ //NO CONNECTION
                    e.printStackTrace();
                    String[] params = {"TOAST", "CONNECTION ERROR"};
                    publishProgress("CON_ERR");
                    establishConnection();
                } catch (JSONException e){

                } catch (Exception e){

                }

            }
            return null;
        }


        private void establishConnection()
        {
            try {
                echoSocket = new Socket(eConstants.SOCKET_HOST, eConstants.SOCKET_PORT);
            }catch (IOException e){
                e.printStackTrace();
                String[] params = {"TOAST", "CONNECTION ERROR"};
                publishProgress("CON_ERR");
                try {
                    Thread.sleep(500);
                }catch (InterruptedException e1){

                }
                establishConnection();
            }finally {
                publishProgress("CON_OK");
            }
        }

        ProgressDialog dialog;

        @Override
        protected void onProgressUpdate(String... msg) {
            super.onProgressUpdate(msg);
            if (msg[0].equals("0"))
                return;
            else if (msg[0].equals("DIALOG")) {
                dialog = new ProgressDialog(ChatActivitySwipe.this);
                dialog.setTitle("Changing AES Key...");
                dialog.setMessage(msg[1]);
                dialog.setCancelable(false);
                dialog.show();
            } else if (msg[0].equals("AES")) {
                dialog.setMessage(msg[1]);
            } else if (msg[0].equals("DONE")) {
                dialog.dismiss();
            } else if (msg[0].equals("TOAST")) {
                Toast.makeText(ChatActivitySwipe.this, msg[1], Toast.LENGTH_SHORT).show();
            } else if (msg[0].equals("CON_ERR")) {
                if(sendBtn.isClickable()) {
                    sendBtn.setClickable(false);
                    sendBtn.setAlpha(0.7f);
                    Toast.makeText(ChatActivitySwipe.this, "Connection error", Toast.LENGTH_LONG).show();
                }
            } else if (msg[0].equals("CON_OK")) {
                sendBtn.setClickable(true);
                sendBtn.setAlpha(1f);
            } else if (msg[0].length() > 0 && msg.length == 2) {

                web.add(msg[0]);
                times.add(msg[1]);
                imageId.add(targetDrawable);
                adapter.notifyDataSetChanged();
            }
        }



        public void showNotification(String from, String message){
            Intent intent = new Intent(ChatActivitySwipe.this, ChatsActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(ChatActivitySwipe.this, 0, intent, 0);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ChatActivitySwipe.this)
                            .setSmallIcon(eConstants.getPpByNumber(from))
                            .setContentTitle(eConstants.getContactByNumber(from))
                            .setContentText(Html.fromHtml(message))
                            .setContentIntent(pIntent);
            Notification mNotification = mBuilder.build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(0, mNotification);
        }


        private void playSound()
        {
            try {
                Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            web.add(data);
            imageId.add(targetDrawable);
            adapter.notifyDataSetChanged();
        }
    }


}
