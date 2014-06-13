package project.unitato.encrypchat;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;


public class LoginActivity extends ActionBarActivity {


    KeyPair kp;
    String phoneNumber;
    TextView welcomeTv;
    EditText numEt;
    RelativeLayout mainLayout;
    Socket echoSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        welcomeTv = (TextView) findViewById(R.id.welcome_tv);
        numEt = (EditText) findViewById(R.id.login_num_et);
        mainLayout = (RelativeLayout) findViewById(R.id.login_layout);

        Typeface robotoThin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        Typeface robotoCondensedLight = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Light.ttf");

        AlphaAnimation fullFadeInAnim = new AlphaAnimation(0,1);
        fullFadeInAnim.setDuration(2000);
        fullFadeInAnim.setFillAfter(true);



        welcomeTv.setTypeface(robotoThin);
        numEt.setTypeface(robotoLight);

        welcomeTv.startAnimation(fullFadeInAnim);


        if(Build.VERSION.SDK_INT<16)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            goFullscreen();




        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = tMgr.getLine1Number();
        numEt.setText(phoneNumber);
    }



    @SuppressLint("NewApi")
    void goFullscreen()
    {
        if(Build.VERSION.SDK_INT >= 16){
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getActionBar();
            actionBar.hide();
        }
    }


    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.login_layout:
                if(numEt.getVisibility() == View.INVISIBLE) {
                    AlphaAnimation fullFadeOutAnim = new AlphaAnimation(1, 0);
                    fullFadeOutAnim.setFillAfter(true);
                    fullFadeOutAnim.setDuration(1000);
                    welcomeTv.startAnimation(fullFadeOutAnim);
                    AlphaAnimation fadeIn = new AlphaAnimation(0, 0.7f);
                    fadeIn.setFillAfter(true);
                    fadeIn.setDuration(1000);
                    fadeIn.setStartTime(AnimationUtils.currentAnimationTimeMillis() + 1000);
                    numEt.setAnimation(fadeIn);
                    numEt.setVisibility(View.VISIBLE);
                }else
                {
                    phoneNumber = numEt.getText().toString();
                    if(isValidPhone(phoneNumber))
                    {
                        if(phoneNumber.charAt(0) == '0')
                            phoneNumber = "972" + phoneNumber.substring(1);
                        new mTask2().execute(phoneNumber);
                    }
                }
                break;
        }


    }



    boolean isValidPhone(String number)
    {
        if(number.length() == 10)
            return true;
        else
            return false;
    }




    private class mTask2 extends AsyncTask<String, String, String>
    {

        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setTitle("Registration");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }



        @Override
        protected String doInBackground(String... params) {
            try {
                publishProgress("Generating an RSA key pair...");
                JSONObject data = new JSONObject();
                String number = params[0];
                data.put(eConstants.JSON_MSG_FROM, number);
                data.put(eConstants.JSON_SERVER_REQTYPE, eConstants.REQTYPE_REGISTER);
                kp = Encrypter.GenerateRSAKeyPair();
                publishProgress("Connecting...");
                echoSocket = new Socket(eConstants.SOCKET_HOST, eConstants.SOCKET_PORT);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                data.put(eConstants.JSON_MSG_CONTENT, Encrypter.public2string(kp.getPublic()));
                publishProgress("Registering...");
                out.println(data.toString());
                String response = in.readLine();
                echoSocket.close();
                publishProgress("Done. Saving data...");
                return response;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            dialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if(response.equals("0")){
                String privateKey = Encrypter.private2string(kp.getPrivate());
                SharedPreferences.Editor editor = getSharedPreferences(eConstants.PREFERENCES_FILE, 0).edit();
                editor.putString(eConstants.PREFS_PRIVATE_KEY, privateKey);
                editor.commit();

                Intent data = new Intent();
                data.putExtra(eConstants.PREFS_PHONE_NUMEBR, phoneNumber);
                setResult(0, data);
                dialog.dismiss();
                finish();
            }else if(response.equals("1"))
            {
                Toast.makeText(LoginActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(LoginActivity.this, "Server error:\n" + response, Toast.LENGTH_SHORT).show();
            }
        }
    }






    private class mTask extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... data) {
            try {
                String number = data[0];
                kp = Encrypter.GenerateRSAKeyPair();
                echoSocket = new Socket(eConstants.SOCKET_HOST, eConstants.SOCKET_PORT);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                out.println("2" + number + Encrypter.public2string(kp.getPublic()));
                Log.i("TAG", "printed");
                String response = in.readLine();
                echoSocket.close();
                return response;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if(response.equals("0")){
                String privateKey = Encrypter.private2string(kp.getPrivate());
                SharedPreferences.Editor editor = getSharedPreferences(eConstants.PREFERENCES_FILE, 0).edit();
                editor.putString(eConstants.PREFS_PRIVATE_KEY, privateKey);
                editor.commit();

                Intent data = new Intent();
                data.putExtra(eConstants.PREFS_PHONE_NUMEBR, phoneNumber);
                setResult(0, data);
                finish();
            }else if(response.equals("1"))
            {
                Toast.makeText(LoginActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(LoginActivity.this, "Server error:\n" + response, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class WebSigner extends AsyncTask<String,String,String>
    {

        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setTitle("Registration");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            String response = "";
            publishProgress("Generating RSA keys...");
            kp = Encrypter.GenerateRSAKeyPair();
            String publicKey = Encrypter.public2string(kp.getPublic());
            String url = args[0] + publicKey;
            publishProgress("Registering...");
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


            if(response.equals("0"))
                publishProgress("Success! Saving data...");
            return decodeHtml(response);
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            dialog.setMessage(values[0]);
            Log.i("TAG", "Progupdate");
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
            if(response.equals("0") || response.equals("1")){
                String privateKey = Encrypter.private2string(kp);
                SharedPreferences.Editor editor = getSharedPreferences(eConstants.PREFERENCES_FILE, 0).edit();
                editor.putString(eConstants.PREFS_PRIVATE_KEY, privateKey);

                Intent data = new Intent();
                data.putExtra(eConstants.PREFS_PHONE_NUMEBR, phoneNumber);
                setResult(0, data);
                finish();
            }else if(response.equals("1"))
            {
                Toast.makeText(LoginActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(LoginActivity.this, "Server error:\n" + response, Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        }
    }




}
