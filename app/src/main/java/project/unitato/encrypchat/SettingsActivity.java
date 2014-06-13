package project.unitato.encrypchat;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;


public class SettingsActivity extends ActionBarActivity {


    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        TextView notificationsTv = (TextView) findViewById(R.id.design_settings_notifications_tv);
        notificationsTv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"));
        ((TextView) findViewById(R.id.design_settings_social_tv)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"));
        ((TextView) findViewById(R.id.design_settings_hashtag_tv)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"));
        ((TextView) findViewById(R.id.design_settings_usertag_tv)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"));
        prefs = getSharedPreferences(eConstants.PREFERENCES_FILE, 0);
        editor = prefs.edit();
        RadioGroup userTagRg = (RadioGroup) findViewById(R.id.settings_usertag_radiogroup);
        RadioGroup hashtagRg = (RadioGroup) findViewById(R.id.settings_hashtag_radiogroup);

        final CheckBox soundCb = (CheckBox) findViewById(R.id.checkbox_sound);
        final CheckBox vibCb = (CheckBox) findViewById(R.id.checkbox_vibration);

        soundCb.setChecked(prefs.getBoolean(eConstants.PREFS_SOUND, true));
        vibCb.setChecked(prefs.getBoolean(eConstants.PREFS_VIBRATION, true));

        String usertag = prefs.getString(eConstants.PREFS_SOCIAL_USERTAG_STARTER, eConstants.USERTAG_TWITTER_STARTER);
        String hashtag = prefs.getString(eConstants.PREFS_SOCIAL_HASHTAG_STARTER, eConstants.HASHTAG_FACEBOOK_STARTER);
        if(usertag.equals(eConstants.USERTAG_FACEBOOK_STARTER))
            userTagRg.check(R.id.rb_facebook_user);
        else if(usertag.equals(eConstants.USERTAG_TWITTER_STARTER))
            userTagRg.check(R.id.rb_twitter_user);
        else if(usertag.equals(eConstants.USERTAG_YOUTUBE_STARTER))
            userTagRg.check(R.id.rb_youtube_user);
        else if(usertag.equals(eConstants.USERTAG_TUMBLR_STARTER))
            userTagRg.check(R.id.rb_tumblr_user);

        if(hashtag.equals(eConstants.HASHTAG_FACEBOOK_STARTER))
            hashtagRg.check(R.id.rb_facebook_hashtag);
        else if(hashtag.equals(eConstants.HASHTAG_TWITTER_STARTER))
            hashtagRg.check(R.id.rb_twitter_hashtag);
        else if(hashtag.equals(eConstants.HASHTAG_GOOGLEPLUS_STARTER))
            hashtagRg.check(R.id.rb_googleplus_hashtag);
        else if(hashtag.equals(eConstants.HASHTAG_TUMBLR_STARTER))
            hashtagRg.check(R.id.rb_tumblr_hashtag);


        CheckBox.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switch (compoundButton.getId())
                {
                    case R.id.checkbox_sound:
                        editor.putBoolean(eConstants.PREFS_SOUND, soundCb.isChecked());
                        break;
                    case R.id.checkbox_vibration:
                        editor.putBoolean(eConstants.PREFS_VIBRATION, vibCb.isChecked());
                        break;
                }
                editor.commit();
            }
        };


        RadioGroup.OnCheckedChangeListener onRadioCheckChangedListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (radioGroup.getId()){
                    case R.id.settings_usertag_radiogroup:
                        switch(id){
                            case R.id.rb_facebook_user:
                                editor.putString(eConstants.PREFS_SOCIAL_USERTAG_STARTER, eConstants.USERTAG_FACEBOOK_STARTER);
                                editor.putString(eConstants.PREFS_SOCIAL_USERTAG_ENDER, eConstants.USERTAG_FACEBOOK_ENDER);
                                editor.commit();
                                break;
                            case R.id.rb_twitter_user:
                                editor.putString(eConstants.PREFS_SOCIAL_USERTAG_STARTER, eConstants.USERTAG_TWITTER_STARTER);
                                editor.putString(eConstants.PREFS_SOCIAL_USERTAG_ENDER, eConstants.USERTAG_TWITTER_ENDER);
                                editor.commit();
                                break;
                            case R.id.rb_youtube_user:
                                editor.putString(eConstants.PREFS_SOCIAL_USERTAG_STARTER, eConstants.USERTAG_YOUTUBE_STARTER);
                                editor.putString(eConstants.PREFS_SOCIAL_USERTAG_ENDER, eConstants.USERTAG_YOUTUBE_ENDER);
                                editor.commit();
                                break;
                            case R.id.rb_tumblr_user:
                                editor.putString(eConstants.PREFS_SOCIAL_USERTAG_STARTER, eConstants.USERTAG_TUMBLR_STARTER);
                                editor.putString(eConstants.PREFS_SOCIAL_USERTAG_ENDER, eConstants.USERTAG_TUMBLR_ENDER);
                                editor.commit();
                                break;
                        }
                        break;

                    case R.id.settings_hashtag_radiogroup:
                        switch(id){
                            case R.id.rb_facebook_hashtag:
                                editor.putString(eConstants.PREFS_SOCIAL_HASHTAG_STARTER, eConstants.HASHTAG_FACEBOOK_STARTER);
                                editor.putString(eConstants.PREFS_SOCIAL_HASHTAG_ENDER, eConstants.HASHTAG_FACEBOOK_ENDER);
                                editor.commit();
                                break;
                            case R.id.rb_twitter_hashtag:
                                editor.putString(eConstants.PREFS_SOCIAL_HASHTAG_STARTER, eConstants.HASHTAG_TWITTER_STARTER);
                                editor.putString(eConstants.PREFS_SOCIAL_HASHTAG_ENDER, eConstants.HASHTAG_TWITTER_ENDER);
                                editor.commit();
                                break;
                            case R.id.rb_googleplus_hashtag:
                                editor.putString(eConstants.PREFS_SOCIAL_HASHTAG_STARTER, eConstants.HASHTAG_GOOGLEPLUS_STARTER);
                                editor.putString(eConstants.PREFS_SOCIAL_HASHTAG_ENDER, eConstants.HASHTAG_GOOGLEPLUS_ENDER);
                                editor.commit();
                                break;
                            case R.id.rb_tumblr_hashtag:
                                editor.putString(eConstants.PREFS_SOCIAL_HASHTAG_STARTER, eConstants.HASHTAG_TUMBLR_STARTER);
                                editor.putString(eConstants.PREFS_SOCIAL_HASHTAG_ENDER, eConstants.HASHTAG_TUMBLR_ENDER);
                                editor.commit();
                                break;
                        }
                        break;
                }
            }
        };

        soundCb.setOnCheckedChangeListener(onCheckedChangeListener);
        vibCb.setOnCheckedChangeListener(onCheckedChangeListener);
        userTagRg.setOnCheckedChangeListener(onRadioCheckChangedListener);
        hashtagRg.setOnCheckedChangeListener(onRadioCheckChangedListener);
    }

}
