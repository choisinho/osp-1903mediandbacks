package app.bqlab.mediandbacks;

import android.app.ActivityManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.PropertyResourceBundle;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        checkUserService();
    }

    private void checkUserService() {
        if (ServiceChecker.isRunning(this, UserService.class.getName())) {

        }
    }
}
