package project.unitato.encrypchat;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class SecretActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret);
        Uri data = getIntent().getData();
        TextView secretTv = (TextView) findViewById(R.id.secret_tv);
        if(data.getHost() != null)
            setTitle(data.getHost());
        List<String> segments = data.getPathSegments();
        if(segments.size() >= 1)
            secretTv.setText(segments.get(0));
        if(segments.size() >= 2)
            secretTv.setTextSize(Integer.parseInt(segments.get(1)));
    }
}
