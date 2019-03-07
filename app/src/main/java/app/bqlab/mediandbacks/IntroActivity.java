package app.bqlab.mediandbacks;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        checkUserService();
    }

    private void checkUserService() {
        if (ServiceChecker.isRunning(this, UserService.class.getName())) {
            startService(new Intent(this, MainActivity.class));
            finish();
        } else {
            startService(new Intent(this, StartActivity.class));
            finish();
        }
    }
}
