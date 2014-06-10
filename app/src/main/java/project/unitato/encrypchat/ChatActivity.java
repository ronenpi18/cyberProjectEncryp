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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class ChatActivity extends Activity{
	ListView list;
	EditText msgEt;
	ChatList adapter;
	Button sendBtn;
    Socket echoSocket;
    boolean isNewChat;
    boolean needNewAes = false;
    boolean activityKilled = false;
    String nextMsg = "";
    JSONObject nextMsgJson;
    boolean sendMsg = false;
    String targetNumber;
    String sourceNumber;
    int targetDrawable;
    int sourceDrawable;
    public static String WEB_HOME = "http://encrypchat.bl.ee/";
	ArrayList<String> web;
	ArrayList<Integer> imageId;
    ArrayList<String> times;
    Encrypter encrypter;

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
          web = new ArrayList<String>();
          imageId = new ArrayList<Integer>();
          times = new ArrayList<String>();
          setContentView(R.layout.activity_chat);
          Typeface msgTypeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
          adapter = new ChatList(ChatActivity.this, web, imageId, times, msgTypeface, 20);
          list = (ListView)findViewById(R.id.list);
          msgEt = (EditText) findViewById(R.id.msg_et);
          sendBtn = (Button) findViewById(R.id.button_sendMsg);
          list.setAdapter(adapter);
          targetNumber = getIntent().getExtras().get(eConstants.EXTRA_TARGET_NUMBER).toString();    //FOR SOME REASON DOESN'T WORK
          sourceNumber = getIntent().getExtras().get(eConstants.EXTRA_SELF_NUMBER).toString();
          targetDrawable = R.drawable.night_blur;
          setTitle(targetNumber);
          String realnumber = eConstants.getNumberByName(targetNumber);
          if(realnumber != null) {
              targetDrawable = eConstants.getPpByNumber(realnumber);
              targetNumber = realnumber;
          }
          sourceDrawable = eConstants.getPpByNumber(sourceNumber);
          if(sourceDrawable == -1)
              sourceDrawable = R.drawable.icon;


          SharedPreferences prefs = getSharedPreferences(eConstants.PREFERENCES_FILE, 0);
          nextMsgJson = new JSONObject();
          try {
              nextMsgJson.put(eConstants.JSON_MSG_FROM, sourceNumber);
              nextMsgJson.put(eConstants.JSON_MSG_TO, targetNumber);
              nextMsgJson.put(eConstants.JSON_MSG_TIME, 0);
              nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETMSG);
          }catch (Exception e){
              e.printStackTrace();
          }
          if(prefs.getString(targetNumber + "AES", "").equals(""))
          {
              isNewChat = true;
              encrypter = new Encrypter();
              SharedPreferences.Editor editor = prefs.edit();
              editor.putString(targetNumber + "AES", encrypter.getAesKey());
              editor.commit();
          }else{
              isNewChat = false;
              encrypter = new Encrypter(prefs.getString(targetNumber + "AES", ""));
          }
          new mTask2().execute();



        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ChatActivity.this, encrypter.AESEncrypt(web.toArray()[+position].toString()), Toast.LENGTH_SHORT).show();
            }

        });
        
        
        sendBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                String message = msgEt.getText().toString();
				web.add(message);
                imageId.add(sourceDrawable);
                Calendar calendar = Calendar.getInstance();
                times.add(millis2time(calendar.getTimeInMillis()));
                adapter.notifyDataSetChanged();
                String encryptedMessage = encrypter.AESEncrypt(message);
               // WebSigner webSigner = new WebSigner();
               // webSigner.execute(WEB_HOME + "send.php?from=" + sourceNumber + "&to=" + targetNumber + "&message=" + encryptedMessage + "&time=" + System.currentTimeMillis());
                //nextMsg = "0" + encryptedMessage;
                nextMsg = msgEt.getText().toString();
                try {
                    nextMsgJson.put(eConstants.JSON_MSG_CONTENT, encrypter.AESEncrypt(nextMsg));
                    nextMsgJson.put(eConstants.JSON_MSG_TYPE, eConstants.MSGTYPE_TEXT);
                    nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_SENDMSG);
                    nextMsgJson.put(eConstants.JSON_MSG_TIME, calendar.getTimeInMillis());
                    sendMsg = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
                msgEt.setText("");


			}
		});

	  }


    @Override
    protected void onDestroy() {
        activityKilled = true;
        super.onDestroy();
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
                AlertDialog.Builder aesDialogBuilder = new AlertDialog.Builder(ChatActivity.this);
                aesDialogBuilder.setTitle("AES Key");
                aesDialogBuilder.setMessage(encrypter.getAesKey());
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







    private class mTask2 extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... data) {
            establishConnection();
            SharedPreferences prefs = getSharedPreferences(eConstants.PREFERENCES_FILE, 0);
            SharedPreferences.Editor editor = prefs.edit();
            if(isNewChat)
            {
                try {
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                    out.println(nextMsgJson.toString());
                    String prevJson = in.readLine();
                    if(prevJson.equals("0")) {
                        nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETPK);
                        out.println(nextMsgJson.toString());
                        String pk = in.readLine();
                        String encryptedAes = Encrypter.RSAEncrypt(encrypter.getAesKey(), pk);

                        nextMsgJson.put(eConstants.JSON_MSG_CONTENT, encryptedAes);
                        nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_SENDMSG);
                        nextMsgJson.put(eConstants.JSON_MSG_TYPE, eConstants.MSGTYPE_AES);
                        out.println(nextMsgJson.toString());
                        in.readLine();
                    }else{
                        JSONObject prevMsg = new JSONObject(prevJson);
                        String privateKey = getSharedPreferences(eConstants.PREFERENCES_FILE,0).getString(eConstants.PREFS_PRIVATE_KEY, "");
                        String aes = Encrypter.RSADecrypt(prevMsg.getString(eConstants.JSON_MSG_CONTENT), privateKey);
                        encrypter.setAESKey(aes);
                        editor.putString(targetNumber + "AES", aes);
                        editor.commit();
                    }

                    nextMsgJson.put(eConstants.JSON_MSG_CONTENT, "");
                    nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETMSG);
                    nextMsgJson.put(eConstants.JSON_MSG_TYPE, eConstants.MSGTYPE_TEXT);
                }catch (Exception e){
                    e.printStackTrace();
                }
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
                    {
                        sendMsg = false;
                        nextMsgJson.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_GETMSG);
                    }

                    String response = in.readLine();
                    if(!response.equals("0") && response.charAt(0) == '{') {    //NEW MSG RECEIVED
                        JSONObject msg = new JSONObject(response);
                        int responseType = msg.getInt(eConstants.JSON_SERVER_REQTYPE);
                        int messageType = msg.getInt(eConstants.JSON_MSG_TYPE);
                        String message = msg.getString(eConstants.JSON_MSG_CONTENT);
                        long time_t = msg.getLong(eConstants.JSON_MSG_TIME);
                        switch (messageType)
                        {
                            case eConstants.MSGTYPE_TEXT:
                                String[] params1 = {encrypter.AESDecrypt(message), millis2time(time_t)};
                                publishProgress(params1);

                                break;
                            case eConstants.MSGTYPE_AES:
                                String privateKey = prefs.getString(eConstants.PREFS_PRIVATE_KEY, "");
                                String aes = Encrypter.RSADecrypt(message, privateKey);
                                encrypter.setAESKey(aes);
                                editor.putString(targetNumber + "AES", aes);
                                editor.commit();
                                String[] params2 = {"TOAST", "AES key updated by partner"};
                                publishProgress(params2);
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
                }catch (Exception e){
                    e.printStackTrace();
                    establishConnection();
                }

            }
            return null;
        }


        private void establishConnection()
        {
            try {
                echoSocket = new Socket(eConstants.SOCKET_HOST, eConstants.SOCKET_PORT);
            }catch (Exception e){
                e.printStackTrace();
                establishConnection();
            }
        }

        ProgressDialog dialog;

        @Override
        protected void onProgressUpdate(String... msg) {
            super.onProgressUpdate(msg);
            if(msg[0].equals("0"))
                return;
            else if(msg[0].equals("DIALOG"))
            {
                dialog = new ProgressDialog(ChatActivity.this);
                dialog.setTitle("Changing AES Key...");
                dialog.setMessage(msg[1]);
                dialog.setCancelable(false);
                dialog.show();
            }else if(msg[0].equals("AES")) {
                dialog.setMessage(msg[1]);
            }else if(msg[0].equals("DONE")){
                dialog.dismiss();
            }else if(msg[0].equals("TOAST")){
                Toast.makeText(ChatActivity.this, msg[1], Toast.LENGTH_SHORT).show();
            } else if(msg[0].length() > 0 && msg.length == 2){
                web.add(msg[0]);
                times.add(msg[1]);
                imageId.add(targetDrawable);
                adapter.notifyDataSetChanged();
            }


            /*
            try {
                JSONObject data = new JSONObject(json[0]);
                int responseType = data.getInt(eConstants.JSON_SERVER_REQTYPE);
                int messageType = data.getInt(eConstants.JSON_MSG_TYPE);
                if(messageType == eConstants.MSGTYPE_TEXT)
                {
                    String message = data.getString(eConstants.JSON_MSG_CONTENT);
                    web.add(encrypter.AESDecrypt(message));
                    imageId.add(targetDrawable);
                    adapter.notifyDataSetChanged();
                }
            }catch (Exception e){
                e.printStackTrace();
            }*/




        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            web.add(data);
            imageId.add(targetDrawable);
            adapter.notifyDataSetChanged();
        }
    }

    private class mTask extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... data) {
            establishConnection();
        /*    if(isNewChat)
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    //Check if friend has tried to connect yet
                    out.println("1" + sourceNumber + targetNumber);
                    String msg = in.readLine();
                    if (msg.equals("0")) {
                        out.println("3" + targetNumber);
                        String pk = in.readLine();
                        String encryptedAes = Encrypter.RSAEncrypt(encrypter.getAesKey(), pk);
                        out.println("0" + targetNumber + sourceNumber + "/" + encryptedAes);
                        in.readLine();
                    }
                    else
                        publishProgress(msg);
                }catch (Exception e){
                    establishConnection();
                }*/
            int x = 0;
            while(x < 1) {
                try {
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                    String msg = "";
                    if(nextMsg.equals("")) {
                        out.println("1" + sourceNumber + targetNumber);
                        msg = in.readLine();
                        if (!msg.equals("0"))
                            publishProgress(msg);
                    }else {
                        out.println("0" + targetNumber + sourceNumber + nextMsg);
                        nextMsg = "";
                    }
                    Thread.sleep(200);
                }catch (Exception e){
                    establishConnection();
                }


            }
            return null;
        }


        private void establishConnection()
        {
            try {
                echoSocket = new Socket(eConstants.SOCKET_HOST, eConstants.SOCKET_PORT);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg = values[0];
            if(msg.contains("Delivered: "))             //Check if msg from server is delivery report
                Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
            else if(msg.charAt(0) == '/')
            {
                SharedPreferences prefs = getSharedPreferences(eConstants.PREFERENCES_FILE, 0);
                String encryptedAes = msg.substring(1);
                String pr = prefs.getString(eConstants.PREFS_PRIVATE_KEY, "SHIEEET");
                String AesKey = Encrypter.RSADecrypt(encryptedAes, prefs.getString(eConstants.PREFS_PRIVATE_KEY, ""));
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(targetNumber + "AES", AesKey);
                editor.commit();
                encrypter.setAESKey(AesKey);
            }
            else {
                web.add(msg);
                imageId.add(targetDrawable);
                adapter.notifyDataSetChanged();
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


    private class WebSigner extends AsyncTask<String,String,String>
    {
        @Override
        protected String doInBackground(String... args) {
            String response = "";
            String url = args[0];
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR", "EXCEPTION");
            }

            Log.i("INFO", "RESPONSE IS: " + response);
            return decodeHtml(response);
        }


        public String decodeHtml(String original)
        {
            String unfiltered = Html.fromHtml(original).toString();
            String filtered = "";
            for(int i = 0; i < unfiltered.length(); i++)
            {
                if(i + 5 < unfiltered.length() && unfiltered.substring(i, i+5).equals("Free "))
                    break;
                filtered += unfiltered.charAt(i);
            }
            return filtered;
        }






        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if(response.equals("1"))
            {
                Toast.makeText(ChatActivity.this, "User not registered", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ChatActivity.this, "Server error:\n" + response, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class MessageRetriever extends AsyncTask<String,Object,String>
    {
        @Override
        protected String doInBackground(String... args) {
            String response = "";
            String url = WEB_HOME + "getpk.php?user=" + targetNumber;
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }


                    if(!response.equals("") && response.length() > 5) {
                        response = decodeHtml(response);
                        ArrayList<String> newMessages = new ArrayList<String>();
                        String message = "";
                        for(int i = 0; i < response.length(); i++)
                        {
                            if(response.charAt(i) != '\n')
                                message += response.charAt(i);
                            else
                                newMessages.add(message);
                        }
                         publishProgress(newMessages.toArray());
                        Log.i("TAG", "SOMETHING WAS RECEIVED!");
                    }
                    response = "";


                }  catch (Exception e)
                {
                    Log.e("ERROR", "EXCEPTION");
                }


            int x=1;
            while(x == 1)
            {
                Log.e("TAG", "LOOOOOOP");
                 response = "";
                if(!nextMsg.equals("")) {
                    url = WEB_HOME + "send.php?from=" + sourceNumber + "&to=" + targetNumber + "&message=" + nextMsg + "&time=" + System.currentTimeMillis();
                    nextMsg = "";
                    Log.d("TAG", "Sent a message");
                }else
                    url = WEB_HOME + "getmsgs.php?user=" + sourceNumber;
                 client = new DefaultHttpClient();
                 httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }


                    if(!response.equals("") && response.length() > 5) {
                        response = decodeHtml(response);
                        ArrayList<String> newMessages = new ArrayList<String>();
                        String message = "";
                        for(int i = 0; i < response.length(); i++)
                        {
                            if(response.charAt(i) != '\n')
                                message += response.charAt(i);
                            else
                                newMessages.add(message);
                        }
                        publishProgress(newMessages.toArray());
                        Log.i("TAG", "SOMETHING WAS RECEIVED!");
                    }
                    response = "";
                    Thread.sleep(500);


                }  catch (Exception e)
                {
                    Log.e("ERROR", "EXCEPTION");
                }
            }

            Log.i("INFO", "RESPONSE IS: " + response);
            return decodeHtml(response);
        }


        public String decodeHtml(String original)
        {
            String unfiltered = Html.fromHtml(original).toString();
            String filtered = "";
            for(int i = 0; i < unfiltered.length(); i++)
            {
                if(i + 5 < unfiltered.length() && unfiltered.substring(i, i+5).equals("Free "))
                    break;
                filtered += unfiltered.charAt(i);
            }
            return filtered;
        }


        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            String encodedMsg;
            String decodedMsg;
            try {
                for(int i = 0; i < values.length; i++) {
                    encodedMsg = values[i].toString();
                    decodedMsg = encodedMsg.substring(1);
                    if(encodedMsg.charAt(0) == '1')
                        encrypter.setAESKey(decodedMsg);
                    else {
                       //WAS ADAPTER.POSTMESSAGE
                    }
                }
            }catch (Exception e){
                int x;
            }
        }


        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if(response.equals("1"))
            {
                Toast.makeText(ChatActivity.this, "User not registered", Toast.LENGTH_SHORT).show();
            }else{
                //Toast.makeText(ChatActivity.this, "Server error:\n" + response, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
