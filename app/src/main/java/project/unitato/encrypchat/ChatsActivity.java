package project.unitato.encrypchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.AndroidCharacter;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class ChatsActivity extends ActionBarActivity {


    private Socket socket;
    String phoneNumber;
    int RESULT_LOGIN = 0;
    int RESULT_CONTACT = 1;
    ListView list;
    ChatsList adapter;
    String nextMsg = "";
    String encryptedAES;
    String targetPublicKey;
    MessageRetriever messageRetriever;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String targetNumber;
    String sourceNumber;
    public static String WEB_HOME = "http://encrypchat.bl.ee/";
    public static String EXTRA_TARGET_NUMBER = "target_number";
    public static String EXTRA_SELF_NUMBER = "number";
    ArrayList<String> web;
    ArrayList<Integer> imageId;
    Encrypter encrypter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getPreferences(0);
        editor = prefs.edit();

        //Check if user registered yet
        if(prefs.getString(eConstants.PREFS_PHONE_NUMEBR, "").equals(""))
        {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, RESULT_LOGIN);
        }else
        {
            phoneNumber = prefs.getString(eConstants.PREFS_PHONE_NUMEBR, "0");
            setTitle("Your phone is: " + phoneNumber.substring(0,3) + "-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6));
            //Intent msgServiceIntent = new Intent(this, MessageService.class);TODO later
            //startService(msgServiceIntent);
        }



        web = new ArrayList<String>();
        imageId = new ArrayList<Integer>();
        if(prefs.getString(eConstants.PREFS_CHATS, "").equals(""))
        {
            web.add("NO CHATS");
            imageId.add(R.drawable.icon);
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
        }
        setContentView(R.layout.activity_chats);
        Typeface msgTypeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        adapter = new ChatsList(ChatsActivity.this, web, imageId, msgTypeface, 40);
        list = (ListView)findViewById(R.id.allchats_list);
        list.setAdapter(adapter);
        encrypter = new Encrypter();







        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                beginChat(web.get(i).toString());
            }
        });

        setTitle("Encrypchat");

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







    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOGIN) {
            phoneNumber = data.getExtras().getString(eConstants.PREFS_PHONE_NUMEBR);
            editor.putString(eConstants.PREFS_PHONE_NUMEBR, phoneNumber);
            editor.commit();
            setTitle(phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6));
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
                            web.add(number);
                            imageId.add(R.id.icon);
                            String allchats = prefs.getString(eConstants.PREFS_CHATS, "");
                            allchats += number + ";" ;
                            editor = prefs.edit();
                            editor.putString(eConstants.PREFS_CHATS, allchats);
                            editor.commit();
                            adapter.notifyDataSetChanged();
                            beginChat(filterNumber(number));
                        }
                    }
                }
            }
        }
    }


    private void beginChat(String number)
    {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra(eConstants.EXTRA_SELF_NUMBER, filterNumber(phoneNumber));
        if(number.charAt(0) >= '0' && number.charAt(0) <= '9')
            i.putExtra(eConstants.EXTRA_TARGET_NUMBER, filterNumber(number));
        else
            i.putExtra(eConstants.EXTRA_TARGET_NUMBER, number);
        startActivity(i);
    }

    public static String filterNumber(String number)
    {
        String filteredNumber = "";
        for(int i = 0; i < number.length(); i++)
            if(number.charAt(i) <= '9' && number.charAt(i) >= '0')
                filteredNumber += number.charAt(i);
        return filteredNumber;
    }



    private class MessageRetriever extends AsyncTask<String,Object,String> {
        @Override
        protected String doInBackground(String... args){
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                String url = WEB_HOME + "send_post.php";//?from=" + sourceNumber + "&to=" + "0504789654" + "&message=" + "HELLO" + "&time=" + System.currentTimeMillis();
                HttpPost httppost = new HttpPost(url);

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                    nameValuePairs.add(new BasicNameValuePair("from", sourceNumber));
                    nameValuePairs.add(new BasicNameValuePair("to", "0504789654"));
                    nameValuePairs.add(new BasicNameValuePair("message", "HELLO"));
                  //  nameValuePairs.add(new BasicNameValuePair("time", Long.toString(System.currentTimeMillis())));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                    Log.i("RESPONSE", decodeResponse(response));

                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
            return null;
        }
        protected String doInBackgrounds(String... args) {
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


                if (!response.equals("") && response.length() > 5) {
                    response = decodeHtml(response);
                    ArrayList<String> newMessages = new ArrayList<String>();
                    String message = "";
                    for (int i = 0; i < response.length(); i++) {
                        if (response.charAt(i) != '\n')
                            message += response.charAt(i);
                        else
                            newMessages.add(message);
                    }
                    publishProgress(newMessages.toArray());
                    Log.i("TAG", "SOMETHING WAS RECEIVED!");
                }
                response = "";


            } catch (Exception e) {
                Log.e("ERROR", "EXCEPTION");
            }


            int x = 1;
            while (x == 1) {
                Log.e("TAG", "LOOOOOOP");
                response = "";
                if (!nextMsg.equals("")) {
                    url = WEB_HOME + "send.php?from=" + sourceNumber + "&to=" + targetNumber + "&message=" + nextMsg + "&time=" + System.currentTimeMillis();
                    nextMsg = "";
                    Log.d("TAG", "Sent a message");
                } else
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


                    if (!response.equals("") && response.length() > 5) {
                        response = decodeHtml(response);
                        ArrayList<String> newMessages = new ArrayList<String>();
                        String message = "";
                        for (int i = 0; i < response.length(); i++) {
                            if (response.charAt(i) != '\n')
                                message += response.charAt(i);
                            else
                                newMessages.add(message);
                        }
                        publishProgress(newMessages.toArray());
                        Log.i("TAG", "SOMETHING WAS RECEIVED!");
                    }
                    response = "";
                    Thread.sleep(500);


                } catch (Exception e) {
                    Log.e("ERROR", "EXCEPTION");
                }
            }

            Log.i("INFO", "RESPONSE IS: " + response);
            return decodeHtml(response);

        }



        private String decodeResponse(HttpResponse httpResponse)
        {
            String response = "";
            try {
                InputStream content = httpResponse.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return response;
        }


        public String decodeHtml(String original) {
            String unfiltered = Html.fromHtml(original).toString();
            String filtered = "";
            for (int i = 0; i < unfiltered.length(); i++) {
                if (i + 5 < unfiltered.length() && unfiltered.substring(i, i + 5).equals("Free "))
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
                for (int i = 0; i < values.length; i++) {
                    encodedMsg = values[i].toString();
                    decodedMsg = encodedMsg.substring(1);
                    if (encodedMsg.charAt(0) == '1')
                        encrypter.setAESKey(decodedMsg);
                    else
                        adapter.postMessage(R.drawable.night_blur, encrypter.AESDecrypt(values[i].toString()));
                }
            } catch (Exception e) {
            }
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
        if(item.getItemId() == R.id.action_new)
        {
            // user BoD suggests using Intent.ACTION_PICK instead of .ACTION_GET_CONTENT to avoid the chooser
            Intent intent = new Intent(Intent.ACTION_PICK);
            // BoD con't: CONTENT_TYPE instead of CONTENT_ITEM_TYPE
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(intent, RESULT_CONTACT);
        }
        return super.onOptionsItemSelected(item);
    }
}
