package project.unitato.encrypchat;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Type;


public class HelpActivity extends FragmentActivity {


    HelpCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);


        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        mDemoCollectionPagerAdapter = new HelpCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.help_pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        TextView versionTv = (TextView) findViewById(R.id.design_help_version_tv);
        versionTv.setText(pInfo.versionName);
/*
        PackageInfo pInfo = null;
        try {
             pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        TextView titleTv = (TextView) findViewById(R.id.design_help_title);
        TextView textTv = (TextView) findViewById(R.id.design_help_text);
        TextView versionTv = (TextView) findViewById(R.id.design_help_version_tv);
        versionTv.setText(pInfo.versionName);
        titleTv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"));
        textTv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf"));
        textTv.setMovementMethod(LinkMovementMethod.getInstance());
        textTv.setText(Html.fromHtml("<p> Encrypchat is a high-end secure chat app. But it also contains a few... gimmicks, if you may.</p>" +
                "<p> For instance, you can use the <b>@</b> character to link a social network username, " +
                "like this: <a href=\"https://twitter.com/mkbhd\">@MKBHD</a>.<br>" +
                " You can also link a hashtag using the <b>#</b> character, " +
                "like this: <a href=\"https://www.facebook.com/hashtag/ThroughGlass\">#ThroughGlass</a>.</p>" +
                "<p> You can embold "));
                */




    }




    public class HelpCollectionPagerAdapter extends FragmentPagerAdapter {
        public HelpCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment(getAssets());
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(DemoObjectFragment.ARG_OBJECT, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position);
        }
    }



    public static class DemoObjectFragment extends Fragment {
        public static final String ARG_OBJECT = "object";
        private AssetManager assetManager;

        public DemoObjectFragment(AssetManager assetManager) {
            this.assetManager = assetManager;
        }

        private AssetManager getAssets(){
            return assetManager;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            Bundle args = getArguments();
            int screenId = args.getInt(ARG_OBJECT);
            View rootView = inflater.inflate(R.layout.fragment_help, container, false);
            TextView titleTv = (TextView) rootView.findViewById(R.id.design_help_title);
            TextView textTv = (TextView) rootView.findViewById(R.id.design_help_text);
            Button startBtn = (Button) rootView.findViewById(R.id.help_btn_start);
            titleTv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf"));
            textTv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf"));
            textTv.setMovementMethod(LinkMovementMethod.getInstance());
            AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
            alphaAnimation.setDuration(2000);

            switch (screenId)
            {
                case 0:
                    titleTv.setVisibility(View.GONE);
                    textTv.setText(Html.fromHtml("Welcome to <b>Encrypchat</b>.<br>Encrypchat is a high-end secure chat app. But it also contains a few... <i>gimmicks</i>, if you may.<br><small>(swipe to continue)</small>"));
                    textTv.setGravity(Gravity.CENTER);
                    break;
                case 1:
                    titleTv.setText("Usernames");
                    textTv.setText(Html.fromHtml("Encrypchat will link your favorite social networks whenever you use the <b>@</b> character, like this: <a href=\"https://twitter.com/mkbhd\">@MKBHD</a>"));
                    break;
                case 2:
                    titleTv.setText("Hashtags");
                    textTv.setText(Html.fromHtml("Like usernames, Encrypchat recognizes and links hashtags in your messages whenever you use the <b>#</b> character, like this: <a href=\"https://www.facebook.com/hashtag/ThroughGlass\">#ThroughGlass</a>"));
                    break;
                case 3:
                    titleTv.setText("Social Networks");
                    textTv.setText("You can choose the default social networks in settings.");
                    break;
                case 4:
                    titleTv.setText("Bold Text");
                    textTv.setText(Html.fromHtml("Encrypchat will automatically <b>embolden</b> parts of your message written between two <b>*</b> characters."));
                    break;
                case 5:
                    titleTv.setText("HTML Tags");
                    textTv.setText(Html.fromHtml("For experts and geeks: Encrypchat is a fully suited HTML interpreter. Just put the <pre>&lt;html&gt;</pre> tag at the beginning and code away!"));
                    break;
                case 6:
                    startBtn.setVisibility(View.VISIBLE);
                    titleTv.setText("");
                    textTv.setText("That's it! Enjoy!");
                    textTv.setTextSize(30);
            }


            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });
            return rootView;
        }
    }

}

