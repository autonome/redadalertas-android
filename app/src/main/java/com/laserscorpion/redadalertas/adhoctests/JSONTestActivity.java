package com.laserscorpion.redadalertas.adhoctests;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.laserscorpion.redadalertas.Alert;
import com.laserscorpion.redadalertas.AlertJSONParser;
import com.laserscorpion.redadalertas.R;
import com.laserscorpion.redadalertas.TorURLLoader;
import com.laserscorpion.redadalertas.URLDataReceiver;
import com.laserscorpion.redadalertas.db.AlertsDatabaseHelper;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class JSONTestActivity extends AppCompatActivity implements URLDataReceiver {
    private static final String URL = "https://laserscorpion.com/other/example.json";

    private TorURLLoader loader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tor_check);

        try {
            loader = new TorURLLoader(this, new URL(URL), this);
            loader.start();
        } catch (MalformedURLException e) {
            // let's just not malform the URL, ok?
        }
    }

    @Override
    protected void onDestroy() {
        if (loader != null)
            loader.cancel();
        super.onDestroy();
    }

    @Override
    public void requestComplete(final boolean successful, final String data) {
        loader = null;
        AlertsDatabaseHelper db = new AlertsDatabaseHelper(this);
        db.reset(); // for testing...
        String result = new String();

        if (successful) {
            try {
                ArrayList<Alert> alerts = AlertJSONParser.parse(data);
                db.addNewAlerts(alerts);
                ArrayList<Alert> backOut = db.getAlertsSince(new Date(0)); // race condition here ... synchronized doesn't help
                for (Alert alert : backOut) {
                    result += alert + "<br><br>";
                }
                if (alerts.size() != backOut.size())
                    result = "a bad thing happened to the db. better have a look.";
            } catch (JSONException e) {
                e.printStackTrace();
                result = "json fail!";
            }
        } else
            result = "uhhhh failure: " + data;

        final String text = result;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView web = (WebView)findViewById(R.id.tor_web_view);
                web.loadData(text, "text/html", "utf-8");
            }
        });
    }
}
