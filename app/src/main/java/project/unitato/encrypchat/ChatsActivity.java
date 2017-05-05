package project.unitato.encrypchat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class ChatsActivity extends ActionBarActivity {

    private Socket socket;
    String phoneNumber;
    int RESULT_LOGIN = 0;
    int RESULT_CONTACT = 1;
    Vibrator vibrator;
    ListView list;
    ChatsList adapter;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    ArrayList<String> web;
    ArrayList<String> lastMsgs;
    ArrayList<Integer> imageId;
    Socket echoSocket;
    boolean activityKilled = false;
    boolean activityActive = true;
    boolean newChat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(eConstants.PREFERENCES_FILE, 0);
        editor = prefs.edit();
        Encrypter.startSampleTransmission();
        //Check if user haven't registered yet
        if(prefs.getString(eConstants.PREFS_PHONE_NUMEBR, "").equals(""))
        {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, RESULT_LOGIN);
        }else
        {
            phoneNumber = prefs.getString(eConstants.PREFS_PHONE_NUMEBR, "0");
            setTitle("Your phone is: " + phoneNumber.substring(0,3) + "-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6));
            new mTask2().execute();
        }

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        web = new ArrayList<String>();
        imageId = new ArrayList<Integer>();
        lastMsgs = new ArrayList<String>();

        if(prefs.getString(eConstants.PREFS_CHATS, "").equals(""))
        {
            web.add("NO CHATS");
            imageId.add(R.drawable.night_blur);
            lastMsgs.add("Tap '+'");

        }else{
            String data = prefs.getString(eConstants.PREFS_CHATS, "");
            String number = "";
            for(int i = 0; i < data.length(); i++)
                if(data.charAt(i) == ';')
                {
                    int pp = eConstants.getPpByNumber(filterNumber(number));
                    if(pp == -1) {
                        web.add(number);
                        imageId.add(R.drawable.night_blur);
                    }else{
                        web.add(eConstants.getContactByNumber(filterNumber(number)));
                        imageId.add(pp);
                    }
                    number = "";
                }else
                    number += data.charAt(i);
            //Get last msg
            for(int i = 0; i < web.size(); i++)
            {
                String allmsgs = prefs.getString(eConstants.getNumberByName((String)web.toArray()[i]) + "MSGS", "");
                if(allmsgs.length() > 0) {
                    int j;
                    for (j = allmsgs.length() - 1; allmsgs.charAt(j) != '{'; j--) ;
                    try {
                        JSONObject lastMsgJson = new JSONObject(allmsgs.substring(j));
                        lastMsgs.add(lastMsgJson.getString(eConstants.JSON_MSG_CONTENT));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    lastMsgs.add("");
                }
            }
        }


        setContentView(R.layout.activity_chats);
        Typeface msgTypeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        adapter = new ChatsList(ChatsActivity.this, web, imageId, lastMsgs, msgTypeface, 40);
        list = (ListView)findViewById(R.id.allchats_list);
        list.setAdapter(adapter);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                beginChat(web.get(i).toString());
            }
        });


        setTitle("Encrypchat");

    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode != KeyEvent.KEYCODE_BACK)
            return super.onKeyUp(keyCode, event);
        else
            return super.onKeyUp(KeyEvent.KEYCODE_HOME, event);
    }

    class ClientThread implements Runnable {

        public void runs() {
            NetClient nc = new NetClient(eConstants.SOCKET_HOST, eConstants.SOCKET_PORT);
            nc.sendDataWithString("YELLO NIGGLET");
            Log.i("RESPONSE!", nc.receiveDataFromServer());
        }

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(eConstants.SOCKET_HOST);

                socket = new Socket(serverAddr, eConstants.SOCKET_PORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true
                );
                //out.println("HELLO WORLD");
            } catch (Exception e) {

            }


        }

    }


    /**
     * Refresher
     */
    private void refreshLastMsgs()
    {
        if(web.contains("NO CHATS") && web.size() > 1)
        {
            web.remove(0);
            imageId.remove(0);
            lastMsgs.remove(0);
        }
        for(int i = 0; i < web.size(); i++)
        {
            String allmsgs = prefs.getString(eConstants.getNumberByName((String)web.toArray()[i]) + "MSGS", "");
            if(allmsgs.length() > 0) {
                int j;
                for (j = allmsgs.length() - 1; allmsgs.charAt(j) != '{'; j--) ;
                try {
                    JSONObject lastMsgJson = new JSONObject(allmsgs.substring(j));
                    if(i < lastMsgs.size())
                        lastMsgs.set(i, lastMsgJson.getString(eConstants.JSON_MSG_CONTENT));
                    else
                        lastMsgs.add(lastMsgJson.getString(eConstants.JSON_MSG_CONTENT));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                if(i < lastMsgs.size())
                    lastMsgs.set(i, "");
                else
                    lastMsgs.add("");
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * checks the result of login && result of contacts
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOGIN) {
            phoneNumber = data.getExtras().getString(eConstants.PREFS_PHONE_NUMEBR);
            editor.putString(eConstants.PREFS_PHONE_NUMEBR, phoneNumber);
            editor.commit();
            new mTask2().execute();
            Intent i2 = new Intent(ChatsActivity.this, HelpActivity.class);
            startActivity(i2);
        }else if(requestCode == RESULT_CONTACT){
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String number = "";
                if (uri != null) {
                    Cursor c = null;
                    try {
                        c = getContentResolver().query(uri, new String[]{
                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.TYPE },
                                null, null, null);
                        if (c != null && c.moveToFirst()) {
                            number = c.getString(0);
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                        if(number != null) {
                            if(web.contains("NO CHATS"))
                            {
                                int indx = web.indexOf("NO CHATS");
                                web.remove(indx);
                                imageId.remove(indx);
                                lastMsgs.remove(indx);
                            }
                            if(!web.contains(eConstants.getContactByNumber(filterNumber(number)))) {
                                number = filterNumber(number);
                                web.add(eConstants.getContactByNumber(number));
                                imageId.add(eConstants.getPpByNumber(number));
                                lastMsgs.add("");
                                String allchats = prefs.getString(eConstants.PREFS_CHATS, "");
                                allchats += filterNumber(number) + ";";
                                editor.putString(eConstants.PREFS_CHATS, allchats);
                                editor.commit();
                                adapter.notifyDataSetChanged();
                            }
                            newChat = true;
                            beginChat(filterNumber(number));
                        }
                    }
                }
            }
        }
    }


    private void beginChat(String number)
    {
        activityKilled = true;
        Intent i = new Intent(ChatsActivity.this, ChatActivity.class);
        if((number.charAt(0) >= '0' && number.charAt(0) <= '9') || number.charAt(0) == '(')
            i.putExtra(eConstants.EXTRA_TARGET_NUMBER, filterNumber(number));
        else
            i.putExtra(eConstants.EXTRA_TARGET_NUMBER, number);
        startActivity(i);
        overridePendingTransition(R.anim.activity_enter_l2r,R.anim.activity_leave_l2r);
    }

    public static String filterNumber(String number)
    {
        String filteredNumber = "";
        for(int i = 0; i < number.length(); i++)
            if(number.charAt(i) <= '9' && number.charAt(i) >= '0')
                filteredNumber += number.charAt(i);
        if(filteredNumber.charAt(0) == '0')
            filteredNumber = "972" + filteredNumber.substring(1);
        return filteredNumber;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(activityKilled)
            overridePendingTransition(R.anim.activity_enter_r2l, R.anim.activity_leave_r2l);
        activityActive = true;
        if(activityKilled && !newChat) {
            activityKilled = false;
            new mTask2().execute();
            String allnumsStr = prefs.getString(eConstants.PREFS_CHATS, "");
            String number = "";
            for(int i = 0; i < allnumsStr.length(); i++) {
                if (allnumsStr.charAt(i) == ';') {
                    if(!web.contains(eConstants.getContactByNumber(number))) {
                        web.add(eConstants.getContactByNumber(number));
                        imageId.add(eConstants.getPpByNumber(number));
                        String allmsgs = prefs.getString(number + "MSGS", "");
                        if(allmsgs.length() > 0) {
                            int j;
                            for (j = allmsgs.length() - 1; allmsgs.charAt(j) != '{'; j--) ;
                            try {
                                JSONObject lastMsgJson = new JSONObject(allmsgs.substring(j));
                                lastMsgs.add(lastMsgJson.getString(eConstants.JSON_MSG_CONTENT));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            lastMsgs.add("");
                        }
                    }
                    number = "";
                } else
                    number += allnumsStr.charAt(i);
            }
        }else if(newChat){
            newChat = false;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        activityActive = false;
    }

    /**
     * Async task which interacts with the server
     */
    private class mTask2 extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... data) {
            establishConnection();
            JSONObject getmsgJson = new JSONObject();
            String sourceNumber = phoneNumber;
            try {
                getmsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETMSG);
                getmsgJson.put(eConstants.JSON_MSG_FROM, sourceNumber);
            }catch (JSONException e){
                Log.e("JSON ERROR", "JSON ERROR");
                e.printStackTrace();
            }
            int x = 0;
            while(x < 1) {
                if(activityKilled) {
                    try {
                        echoSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                try {
                    publishProgress("");
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                    out.println(getmsgJson.toString());
                    String response = in.readLine();
                    if(!response.equals("0") && response.charAt(0) == '{') {    //NEW MSG RECEIVED
                        JSONObject msg = new JSONObject(response);
                        String from = msg.getString(eConstants.JSON_MSG_FROM);
                        int messageType = msg.getInt(eConstants.JSON_MSG_TYPE);
                        String message = msg.getString(eConstants.JSON_MSG_CONTENT);
                        String currentMsgsPrefs = prefs.getString(from + "MSGS", "");
                        switch (messageType)
                        {
                            case eConstants.MSGTYPE_TEXT:
                                Encrypter encrypter = new Encrypter(prefs.getString(from + "AES", ""));
                                String decryptedMsg = encrypter.AESDecrypt(message);
                                msg.put(eConstants.JSON_MSG_CONTENT, decryptedMsg);
                                currentMsgsPrefs += msg.toString();
                                editor.putString(from + "MSGS", currentMsgsPrefs);
                                editor.commit();
                                if(!web.contains(eConstants.getContactByNumber(from)))
                                {
                                    web.add(eConstants.getContactByNumber(from));
                                    imageId.add(eConstants.getPpByNumber(from));
                                    lastMsgs.add(decryptedMsg);
                                    String allchats = prefs.getString(eConstants.PREFS_CHATS, "");
                                    allchats += from + ";";
                                    editor.putString(eConstants.PREFS_CHATS, allchats);
                                    editor.commit();
                                    publishProgress("NOTIFY");
                                }
                                if(!activityActive)
                                    showNotification(from, decryptedMsg);
                                if(prefs.getBoolean(eConstants.PREFS_SOUND, true))
                                    playSound();
                                if(prefs.getBoolean(eConstants.PREFS_VIBRATION, true))
                                    vibrator.vibrate(20);
                                break;
                            case eConstants.MSGTYPE_AES:
                                String privateKey = prefs.getString(eConstants.PREFS_PRIVATE_KEY, "");
                                String aes = Encrypter.RSADecrypt(message, privateKey);
                                if(aes.length() == 16) {
                                    editor.putString(from + "AES", aes);
                                    editor.commit();
                                }
                                break;
                        }
                    }
                    Thread.sleep(150);
                } catch (IOException e){ //NO CONNECTION
                    e.printStackTrace();
                    establishConnection();
                } catch (JSONException e){

                } catch (Exception e){

                }

            }
            return null;
        }

        /**
         * Socket opener and establisher
         */
        private void establishConnection()
        {
            try {
                echoSocket = new Socket(eConstants.SOCKET_HOST, eConstants.SOCKET_PORT);
            }catch (IOException e){
                e.printStackTrace();
                String[] params = {"TOAST", "CONNECTION ERROR"};
                try {
                    Thread.sleep(500);
                }catch (InterruptedException e1){

                }
                establishConnection();
            }finally {
            }
        }

        @Override
        protected void onProgressUpdate(String... msg) {
            super.onProgressUpdate(msg);
            if (msg[0].equals("0"))
                return;
            else if(msg[0].equals("NOTIFY"))
                adapter.notifyDataSetChanged();
            refreshLastMsgs();
        }

        /**
         * Sets the notifications for incoming msgs
         * @param from
         * @param message
         */
        public void showNotification(String from, String message){
            Intent intent = new Intent(ChatsActivity.this, ChatsActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(ChatsActivity.this, 0, intent, 0);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ChatsActivity.this)
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
        }
    }









    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.allchats_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_new:
                // user BoD suggests using Intent.ACTION_PICK instead of .ACTION_GET_CONTENT to avoid the chooser
                Intent intent = new Intent(Intent.ACTION_PICK);
                // BoD con't: CONTENT_TYPE instead of CONTENT_ITEM_TYPE
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, RESULT_CONTACT);
                break;
            case R.id.action_reset:
                getSharedPreferences(eConstants.PREFERENCES_FILE, 0).edit().putString(eConstants.PREFS_PHONE_NUMEBR, "").commit();
                finish();
                break;
            case R.id.action_settings:
                Intent i1 = new Intent(ChatsActivity.this, SettingsActivity.class);
                startActivity(i1);
                break;
            case R.id.action_help:
                Intent i2 = new Intent(ChatsActivity.this, HelpActivity.class);
                startActivity(i2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
